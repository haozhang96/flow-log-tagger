import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Arrays;
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
     * Construct an instance of {@link TableFileWriter} that writes to a given {@link Path} using a given list of
     *   {@link OpenOption}(s) to pass to {@link Files#newBufferedWriter(Path, OpenOption...)}.
     *
     * @param path The {@link Path} that the constructed {@link TableFileWriter} will write to
     * @param options The list of {@link OpenOption}(s) to pass to {@link Files#newBufferedWriter(Path, OpenOption...)}
     *
     * @apiNote The column separator will be inferred based on {@code path}'s file characteristics.
     */
    TableFileWriter(Path path, OpenOption... options) {
        this(path, null, options);
    }

    /**
     * Construct an instance of {@link TableFileWriter} that writes to a given {@link Path}, using a given column
     *   separator and list of {@link OpenOption}(s) to pass to {@link Files#newBufferedWriter(Path, OpenOption...)}.
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
        for (final var columns : rows) {
            // Write the row and line separator in a single call to ensure that the line separator immediately follows.
            try {
                writer.write(String.join(separator, columns) + System.lineSeparator());
            } catch (IOException exception) {
                throw new UncheckedIOException(
                    "Failed to write to [%s]: %s".formatted(path, Arrays.toString(columns)),
                    exception
                );
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
        try {
            writer.close();
        } finally {
            // We wouldn't know for sure whether anything was written at this point, but this assumption is most likely.
            Loggers.INFO.accept("[<] Wrote file: " + path);
        }
    }
}
