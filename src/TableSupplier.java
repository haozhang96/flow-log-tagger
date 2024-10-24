import java.util.Collections;
import java.util.NavigableMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This interface defines an object that supplies tabular data lazily into a {@link Stream} of columns as string arrays.
 */
@FunctionalInterface
interface TableSupplier extends Supplier<Stream<String[]>> {
    /**
     * A {@link TableConsumer} that generates no tabular data; mainly used for testing
     */
    TableSupplier NOOP = Stream::empty;

    //==================================================================================================================
    // Implementation Methods
    //==================================================================================================================

    /**
     * Project the data of this {@link TableSupplier} onto a {@link Table} using its given
     *   {@link TableSupplier}-accepting constructor.
     *
     * @param tableFactory The {@link TableSupplier}-accepting constructor of the {@link Table} to project this
     *                     {@link TableSupplier}'s data onto
     * @param <K> The type of keys mapped by the constructed {@link Table}
     * @param <V> The type of values mapped by the constructed {@link Table}
     * @return An unmodifiable {@link NavigableMap} view of the constructed {@link Table} with the data of this
     *         {@link TableSupplier} projected onto it
     */
    default <K extends Comparable<? super K>, V> NavigableMap<K, V> project(
        Function<? super TableSupplier, ? extends Table<K, V>> tableFactory
    ) {
        return Collections.unmodifiableNavigableMap(tableFactory.apply(this));
    }

    //==================================================================================================================
    // Supplier Implementation Methods
    //==================================================================================================================

    /**
     * Retrieve a {@link Stream} of the tabular data as string column arrays.
     */
    @Override
    Stream<String[]> get();
}
