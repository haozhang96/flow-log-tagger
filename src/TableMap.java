import java.io.Serial;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * This class is an implementation of {@link ConcurrentMap} that uses a {@link TableSupplier} to build its mappings.
 *
 * @param <K> {@inheritDoc}
 * @param <V> {@inheritDoc}
 */
class TableMap<K, V> extends ConcurrentHashMap<K, V> implements TableConsumer {
    private static final @Serial long serialVersionUID = 1L;

    private final UnaryOperator<Stream<String[]>> mapper;
    private final Collector<? super String[], ?, ? extends Map<? extends K, ? extends V>> collector;

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    TableMap(Collector<? super String[], ?, ? extends Map<? extends K, ? extends V>> collector) {
        this(TableSupplier.NOOP, collector);
    }

    TableMap(
        UnaryOperator<Stream<String[]>> mapper,
        Collector<? super String[], ?, ? extends Map<? extends K, ? extends V>> collector
    ) {
        this(TableSupplier.NOOP, mapper, collector);
    }

    TableMap(TableSupplier data, Collector<? super String[], ?, ? extends Map<? extends K, ? extends V>> collector) {
        this(data, UnaryOperator.identity(), collector);
    }

    TableMap(
        TableSupplier data,
        UnaryOperator<Stream<String[]>> mapper,
        Collector<? super String[], ?, ? extends Map<? extends K, ? extends V>> collector
    ) {
        this.mapper = Objects.requireNonNull(mapper);
        this.collector = Objects.requireNonNull(collector);

        try (var rows = data.get()) {
            putAll(rows);
        }
    }

    //==================================================================================================================
    // Consumer Implementation Methods
    //==================================================================================================================

    @Override
    public void accept(Iterable<String[]> rows) {
        putAll(Utils.stream(rows));
    }

    //==================================================================================================================
    // Private Helper Methods
    //==================================================================================================================

    /**
     * @see #putAll(Map)
     */
    private void putAll(Stream<String[]> rows) {
        putAll(mapper.apply(rows).collect(collector));
    }
}
