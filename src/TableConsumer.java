import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This interface defines an object that consumes tabular data from a {@link Stream} of columns as string arrays.
 * <br/><br/>
 *
 * <b>Note:</b> Instances of this interface must be used with a
 *   <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">try-with-resources
 *   statement</a> to ensure any underlying resources are properly closed.
 */
@FunctionalInterface
interface TableConsumer extends Consumer<Stream<String[]>>, Closeable {
    /**
     * A {@link TableConsumer} that consumes no tabular data; mainly used for testing
     */
    TableConsumer NOOP = rows -> {};

    //==================================================================================================================
    // Implementation Methods
    //==================================================================================================================

    /**
     * Append a new row with a given list of column values.
     *
     * @param columns The values for the columns to append to the new row
     * @return The same {@link TableConsumer} for chaining
     */
    default TableConsumer row(String... columns) {
        return rows(columns);
    }

    /**
     * Append a new row with a given {@link Iterable} of column values.
     *
     * @param columns The values for the columns to append to the new row
     * @return The same {@link TableConsumer} for chaining
     */
    default TableConsumer row(Iterable<String> columns) {
        return switch (columns) {
            case Collection<String> collection -> rows(collection.toArray(String[]::new));
            case null -> rows(); // Append an empty row.
            default -> rows(StreamSupport.stream(columns.spliterator(), false).toArray(String[]::new));
        };
    }

    /**
     * Append a given list of rows of column values.
     *
     * @param rows The new rows of column values to append
     * @return The same {@link TableConsumer} for chaining
     */
    default TableConsumer rows(String[]... rows) {
        return rows(Arrays.asList(rows));
    }

    /**
     * Append a given {@link Iterable} of rows of column values.
     *
     * @param rows The new rows of column values to append
     * @return The same {@link TableConsumer} for chaining
     */
    default TableConsumer rows(Iterable<String[]> rows) {
        return switch (rows) {
            case Collection<String[]> collection -> { accept(collection.stream()); yield this; }
            case null -> this; // Do nothing.
            default -> { accept(StreamSupport.stream(rows.spliterator(), false)); yield this; }
        };
    }

    //==================================================================================================================
    // Consumer Implementation Methods
    //==================================================================================================================

    /**
     * Append a given {@link Stream} of rows of column values.
     *
     * @param rows The new rows of column values to append
     */
    @Override
    void accept(Stream<String[]> rows);

    //==================================================================================================================
    // AutoCloseable Implementation Methods
    //==================================================================================================================

    @Override
    default void close() throws IOException {
        // Do nothing.
    }
}
