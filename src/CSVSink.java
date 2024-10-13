import java.io.*;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

/**
 * This class consumes CSV (comma-separated values) data lazily from an {@link Iterable} of columns as string arrays.
 * <br/><br/>
 *
 * <b>Note:</b> Instances of this class must be used with a
 *   <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">try-with-resources
 *   statement</a> to ensure any underlying file system resources are properly closed.
 */
abstract class CSVSink implements Consumer<Iterable<String[]>>, Closeable {
    /**
     * A {@link CSVSink} that does nothing; mainly used for testing
     */
    static final CSVSink NOOP =
        new CSVSink() {
            @Override
            public void row(String... columns) {
                // Do nothing.
            }
        };

    //==================================================================================================================
    // Implementation Methods
    //==================================================================================================================

    /**
     * Append a new row with a given list of column values.
     *
     * @param columns The values for the columns to append to the new row
     */
    public abstract void row(String... columns);

    /**
     * Append a given list of rows of column values.
     *
     * @param rows The new rows of column values to append
     */
    public void rows(String[]... rows) {
        accept(Arrays.asList(rows));
    }

    //==================================================================================================================
    // Consumer Implementation Methods
    //==================================================================================================================

    @Override
    public void accept(Iterable<String[]> rows) {
        StreamSupport
            .stream(rows.spliterator(), true)
            .forEach(this::row);
    }

    //==================================================================================================================
    // AutoCloseable Implementation Methods
    //==================================================================================================================

    @Override
    public void close() throws IOException {
        // Do nothing.
    }
}
