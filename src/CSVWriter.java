import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * This class writes a CSV (comma-separated values) file lazily from an {@link Iterable} of columns as string arrays.
 */
class CSVWriter implements Consumer<Iterable<String[]>> {
    static CSVWriter OUTPUT = new CSVWriter(Constants.OUTPUT_PATH);

    private static final String[] EMPTY_ROW = {};

    private final Path path;
    private final OpenOption[] options;

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    CSVWriter(Path path) {
        this(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
    }

    CSVWriter(Path path, OpenOption... options) {
        this.path = path;
        this.options = options;
    }

    //==================================================================================================================
    // Implementation Methods
    //==================================================================================================================

    /**
     * Append
     *
     * @param rows
     */
    public void rows(String[]... rows) {
        accept(Arrays.asList(rows));
    }

    /**
     * Append a new row with a given list of column values.
     *
     * @param columns The values for the columns to append to the new row
     */
    public void columns(String... columns) {
        rows(columns);
    }

    /**
     * Append a new line to the underlying CSV file.
     */
    public void newline() {
        rows(EMPTY_ROW);
    }

    //==================================================================================================================
    // Consumer Implementation Methods
    //==================================================================================================================

    @Override
    public void accept(Iterable<String[]> rows) {
        try {
            Files.write(path, new CSVIterable(rows.iterator()), options);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to write CSV to " + path, exception);
        }
    }

    //==================================================================================================================
    // Iterable Implementation
    //==================================================================================================================

    private record CSVIterable(Iterator<String[]> rows) implements Iterable<String> {
        @Override
        public Iterator<String> iterator() {
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return rows().hasNext();
                }

                @Override
                public String next() {
                    return String.join(Constants.COMMA, rows().next());
                }
            };
        }
    }
}
