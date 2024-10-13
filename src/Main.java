import java.nio.file.Path;

public class Main {
    //==================================================================================================================
    // Bootstrap
    //==================================================================================================================

    public static void main(String[] args) {
        final var flowLog = args.length > 0 ? new CSVReader(Path.of(args[0])) : CSVReader.FLOW_LOG;
        final var tags = args.length > 1 ? new Tags(new CSVReader(Path.of(args[1]))) : Tags.DEFAULT;
        final var output = args.length > 2 ? new CSVWriter(Path.of(args[2])) : CSVWriter.OUTPUT;
        new Processor(flowLog, tags, output).run();
    }
}
