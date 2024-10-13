import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

class Processor implements Runnable, Closeable {
    private static final int DESTINATION_PORT = 6;
    private static final int PROTOCOL = 7;

    private final CSVGenerator flowLog;
    private final Map<Protocol, String> tags;
    private final CSVSink input = Settings.DEBUG ? new CSVFileWriter(Constants.INPUT_PATH) : CSVSink.NOOP;
    private final CSVSink output;
    private final Collector<Protocol, ?, Map.Entry<Map<String, Long>, Map<Protocol, Long>>> countingCollector =
        Collectors.teeing(
            Collectors.groupingByConcurrent(this::getTag, Collectors.counting()),
            Collectors.groupingByConcurrent(Function.identity(), Collectors.counting()),
            Map::entry // We're simply using this as a pair/2-tuple. This becomes infeasible with more data points.
        );

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    Processor(CSVGenerator flowLog, Map<Protocol, String> tags, CSVSink output) {
        this.flowLog = flowLog;
        this.tags = tags;
        this.output = output;
    }

    //==================================================================================================================
    // Runnable Implementation Methods
    //==================================================================================================================

    @Override
    public void run() {
        final long started = System.currentTimeMillis();

        try (var rows = flowLog.get()) {
            final Map.Entry<Map<String, Long>, Map<Protocol, Long>> counts =
                rows
                    .parallel()
                    .peek(input::row) // Potentially write the input data to a file for debugging purposes.
                    .map(this::toProtocol)
                    .collect(countingCollector);

            writeTags(counts.getKey());
            output.newLine();
            writeCombinations(counts.getValue());
            output.newLine();
        } finally {
            System.out.format("Processed in %d ms%n", System.currentTimeMillis() - started);
        }
    }

    //==================================================================================================================
    // AutoCloseable Implementation Methods
    //==================================================================================================================

    @Override
    public void close() throws IOException {
        output.close();
    }

    //==================================================================================================================
    // Private Helper Methods
    //==================================================================================================================

    private void writeTags(Map<String, Long> tags) {
        output.row("Tag Counts:");
        output.row("Tag", "Count");
        tags.forEach(this::writeTag);
    }

    private void writeTag(String tag, long count) {
        output.row(tag.equals(Constants.UNKNOWN) ? "Untagged" : tag, String.valueOf(count));
    }

    private void writeCombinations(Map<Protocol, Long> combinations) {
        output.row("Port/Protocol Combination Counts:");
        output.row("Port", "Protocol", "Count");
        combinations.forEach(this::writeCombination);
    }

    private void writeCombination(Protocol protocol, long count) {
        output.row(String.valueOf(protocol.port()), protocol.name(), String.valueOf(count));
    }

    private Protocol toProtocol(String[] columns) {
        final var name = IANAProtocols.DEFAULT.getOrDefault(columns[PROTOCOL], Protocol.UNKNOWN).name();
        return new Protocol(columns[DESTINATION_PORT], name);
    }

    private String getTag(Protocol protocol) {
        return tags.getOrDefault(protocol, Protocol.UNKNOWN.name());
    }
}
