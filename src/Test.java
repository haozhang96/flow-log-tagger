import java.io.IOException;

public class Test {
    //==================================================================================================================
    // Bootstrap
    //==================================================================================================================

    public static void main(String[] args) throws IOException {
        final var input = FlowLogGenerator.ofMebibytes(args.length > 0 ? Double.parseDouble(args[0]) : 10L);
        try (var processor = new FlowLogProcessor(input, new TableFileWriter(Constants.OUTPUT_PATH))) {
            processor.run();
        }
    }
}
