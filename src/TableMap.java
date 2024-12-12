import java.io.Serial;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class is an abstract implementation of {@link ConcurrentMap} that uses a {@link TableSupplier} to build its
 *   mappings.
 *
 * @param <K> {@inheritDoc}
 * @param <V> {@inheritDoc}
 */
abstract class TableMap<K, V> extends ConcurrentHashMap<K, V> implements TableConsumer {
    private static final @Serial long serialVersionUID = 1L;

    private final UnaryOperator<Stream<String[]>> mapper;
    private final Collector<String[], ?, Map<K, V>> collector;

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    TableMap(Collector<String[], ?, Map<K, V>> collector) {
        this(TableSupplier.NOOP, collector);
    }

    TableMap(UnaryOperator<Stream<String[]>> mapper, Collector<String[], ?, Map<K, V>> collector) {
        this(TableSupplier.NOOP, mapper, collector);
    }

    TableMap(TableSupplier data, Collector<String[], ?, Map<K, V>> collector) {
        this(data, UnaryOperator.identity(), collector);
    }

    TableMap(TableSupplier data, UnaryOperator<Stream<String[]>> mapper, Collector<String[], ?, Map<K, V>> collector) {
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
        switch (rows) {
            case Collection<String[]> collection -> putAll(collection.stream());
            case null -> {} // Do nothing.
            default -> putAll(StreamSupport.stream(rows.spliterator(), false));
        }
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
