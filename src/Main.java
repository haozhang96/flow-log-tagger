import java.io.IOException;
import java.nio.file.Path;

public class Main {
    //==================================================================================================================
    // Bootstrap
    //==================================================================================================================

    public static void main(String[] args) throws IOException {
        final var input = new TableFileReader(args.length > 0 ? Path.of(args[0]) : Constants.INPUT_PATH);
        final var tags = args.length > 1 ? new Tags(new TableFileReader(Path.of(args[1]))) : Constants.TAGS;
        final var output = new TableFileWriter(args.length > 2 ? Path.of(args[2]) : Constants.OUTPUT_PATH);
        try (var processor = new FlowLogProcessor(input, tags, output)) {
            processor.run();
        }
    }
}
