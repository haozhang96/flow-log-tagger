import java.util.Collection;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class contains static utility methods used repeatedly throughout the program.
 */
interface Utils {
    /**
     * Construct a {@link Stream} from a given {@link Iterable}.
     *
     * @param iterable The {@link Iterable} to construct the {@link Stream} from; may be {@code null}
     * @param <T> The type of elements encountered by the given {@link Iterable} and the constructed {@link Stream}
     *
     * @see Collection#stream()
     * @see StreamSupport#stream(Spliterator, boolean)
     */
    static <T> Stream<T> stream(Iterable<T> iterable) {
        return switch (iterable) {
            case Collection<T> collection -> collection.stream();
            case null -> Stream.empty();
            default -> stream(iterable.spliterator());
        };
    }

    /**
     * Construct a {@link Stream} from a given {@link Spliterator}.
     *
     * @param spliterator The {@link Spliterator} to construct the {@link Stream} from
     * @param <T> The type of elements encountered by the given {@link Spliterator} and the constructed {@link Stream}
     *
     * @see StreamSupport#stream(Spliterator, boolean)
     */
    static <T> Stream<T> stream(Spliterator<T> spliterator) {
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * Construct an array containing the elements of a given {@link Iterable} using a given array constructor.
     *
     * @param iterable The {@link Iterable} of to construct the array from; may be {@code null}
     * @param arrayConstructor The array-size-accepting {@link IntFunction} to use to construct the array; typically
     *                         passed in the method/constructor reference syntax (i.e., {@code String[]::new})
     * @param <T> The type of elements encountered by the given {@link Iterable} and contained in the constructed array
     */
    static <T> T[] toArray(Iterable<T> iterable, IntFunction<T[]> arrayConstructor) {
        return switch (iterable) {
            case Collection<T> collection -> collection.toArray(arrayConstructor);
            case null -> arrayConstructor.apply(0);
            default -> stream(iterable).toArray(arrayConstructor);
        };
    }

    /**
     * Construct a {@linkplain Collector.Characteristics#CONCURRENT concurrent} version of {@link Collectors#counting()}
     *   that uses {@link AtomicLong}s to keep element counts, allowing for a concurrent reduction operation.
     */
    static <T> Collector<T, ?, Long> countingCollector() {
        return Collector.of(
            AtomicLong::new,
            (counter, element) -> counter.getAndIncrement(),
            (a, b) -> { a.getAndAdd(b.get()); return a; },
            AtomicLong::get,
            Collector.Characteristics.CONCURRENT, Collector.Characteristics.UNORDERED
        );
    }

    /**
     * Release a given list of resources by invoking their clean-up methods where applicable.
     *
     * @param resources The list of resources to release
     */
    static void releaseResources(Object... resources) {
        for (final var resource : resources) {
            if (!(resource instanceof AutoCloseable closeable)) {
                // We use this particular control flow to avoid deeply nesting the next block of statements.
                continue;
            }

            try {
                closeable.close();
            } catch (Exception exception) {
                Loggers.ERROR.accept("Failed to close resource; this will be ignored: " + resource, exception);
            }
        }
    }
}
