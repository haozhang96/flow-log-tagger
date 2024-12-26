import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * This interface defines an object that consumes tabular data from a {@link Iterable} of columns as string arrays.
 *
 * @implNote This interface originally consumed {@link Stream}s, but the overhead of creating {@link Stream}s for its
 *           use was far too high. Additionally, consuming {@link Iterable}s instead of {@link Stream}s has the
 *           advantage of accepting more common types such as {@link Collection}s.
 */
@FunctionalInterface
interface TableConsumer extends Consumer<Iterable<String[]>> {
    /**
     * A {@link TableConsumer} that consumes no tabular data; mainly used for testing
     */
    TableConsumer NOOP = rows -> { };

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
        return apply(this::row, this::toString, columns);
    }

    /**
     * Append a new row with a given list of column values.
     *
     * @param columns The values for the columns to append to the new row
     * @return The same {@link TableConsumer} for chaining
     */
    default TableConsumer row(String... columns) {
        return apply(this::row, Arrays::asList, columns);
    }

    /**
     * Append a new row with a given {@link Stream} of column values.
     *
     * @param columns The values for the columns to append to the new row
     * @return The same {@link TableConsumer} for chaining
     */
    default TableConsumer row(Stream<String> columns) {
        return apply(this::row, Stream::toList, columns);
    }

    /**
     * Append a new row with a given {@link Iterable} of column values.
     *
     * @param columns The values for the columns to append to the new row
     * @return The same {@link TableConsumer} for chaining
     */
    default TableConsumer row(Iterable<String> columns) {
        return apply(this::rows, Collections::singletonList, toArray(columns));
    }

    /**
     * Append a given list of rows of column values.
     *
     * @param rows The new rows of column values to append
     * @return The same {@link TableConsumer} for chaining
     */
    default TableConsumer rows(Object[]... rows) {
        return apply(this::rows, array -> Stream.of(array).map(this::toString).map(this::toArray).toList(), rows);
    }

    /**
     * Append a given list of rows of column values.
     *
     * @param rows The new rows of column values to append
     * @return The same {@link TableConsumer} for chaining
     */
    default TableConsumer rows(String[]... rows) {
        return apply(this::rows, Arrays::asList, rows);
    }

    /**
     * Append a given {@link Stream} of rows of column values.
     *
     * @param rows The new rows of column values to append
     * @return The same {@link TableConsumer} for chaining
     */
    default TableConsumer rows(Stream<String[]> rows) {
        return apply(this::rows, Stream::toList, rows);
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
        // We could use a simple for-loop to avoid stream creations, but this method isn't invoked enough to make doing
        //   so at the expense of readability worthwhile.
        return Stream
            .of(array)
            .map(String::valueOf)
            .toList();
    }

    /**
     * Convert a given {@link Iterable} of {@link String}s into an array of {@link String}s.
     *
     * @param iterable The {@link Iterable} of {@link String}s to convert into an array of {@link String}s
     */
    private String[] toArray(Iterable<String> iterable) {
        return Utils.toArray(iterable, String[]::new);
    }

    /**
     * Invoke a given {@link Iterable}-accepting method after applying a given {@link T}-accepting mapper to a given
     *   {@link T} input.
     *
     * @param method The method to invoke after applying the given mapper to the given input; typically passed in the
     *               method/constructor reference syntax (i.e., {@code this::rows})
     * @param mapper The mapper to apply to the given value before invoking the given method
     * @param input The input to map using the given mapper and invoke using the given method
     * @param <T> The type of input for the given mapper
     * @param <E> The element type of the {@link Iterable} accepted by the given method
     * @param <R> The type of value returned by the given method
     * @return A value returned by the given method after applying the given mapper to the given input
     */
    private static <T, E, R> R apply(
        Function<? super Iterable<E>, ? extends R> method,
        Function<? super T, ? extends Iterable<E>> mapper,
        T input
    ) {
        return Optional
            .ofNullable(input)
            .map(mapper)
            .<R>map(method)
            .orElseGet(() -> method.apply(List.of()));
    }
}
