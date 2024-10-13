import java.io.IOException;

public class Test {
    //==================================================================================================================
    // Bootstrap
    //==================================================================================================================

    public static void main(String[] args) throws IOException {
        final var flowLog = new FlowLogGenerator(args.length > 0 ? Long.parseLong(args[0]) : 100_000L); // Roughly 13 MB

        try (var processor = new Processor(flowLog, new CSVFileWriter(Constants.OUTPUT_PATH))) {
            processor.run();
        }
    }
}
