import java.io.BufferedWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * This class writes a CSV (comma-separated values) file lazily from an {@link Iterable} of columns as string arrays.
 * <br/><br/>
 *
 * <b>Note:</b> Instances of this class must be used with a
 *   <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">try-with-resources
 *   statement</a> to ensure any underlying file system resources are properly closed.
 */
class CSVFileWriter implements CSVConsumer, Flushable {
    private final Path path;
    private final BufferedWriter writer;

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    CSVFileWriter(Path path) {
        this(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
    }

    CSVFileWriter(Path path, OpenOption... options) {
        try {
            this.path = path;
            writer = Files.newBufferedWriter(path, options);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to open CSV for writing to " + path, exception);
        }
    }

    //==================================================================================================================
    // CSVConsumer Implementation Methods
    //==================================================================================================================

    @Override
    public void accept(Iterable<String[]> rows) {
        for (final var columns : rows) {
            try {
                writer.write(String.join(Constants.COMMA, columns) + System.lineSeparator());
            } catch (IOException exception) {
                throw new UncheckedIOException("Failed to write CSV to " + path, exception);
            }
        }
    }

    //==================================================================================================================
    // Flushable Implementation Methods
    //==================================================================================================================

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    //==================================================================================================================
    // AutoCloseable Implementation Methods
    //==================================================================================================================

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
