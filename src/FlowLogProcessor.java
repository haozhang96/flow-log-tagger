import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * This class calculates certain statistics for a flow log based on mappings of its {@link Protocol}s to {@link Tags}.
 */
class FlowLogProcessor implements Runnable {
    private static final AtomicBoolean WARMED_UP = new AtomicBoolean();
    private static final int DESTINATION_PORT = 6;
    private static final int PROTOCOL = 7;

    private final TableSupplier input;
    private final Tags tags;
    private final TableConsumer output;
    private final TableConsumer debug = Settings.DEBUG ? new TableFileWriter(Constants.DEBUG_PATH) : TableConsumer.NOOP;
    private final Collector<Protocol, ?, Map.Entry<Map<String, Long>, Map<Protocol, Long>>> countingCollector =
        Collectors.teeing(
            Collectors.groupingByConcurrent(this::getTag, ConcurrentSkipListMap::new, Collectors.counting()),
            Collectors.groupingByConcurrent(Function.identity(), ConcurrentSkipListMap::new, Collectors.counting()),
            Map::entry // We're simply using this as a pair/2-tuple. This becomes infeasible with more data points.
        );

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    FlowLogProcessor(TableSupplier input, TableConsumer output) {
        this(input, null, output);
    }

    FlowLogProcessor(TableSupplier input, Tags tags, TableConsumer output) {
        this.input = Objects.requireNonNull(input);
        this.tags = Objects.requireNonNullElse(tags, Constants.TAGS);
        this.output = Objects.requireNonNull(output);
        warmUp();
    }

    //==================================================================================================================
    // Runnable Implementation Methods
    //==================================================================================================================

    @Override
    public void run() {
        final var startTime = Instant.now();
        final var rowCount = new AtomicLong();
        final Consumer<String[]> rowCounter = Settings.DEBUG ? ignored -> rowCount.incrementAndGet() : ignored -> { };
        System.out.format("[%%] Processing %s...%n", input);

        try (var rows = input.get()) {
            final Map.Entry<Map<String, Long>, Map<Protocol, Long>> counts =
                rows
                    .parallel()
                    .unordered()
                    .peek(rowCounter.andThen(debug::rows)) // Potentially write the input data to a file for debugging.
                    .map(this::toProtocol)
                    .collect(countingCollector);

            output
                .row("Tag Counts:")
                .row("Tag", "Count");
            counts
                .getKey()
                .forEach(this::writeTags);
            output
                .row()
                .row("Port/Protocol Combination Counts:")
                .row("Port", "Protocol", "Count");
            counts
                .getValue()
                .forEach(this::writeCombinations);
        } finally {
            releaseResources();

            final var duration = Duration.between(startTime, Instant.now()).toNanos() / 1_000_000_000D;
            if (Settings.DEBUG) {
                final var size = rowCount.get() * Constants.FLOW_LOG_RECORD_SIZE / (double) Constants.MEBIBYTE_SCALE;
                System.out.format("[#] Processed %d rows (~%.2f MiB) in %.5f seconds.%n", rowCount.get(), size, duration);
            } else {
                System.out.format("[#] Processed in %.5f seconds.%n", duration);
            }
        }
    }

    //==================================================================================================================
    // Private Helper Methods
    //==================================================================================================================

    private void writeTags(String tag, long count) {
        output.row(tag.equals(Constants.UNKNOWN) ? "Untagged" : tag, count);
    }

    private void writeCombinations(Protocol protocol, long count) {
        // Invoke the string-only overloaded version of the method to avoid stream creations.
        output.row(String.valueOf(protocol.port()), protocol.name(), String.valueOf(count));
    }

    private Protocol toProtocol(String[] columns) {
        final var ianaProtocol = Constants.IANA_PROTOCOLS.getOrDefault(columns[PROTOCOL], Protocol.UNKNOWN);
        return Protocol.of(columns[DESTINATION_PORT], ianaProtocol.name());
    }

    private String getTag(Protocol protocol) {
        return tags.getOrDefault(protocol, Protocol.UNKNOWN.name());
    }

    private void releaseResources() {
        for (final var resource : List.of(input, output, debug)) {
            if (!(resource instanceof AutoCloseable closeable)) {
                continue;
            }

            try {
                closeable.close();
            } catch (Exception exception) {
                // Ignore and continue closing the rest of the resources.
            }
        }
    }

    /**
     * Warm up the Java virtual runtime by repeatedly running through "hot spots" to hint to the just-in-time compiler
     *   to inline certain methods. This is done only once per JVM start-up, but cannot be done using a static
     *   initialization block due to circular class initialization.
     */
    private static void warmUp() {
        if (WARMED_UP.compareAndSet(false, true)) {
            System.out.println("[!] Warming up the Java virtual machine...");
            new FlowLogProcessor(FlowLogGenerator.ofMebibytes(1 << 10), TableConsumer.NOOP).run();
        }
    }
}
