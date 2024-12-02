import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * This class calculates certain statistics for a flow log based on mappings of its {@link Protocol}s to {@link Tags}.
 */
class FlowLogProcessor implements Runnable, Closeable {
    private static final AtomicBoolean WARMED_UP = new AtomicBoolean();
    private static final int DESTINATION_PORT = 6;
    private static final int PROTOCOL = 7;

    private final TableSupplier input;
    private final Tags tags;
    private final TableConsumer output;
    private final TableConsumer debug = Settings.DEBUG ? new TableFileWriter(Constants.DEBUG_PATH) : TableConsumer.NOOP;
    private final Collector<Protocol, ?, Map.Entry<Map<String, Long>, Map<Protocol, Long>>> countingCollector =
        Collectors.teeing(
            Collectors.groupingByConcurrent(this::getTag, Collectors.counting()),
            Collectors.groupingByConcurrent(Function.identity(), Collectors.counting()),
            Map::entry // We're simply using this as a pair/2-tuple. This becomes infeasible with more data points.
        );

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    FlowLogProcessor(TableSupplier input, TableConsumer output) {
        this(input, null, output);
    }

    FlowLogProcessor(TableSupplier input, Tags tags, TableConsumer output) {
        this.input = input;
        this.tags = Objects.requireNonNullElse(tags, Constants.TAGS);
        this.output = output;
        warmUp(); // This is done only once per JVM start-up, but cannot be done using a static initialization block.
    }

    //==================================================================================================================
    // Runnable Implementation Methods
    //==================================================================================================================

    @Override
    public void run() {
        final var startTime = Instant.now();
        final var rowCount = new AtomicLong();
        System.out.format("Processing %s...%n", input);

        try (var rows = input.get()) {
            final Consumer<String[]> rowCounter =
                Settings.DEBUG ? ignored -> rowCount.incrementAndGet() : ignored -> {};
            final Map.Entry<Map<String, Long>, Map<Protocol, Long>> counts =
                rows
                    .parallel()
                    .unordered()
                    .peek(rowCounter.andThen(debug::row)) // Potentially write the input data to a file for debugging.
                    .map(this::toProtocol)
                    .collect(countingCollector);

            writeTags(counts.getKey());
            output.row();
            writeCombinations(counts.getValue());
        } finally {
            final var duration = Duration.between(startTime, Instant.now()).toNanos() / 1_000_000_000D;
            if (Settings.DEBUG) {
                final var size = rowCount.get() * Constants.FLOW_LOG_RECORD_SIZE / (double) Constants.MEBIBYTE_SCALE;
                System.out.format("Processed %d rows / ~%.2f MiB in %.5f seconds.%n", rowCount.get(), size, duration);
            } else {
                System.out.format("Processed in %.5f seconds.%n", duration);
            }
        }
    }

    //==================================================================================================================
    // AutoCloseable Implementation Methods
    //==================================================================================================================

    @Override
    @SuppressWarnings("EmptyTryBlock")
    public void close() throws IOException {
        try (output; debug) {
            // We use this try-with-resources statement to ensure all resources are closed regardless of exceptions.
        }
    }

    //==================================================================================================================
    // Private Helper Methods
    //==================================================================================================================

    private void writeTags(Map<String, Long> tags) {
        output
            .row("Tag Counts:")
            .row("Tag", "Count");
        tags.forEach((tag, count) -> output.row(tag.equals(Constants.UNKNOWN) ? "Untagged" : tag, count));
    }

    private void writeCombinations(Map<Protocol, Long> combinations) {
        output
            .row("Port/Protocol Combination Counts:")
            .row("Port", "Protocol", "Count");
        combinations.forEach((protocol, count) -> output.row(protocol.port(), protocol.name(), count));
    }

    private Protocol toProtocol(String[] columns) {
        final var ianaProtocol = Constants.IANA_PROTOCOLS.getOrDefault(columns[PROTOCOL], Protocol.UNKNOWN);
        return new Protocol(columns[DESTINATION_PORT], ianaProtocol.name());
    }

    private String getTag(Protocol protocol) {
        return tags.getOrDefault(protocol, Protocol.UNKNOWN.name());
    }

    private static void warmUp() {
        if (WARMED_UP.compareAndSet(false, true)) {
            try (var processor = new FlowLogProcessor(FlowLogGenerator.ofMebibytes(1000L), TableConsumer.NOOP)) {
                System.out.println("Warming up the Java virtual machine...");
                processor.run();
            } catch (IOException exception) {
                // We don't care; this is only a warm-up.
            }
        }
    }
}
