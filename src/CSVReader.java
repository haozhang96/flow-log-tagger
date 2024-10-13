import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * This class reads a CSV (comma-separated values) file lazily into a {@link Stream} of columns as string arrays.
 * <br/><br/>
 *
 * <b>Note:</b> {@link Stream}s created by instances of this class must be used with a
 *   <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">try-with-resources
 *   statement</a> to ensure any underlying file system resources are properly closed.
 */
class CSVReader extends CSVGenerator {
    static CSVReader FLOW_LOG = new CSVReader(Constants.FLOW_LOG_PATH);
    static CSVReader LOOKUP_TABLE = new CSVReader(Constants.LOOKUP_TABLE_PATH);
    static CSVReader IANA_PROTOCOLS = new CSVReader(Constants.IANA_PROTOCOLS_PATH);

    private final Path path;
    private final boolean hasHeaders;

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    CSVReader(Path path) {
        this(path, true);
    }

    CSVReader(Path path, boolean hasHeaders) {
        this.path = path;
        this.hasHeaders = hasHeaders;
    }

    //==================================================================================================================
    // Supplier Implementation Methods
    //==================================================================================================================

    @Override
    @SuppressWarnings("resource") // The stream must be closed by the caller.
    public Stream<String[]> get() {
        try {
            return Files
                .lines(path)
                .skip(hasHeaders ? 1L : 0L) // Skip the header.
                .map(line -> line.strip().split(Constants.COMMA));
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
