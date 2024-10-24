import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This class reads a tabular data file lazily into a {@link Stream} of columns as string arrays.
 * <br/><br/>
 *
 * <b>Note:</b> {@link Stream}s created by instances of this class must be used with a
 *   <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">try-with-resources
 *   statement</a> to ensure any underlying file system resources are properly closed.
 */
class TableFileReader implements TableSupplier {
    private final Path path;
    private final String separator;
    private final boolean hasHeaders;

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    TableFileReader(Path path) {
        this(path, true);
    }

    TableFileReader(Path path, boolean hasHeaders) {
        this(path, null, hasHeaders);
    }

    TableFileReader(Path path, String separator, boolean hasHeaders) {
        this.path = path;
        this.separator = Pattern.quote(Objects.requireNonNullElseGet(separator, () -> inferSeparator(path)));
        this.hasHeaders = hasHeaders;
    }

    //==================================================================================================================
    // Supplier Implementation Methods
    //==================================================================================================================

    @Override
    @SuppressWarnings("resource") // The stream must be closed by the caller.
    public Stream<String[]> get() {
        try {
            System.out.println("Reading file: " + path);
            return Files
                .lines(path)
                .skip(hasHeaders ? 1L : 0L) // Skip the header.
                .map(line -> line.split(separator));
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to read table from file: " + path, exception);
        }
    }

    //==================================================================================================================
    // Package-private Helper Methods
    //==================================================================================================================

    static String inferSeparator(Path path) {
        return Optional
            .ofNullable(path.getFileName())
            .map(Path::toString)
            .filter(fileName -> fileName.contains("."))
            .map(fileName -> fileName.substring(fileName.lastIndexOf('.') + 1))
            .map(String::toLowerCase)
            .map(Constants.SEPARATORS::get)
            .orElseGet(() -> Constants.SEPARATORS.get(Constants.UNKNOWN));
    }
}
