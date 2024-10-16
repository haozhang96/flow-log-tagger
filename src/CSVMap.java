import java.io.Serial;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * This class is an abstract implementation of {@link ConcurrentHashMap} that uses a {@link CSVSupplier} to build its
 *   mappings.
 *
 * @param <K> {@inheritDoc}
 * @param <V> {@inheritDoc}
 */
abstract class CSVMap<K, V> extends ConcurrentHashMap<K, V> {
    private static final @Serial long serialVersionUID = 1L;

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    CSVMap(CSVSupplier csv, Collector<String[], ?, Map<K, V>> collector) {
        this(csv, UnaryOperator.identity(), collector);
    }

    CSVMap(CSVSupplier csv, UnaryOperator<Stream<String[]>> mapper, Collector<String[], ?, Map<K, V>> collector) {
        try (var rows = csv.get()) {
            putAll(mapper.apply(rows).collect(collector));
        }
    }
}
