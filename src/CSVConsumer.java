import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

/**
 * This interface defines an object that consumes CSV (comma-separated values) data from a {@link Iterable} of columns
 *   as string arrays.
 * <br/><br/>
 *
 * <b>Note:</b> Instances of this interface must be used with a
 *   <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">try-with-resources
 *   statement</a> to ensure any underlying file system resources are properly closed.
 */
@FunctionalInterface
interface CSVConsumer extends Consumer<Iterable<String[]>>, Closeable {
    /**
     * A {@link CSVConsumer} that consumes no CSV data; mainly used for testing
     */
    CSVConsumer NOOP = columns -> { };

    //==================================================================================================================
    // Implementation Methods
    //==================================================================================================================

    /**
     * Append a new row with a given list of column values.
     *
     * @param columns The values for the columns to append to the new row
     */
    default void row(String... columns) {
        rows(columns);
    }

    /**
     * Append a new row with a given {@link Iterable} of column values.
     *
     * @param columns The values for the columns to append to the new row
     */
    default void row(Iterable<String> columns) {
        if (columns instanceof Collection<String> collection) {
            rows(collection.toArray(String[]::new));
        } else {
            rows(StreamSupport.stream(columns.spliterator(), false).toArray(String[]::new));
        }
    }

    /**
     * Append a given list of rows of column values.
     *
     * @param rows The new rows of column values to append
     */
    default void rows(String[]... rows) {
        rows(Arrays.asList(rows));
    }

    /**
     * Append a given {@link Iterable} of rows of column values.
     *
     * @param rows The new rows of column values to append
     */
    default void rows(Iterable<String[]> rows) {
        accept(rows);
    }

    //==================================================================================================================
    // Consumer Implementation Methods
    //==================================================================================================================

    /**
     * @see #rows(Iterable)
     */
    @Override
    void accept(Iterable<String[]> rows);

    //==================================================================================================================
    // AutoCloseable Implementation Methods
    //==================================================================================================================

    @Override
    default void close() throws IOException {
        // Do nothing.
    }
}
