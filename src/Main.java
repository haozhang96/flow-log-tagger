import java.nio.file.Path;

public class Main {
    //==================================================================================================================
    // Bootstrap
    //==================================================================================================================

    public static void main(String[] args) {
        final var flowLog = new CSVReader(args.length > 0 ? Path.of(args[0]) : Constants.FLOW_LOG_PATH);
        final var lookupTable = new LookupTable(new CSVReader(args.length > 1 ? Path.of(args[1]) : Constants.LOOKUP_TABLE_PATH));
        final var output = new CSVWriter(args.length > 2 ? Path.of(args[2]) : Constants.OUTPUT_PATH, Constants.WRITE_OPTIONS);
        new Processor(flowLog, lookupTable, output).run();
    }
}
