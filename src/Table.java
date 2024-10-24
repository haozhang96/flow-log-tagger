import java.io.Serial;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * This class is an abstract implementation of {@link ConcurrentNavigableMap} that uses a {@link TableSupplier} to build
 *   its mappings.
 *
 * @param <K> {@inheritDoc}
 * @param <V> {@inheritDoc}
 */
abstract class Table<K extends Comparable<? super K>, V> extends ConcurrentSkipListMap<K, V> {
    private static final @Serial long serialVersionUID = 1L;

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    Table(TableSupplier data, Collector<String[], ?, Map<K, V>> collector) {
        this(data, UnaryOperator.identity(), collector);
    }

    Table(TableSupplier data, UnaryOperator<Stream<String[]>> mapper, Collector<String[], ?, Map<K, V>> collector) {
        try (var rows = data.get()) {
            putAll(mapper.apply(rows).collect(collector));
        }
    }
}
