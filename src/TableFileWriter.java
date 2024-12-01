import java.io.BufferedWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This class writes a tabular data file lazily from a {@link Stream} of columns as string arrays.
 * <br/><br/>
 *
 * <b>Note:</b> Instances of this class must be used with a
 *   <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">try-with-resources
 *   statement</a> to ensure any underlying resources are properly closed.
 */
class TableFileWriter implements TableConsumer, Flushable {
    private final Path path;
    private final BufferedWriter writer;
    private final String separator;

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    TableFileWriter(Path path) {
        this(path, null, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
    }

    TableFileWriter(Path path, String separator, OpenOption... options) {
        try {
            this.path = path;
            this.separator = Objects.requireNonNullElseGet(separator, () -> TableFileReader.inferSeparator(path));
            writer = Files.newBufferedWriter(path, options);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to open file for writing: " + path, exception);
        }
    }

    //==================================================================================================================
    // Consumer Implementation Methods
    //==================================================================================================================

    @Override
    public void accept(Stream<String[]> rows) {
        rows.forEachOrdered(columns -> {
            try {
                writer.write(String.join(separator, columns) + System.lineSeparator());
            } catch (IOException exception) {
                throw new UncheckedIOException("Failed to write to file: " + path, exception);
            }
        });
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
        try {
            writer.close();
        } finally {
            System.out.println("Wrote file: " + path);
        }
    }
}
