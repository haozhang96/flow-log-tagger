import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Main {
    //==================================================================================================================
    // Bootstrap
    //==================================================================================================================

    public static void main(String... args) {
        final var overwrite = Settings.OVERWRITE ? StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.CREATE_NEW;
        final OpenOption[] options = {StandardOpenOption.CREATE, overwrite};

        try {
            // Run the program using randomly generated data if the first argument provided is a number.
            final var input = FlowLogGenerator.ofMebibytes(Double.parseDouble(args[0]));
            final var output = new TableFileWriter(Constants.OUTPUT_PATH, options);
            new FlowLogProcessor(input, output).run();
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException exception) {
            // Otherwise, run the program using file paths provided.
            final var input = new TableFileReader(args.length > 0 ? Path.of(args[0]) : Constants.INPUT_PATH);
            final var tags = args.length > 1 ? new Tags(new TableFileReader(Path.of(args[1]))) : Constants.TAGS;
            final var output = new TableFileWriter(args.length > 2 ? Path.of(args[2]) : Constants.OUTPUT_PATH, options);
            new FlowLogProcessor(input, tags, output).run();
        }
    }
}
