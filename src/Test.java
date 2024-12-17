public class Test {
    //==================================================================================================================
    // Bootstrap
    //==================================================================================================================

    public static void main(String[] args) {
        final var input = FlowLogGenerator.ofMebibytes(args.length > 0 ? Double.parseDouble(args[0]) : 10L);
        new FlowLogProcessor(input, new TableFileWriter(Constants.OUTPUT_PATH)).run();
    }
}
