import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This interface defines an object that consumes tabular data from a {@link Iterable} of columns as string arrays.
 */
@FunctionalInterface
interface TableConsumer extends Consumer<Iterable<String[]>> {
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
    default TableConsumer row(Object... columns) {
        return invoke(this::row, this::toString, columns);
    }

    /**
     * Append a new row with a given list of column values.
     *
     * @param columns The values for the columns to append to the new row
     * @return The same {@link TableConsumer} for chaining
     */
    default TableConsumer row(String... columns) {
        return invoke(this::row, Arrays::asList, columns);
    }

    /**
     * Append a new row with a given {@link Stream} of column values.
     *
     * @param columns The values for the columns to append to the new row
     * @return The same {@link TableConsumer} for chaining
     */
    default TableConsumer row(Stream<String> columns) {
        return invoke(this::row, Stream::toList, columns);
    }

    /**
     * Append a new row with a given {@link Iterable} of column values.
     *
     * @param columns The values for the columns to append to the new row
     * @return The same {@link TableConsumer} for chaining
     */
    default TableConsumer row(Iterable<String> columns) {
        return invoke(this::rows, Collections::singletonList, toArray(columns));
    }

    /**
     * Append a given list of rows of column values.
     *
     * @param rows The new rows of column values to append
     * @return The same {@link TableConsumer} for chaining
     */
    default TableConsumer rows(Object[]... rows) {
        return invoke(this::rows, array -> Stream.of(array).map(this::toString).map(this::toArray).toList(), rows);
    }

    /**
     * Append a given list of rows of column values.
     *
     * @param rows The new rows of column values to append
     * @return The same {@link TableConsumer} for chaining
     */
    default TableConsumer rows(String[]... rows) {
        return invoke(this::rows, Arrays::asList, rows);
    }

    /**
     * Append a given {@link Stream} of rows of column values.
     *
     * @param rows The new rows of column values to append
     * @return The same {@link TableConsumer} for chaining
     */
    default TableConsumer rows(Stream<String[]> rows) {
        return invoke(this::rows, Stream::toList, rows);
    }

    /**
     * Append a given {@link Iterable} of rows of column values.
     *
     * @param rows The new rows of column values to append
     * @return The same {@link TableConsumer} for chaining
     */
    default TableConsumer rows(Iterable<String[]> rows) {
        accept(rows);
        return this;
    }

    //==================================================================================================================
    // Consumer Implementation Methods
    //==================================================================================================================

    /**
     * Append a given {@link Iterable} of rows of column values.
     *
     * @param rows The new rows of column values to append
     */
    @Override
    void accept(Iterable<String[]> rows);

    @Override
    default TableConsumer andThen(Consumer<? super Iterable<String[]>> after) {
        return Consumer.super.andThen(after)::accept;
    }

    //==================================================================================================================
    // Private Helper Methods
    //==================================================================================================================

    /**
     * Convert a given array of {@link Object}s into a {@link Stream} of {@link String}s.
     *
     * @param array The array of {@link Object}s to convert into a {@link Stream} of {@link String}s
     */
    private Iterable<String> toString(Object[] array) {
        return Stream
            .of(array)
            .map(String::valueOf)
            .toList();
    }

    /**
     * Convert a given {@link Stream} of {@link String}s into an array of {@link String}s.
     *
     * @param iterable The {@link Stream} of {@link String}s to convert into an array of {@link String}s
     */
    private String[] toArray(Iterable<String> iterable) {
        return switch (iterable) {
            case Collection<String> collection -> collection.toArray(String[]::new);
            case null -> new String[0]; // This is not enough of an occurrence to worry about repeated allocations.
            default -> StreamSupport.stream(iterable.spliterator(), false).toArray(String[]::new);
        };
    }

    private static <T, E, R> R invoke(
        Function<? super Iterable<E>, R> method,
        Function<? super T, ? extends Iterable<E>> mapper,
        T input
    ) {
        return Optional
            .ofNullable(input)
            .map(method.compose(mapper))
            .orElseGet(() -> method.apply(List.of()));
    }
}
