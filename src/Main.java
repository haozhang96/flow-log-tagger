import java.io.IOException;
import java.nio.file.Path;

public class Main {
    //==================================================================================================================
    // Bootstrap
    //==================================================================================================================

    public static void main(String[] args) throws IOException {
        final var flowLog = new CSVFileReader(args.length > 0 ? Path.of(args[0]) : Constants.FLOW_LOG_PATH);
        final var tags = args.length > 1 ? new Tags(new CSVFileReader(Path.of(args[1]))) : Tags.DEFAULT;
        final var output = new CSVFileWriter(args.length > 2 ? Path.of(args[2]) : Constants.OUTPUT_PATH);

        try (var processor = new Processor(flowLog, tags, output)) {
            processor.run();
        }
    }
}
