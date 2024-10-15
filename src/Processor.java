import java.io.Closeable;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * This class calculates certain statistics for a flow log based on mappings of its {@link Protocol}s to {@link Tags}.
 */
class Processor implements Runnable, Closeable {
    private static final MemoryMXBean MEMORY = ManagementFactory.getMemoryMXBean();
    private static final int DESTINATION_PORT = 6;
    private static final int PROTOCOL = 7;

    private final CSVSupplier flowLog;
    private final Map<Protocol, String> tags;
    private final CSVConsumer input = Settings.DEBUG ? new CSVFileWriter(Constants.INPUT_PATH) : CSVConsumer.NOOP;
    private final CSVConsumer output;
    private final Collector<Protocol, ?, Map.Entry<Map<String, Long>, Map<Protocol, Long>>> countingCollector =
        Collectors.teeing(
            Collectors.groupingByConcurrent(this::getTag, Collectors.counting()),
            Collectors.groupingByConcurrent(Function.identity(), Collectors.counting()),
            Map::entry // We're simply using this as a pair/2-tuple. This becomes infeasible with more data points.
        );

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    Processor(CSVSupplier flowLog, CSVConsumer output) {
        this(flowLog, Tags.DEFAULT, output);
    }

    Processor(CSVSupplier flowLog, Map<Protocol, String> tags, CSVConsumer output) {
        this.flowLog = flowLog;
        this.tags = tags;
        this.output = output;
    }

    //==================================================================================================================
    // Runnable Implementation Methods
    //==================================================================================================================

    @Override
    public void run() {
        final var startTime = System.currentTimeMillis();
        final var startMemory = MEMORY.getHeapMemoryUsage().getUsed();

        try (var rows = flowLog.get()) {
            final Map.Entry<Map<String, Long>, Map<Protocol, Long>> counts =
                rows
                    .parallel()
                    .unordered()
                    .peek(input::row) // Potentially write the input data to a file for debugging purposes.
                    .map(this::toProtocol)
                    .collect(countingCollector);
            writeTags(counts.getKey());
            writeCombinations(counts.getValue());
        } finally {
            System.out.format(
                "Processed in %d ms using %.2f MB of heap memory (may be inaccurate due to garbage collection)%n",
                System.currentTimeMillis() - startTime,
                (MEMORY.getHeapMemoryUsage().getUsed() - startMemory) / 1_000_000D
            );
        }
    }

    //==================================================================================================================
    // AutoCloseable Implementation Methods
    //==================================================================================================================

    @Override
    public void close() throws IOException {
        input.close();
        output.close();
    }

    //==================================================================================================================
    // Private Helper Methods
    //==================================================================================================================

    private void writeTags(Map<String, Long> tags) {
        output.row("Tag Counts:");
        output.row("Tag", "Count");
        tags.forEach(this::writeTag);
        output.row();
    }

    private void writeTag(String tag, long count) {
        output.row(tag.equals(Constants.UNKNOWN) ? "Untagged" : tag, String.valueOf(count));
    }

    private void writeCombinations(Map<Protocol, Long> combinations) {
        output.row("Port/Protocol Combination Counts:");
        output.row("Port", "Protocol", "Count");
        combinations.forEach(this::writeCombination);
        output.row();
    }

    private void writeCombination(Protocol protocol, long count) {
        output.row(String.valueOf(protocol.port()), protocol.name(), String.valueOf(count));
    }

    private Protocol toProtocol(String[] columns) {
        final var ianaProtocol =
            IANAProtocols.DEFAULT.getOrDefault(Integer.parseInt(columns[PROTOCOL]), Protocol.UNKNOWN);
        return new Protocol(columns[DESTINATION_PORT], ianaProtocol.name());
    }

    private String getTag(Protocol protocol) {
        return tags.getOrDefault(protocol, Protocol.UNKNOWN.name());
    }
}
