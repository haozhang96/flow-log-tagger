import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

/**
 * This class writes a tabular data file from a {@link Stream} of columns as string arrays.
 *
 * @apiNote Instances of this class must be used with a
 *          <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">try-with-resources
 *          statement</a> to ensure any underlying resources are properly closed.
 */
non-sealed class TableFileWriter extends AbstractTableFileProcessor implements TableConsumer, Flushable, Closeable {
    private final BufferedWriter writer;

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    /**
     * Construct an instance of {@link TableFileWriter} that writes to a given {@link Path}, inferring the column
     *   separator and creating and/or appending to the end of the file without truncating it first.
     *
     * @param path The {@link Path} that the constructed {@link TableFileWriter} will write to
     */
    TableFileWriter(Path path) {
        this(path, null, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /**
     * Construct an instance of {@link TableFileWriter} that writes to a given {@link Path}, using a given column
     *   separator and {@link OpenOption}s to pass to {@link Files#newBufferedWriter(Path, OpenOption...)}.
     *
     * @param path The {@link Path} that the constructed {@link TableFileWriter} will write to
     * @param separator The column separator to use for joining the columns into lines; may be {@code null} to allow
     *                  {@link TableFileWriter} to infer this information based on {@code path}'s file characteristics
     * @param options The list of {@link OpenOption}(s) to pass to {@link Files#newBufferedWriter(Path, OpenOption...)}
     */
    TableFileWriter(Path path, String separator, OpenOption... options) {
        super(path, separator);

        try {
            writer = Files.newBufferedWriter(path, options);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to open file for writing: " + path, exception);
        }
    }

    //==================================================================================================================
    // Consumer Implementation Methods
    //==================================================================================================================

    @Override
    public void accept(Iterable<String[]> rows) {
        rows.forEach(columns -> {
            // Write the row and line separator in a single call to ensure that the line separator immediately follows.
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
            System.out.println("[<] Wrote file: " + path);
        }
    }
}
