import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Processor implements Runnable {
    private static final int DESTINATION_PORT = 6;
    private static final int PROTOCOL = 7;

    private final CSVGenerator flowLog;
    private final LookupTable lookupTable;
    private final CSVWriter output;

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    Processor(CSVGenerator flowLog, LookupTable lookupTable, CSVWriter output) {
        this.flowLog = flowLog;
        this.lookupTable = lookupTable;
        this.output = output;
    }

    //==================================================================================================================
    // Runnable Implementation Methods
    //==================================================================================================================

    @Override
    public void run() {
        final long started = System.currentTimeMillis();
        try (var rows = flowLog.get()) {
            process(rows);
        } finally {
            System.out.format("Processed in %d ms%n", System.currentTimeMillis() - started);
        }
    }

    //==================================================================================================================
    // Private Helper Methods
    //==================================================================================================================

    private void process(Stream<String[]> rows) {
        final var input = new CSVWriter(Constants.INPUT_PATH, Constants.WRITE_OPTIONS);
        final Map.Entry<Map<String, Long>, Map<Protocol, Long>> counts =
            rows
                .parallel()
                .peek(input::columns) // Write the input data to a file for inspection later.
                .map(this::toProtocol)
                .collect(Collectors.teeing(
                    Collectors.groupingBy(this::getTag, Collectors.counting()),
                    Collectors.groupingBy(Function.identity(), Collectors.counting()),
                    Map::entry // We're simply using this as a pair/2-tuple.
                ));

        writeTags(counts.getKey());
        output.newline();
        writeCombinations(counts.getValue());
        output.newline();
    }

    private void writeTags(Map<String, Long> tags) {
        output.columns("Tag Counts:");
        output.columns("Tag", "Count");
        tags.forEach(this::writeTag);
    }

    private void writeTag(String tag, long count) {
        output.columns(tag.equals(Constants.UNKNOWN) ? "Untagged" : tag, String.valueOf(count));
    }

    private void writeCombinations(Map<Protocol, Long> combinations) {
        output.columns("Port/Protocol Combination Counts:");
        output.columns("Port", "Protocol", "Count");
        combinations.forEach(this::writeCombination);
    }

    private void writeCombination(Protocol protocol, long count) {
        output.columns(String.valueOf(protocol.port()), protocol.name(), String.valueOf(count));
    }

    private Protocol toProtocol(String[] columns) {
        final var name = Constants.IANA_PROTOCOLS.getOrDefault(columns[PROTOCOL], Protocol.UNKNOWN).name();
        return new Protocol(columns[DESTINATION_PORT], name);
    }

    private String getTag(Protocol protocol) {
        return lookupTable.getOrDefault(protocol, Protocol.UNKNOWN.name());
    }
}
