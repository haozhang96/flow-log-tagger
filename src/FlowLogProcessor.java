import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class calculates certain statistics for a flow log based on mappings of its {@link Protocol}s to {@link Tags}.
 */
class FlowLogProcessor implements Runnable {
    private static final AtomicBoolean WARMED_UP = new AtomicBoolean();
    private static final Consumer<String[]> NOOP_DEBUGGER = row -> {};
    private static final int DESTINATION_PORT = 6;
    private static final int PROTOCOL = 7;

    private final TableSupplier input;
    private final Tags tags;
    private final TableConsumer output;
    private final TableConsumer debug = Settings.DEBUG ? new TableFileWriter(Constants.DEBUG_PATH) : TableConsumer.NOOP;

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
        Loggers.INFO.accept("[%%] Processing flow log using %s...".formatted(input));

        try (var rows = input.get()) {
            final var counts = toCounts(rows, row -> rowCount.getAndIncrement(), debug::rows);
            printOutput(counts.getKey(), counts.getValue());

            output
                .row("Tag Counts:")
                .row("Tag", "Count");
            counts
                .getKey()
                .forEach((tag, count) -> output.row(tag, String.valueOf(count)));
            output
                .row()
                .row("Port/Protocol Combination Counts:")
                .row("Port", "Protocol", "Count");
            counts
                .getValue()
                .forEach((protocol, count) -> output.row(protocol.port(), protocol.name(), String.valueOf(count)));
        } finally {
            Utils.releaseResources(input, output, debug);
            printStatistics(startTime, rowCount);
        }
    }

    //==================================================================================================================
    // Private Helper Methods
    //==================================================================================================================

    @SafeVarargs
    private Map.Entry<Map<String, Long>, Map<Protocol, Long>> toCounts(
        Stream<String[]> rows,
        Consumer<String[]>... debuggers
    ) {
        return (Settings.PARALLEL ? rows.parallel() : rows.sequential()) // Use parallel computation, if allowed.
            .unordered() // Lift any potential ordering constraint - if the data source allows it.
            .peek(Settings.DEBUG ? Stream.of(debuggers).reduce(Consumer::andThen).orElse(NOOP_DEBUGGER) : NOOP_DEBUGGER)
            .map(this::toProtocol)
            .collect(Collectors.teeing(
                Collectors.groupingByConcurrent(this::getTag, Utils.countingCollector()),
                Collectors.groupingByConcurrent(Function.identity(), Utils.countingCollector()),
                Map::entry // We're simply using this as a pair/2-tuple. This becomes infeasible with more data points.
            ));
    }

    private Protocol toProtocol(String[] columns) {
        final var ianaProtocol = Constants.IANA_PROTOCOLS.getOrDefault(columns[PROTOCOL], Protocol.UNKNOWN);
        return Protocol.of(columns[DESTINATION_PORT], ianaProtocol.name());
    }

    private String getTag(Protocol protocol) {
        return tags.getOrDefault(protocol, "Untagged");
    }

    private void printOutput(Map<String, Long> tags, Map<Protocol, Long> combinations) {
        if (Settings.DEBUG) {
            Loggers.INFO.accept("[#] Processed [%d] tag(s): %s".formatted(tags.size(), tags));
            Loggers.INFO.accept("[#] Processed [%d] combination(s): %s".formatted(combinations.size(), combinations));
        } else {
            Loggers.INFO.accept("[#] Processed [%d] tag(s), [%d] combination(s)".formatted(
                tags.size(),
                combinations.size()
            ));
        }
    }

    private void printStatistics(Instant startTime, Number rowCount) {
        final var duration = Duration.between(startTime, Instant.now()).toNanos() / 1_000_000_000D;
        if (Settings.DEBUG) {
            Loggers.INFO.accept("[#] Processed flow log with %d rows (~%.2f MiB) in %.5f seconds.".formatted(
                rowCount.longValue(),
                rowCount.longValue() * Constants.FLOW_LOG_RECORD_SIZE / (double) Constants.MEBIBYTE_SCALE,
                duration
            ));
        } else {
            Loggers.INFO.accept("[#] Processed flow log in %.5f seconds.".formatted(duration));
        }
    }

    /**
     * Warm up the Java virtual runtime by repeatedly running through "hot spots" to hint to the just-in-time compiler
     *   to inline certain methods. This is done only once per JVM start-up, but cannot be done using a static
     *   initialization block due to circular class initialization.
     */
    private static void warmUp() {
        if (Settings.WARM_UP && WARMED_UP.compareAndSet(false, true)) {
            Loggers.INFO.accept("[!] Warming up the Java virtual machine...");
            new FlowLogProcessor(FlowLogGenerator.ofMebibytes(1 << 10), TableConsumer.NOOP).run();
        }
    }
}
