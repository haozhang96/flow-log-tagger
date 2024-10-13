public class Test {
    //==================================================================================================================
    // Bootstrap
    //==================================================================================================================

    public static void main(String[] args) {
        final var flowLog = new FlowLogGenerator(args.length > 0 ? Long.parseLong(args[0]) : 100_000L); // Roughly 10 MB
        new Processor(flowLog, Tags.DEFAULT, CSVWriter.OUTPUT).run();
    }
}
