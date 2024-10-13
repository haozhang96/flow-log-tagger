public class Test {
    //==================================================================================================================
    // Bootstrap
    //==================================================================================================================

    public static void main(String[] args) {
        final var flowLog = new FlowLogGenerator(args.length > 0 ? Long.parseLong(args[0]) : 100_000L); // Roughly 10 MB
        final var lookupTable = new LookupTable(new CSVReader(Constants.LOOKUP_TABLE_PATH));
        final var output = new CSVWriter(Constants.OUTPUT_PATH, Constants.WRITE_OPTIONS);
        new Processor(flowLog, lookupTable, output).run();
    }
}
