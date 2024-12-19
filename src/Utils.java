import java.util.Collection;
import java.util.Spliterator;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class contains static utility methods used repeatedly throughout the program.
 */
interface Utils {
    /**
     * Construct a {@link Stream} from a given {@link Iterable}.
     *
     * @param iterable The {@link Iterable} to construct the {@link Stream} from
     * @param <T> The type of elements encountered by the given {@link Iterable} and the constructed {@link Stream}
     *
     * @see Collection#stream()
     * @see StreamSupport#stream(Spliterator, boolean)
     */
    static <T> Stream<T> stream(Iterable<T> iterable) {
        return iterable instanceof Collection<T> collection ? collection.stream() : stream(iterable.spliterator());
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
     * @param iterable The {@link Iterable} of to construct the array from
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
}
