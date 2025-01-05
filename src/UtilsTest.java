import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

class UtilsTest extends BaseUnitTest {
    private static final Collection<Double> COLLECTION = List.of(ThreadLocalRandom.current().nextDouble());
    private static final Iterable<Double> ITERABLE = COLLECTION::iterator;
    private static final Spliterator<Double> SPLITERATOR = COLLECTION.spliterator();
    private static final IntFunction<Double[]> ARRAY_CONSTRUCTOR = Double[]::new;

    //==================================================================================================================
    // Bootstrap
    //==================================================================================================================

    public static void main(String[] args) {
        run();
    }

    //==================================================================================================================
    // Tests
    //==================================================================================================================

    @Test
    void stream_givenNull_willReturnEmptyStream() {
        assert$(Utils.stream((Iterable<?>) null).findAny().isEmpty(), "Stream should be empty");
    }

    @Test
    void stream_givenCollection_willReturnStreamContainingElements() {
        assert$(Utils.stream(COLLECTION).toList().equals(COLLECTION), "Stream contains unexpected content");
    }

    @Test
    void stream_givenIterable_willReturnStreamContainingElements() {
        assert$(Utils.stream(ITERABLE).toList().equals(COLLECTION), "Stream contains unexpected content");
    }

    @Test
    void stream_givenSpliterator_willReturnStreamContainingElements() {
        assert$(Utils.stream(SPLITERATOR).toList().equals(COLLECTION), "Stream contains unexpected content");
    }

    @Test
    void toArray_givenNull_willReturnEmptyArray() {
        assert$(Utils.toArray(null, ARRAY_CONSTRUCTOR).length == 0, "Array should be empty");
    }

    @Test
    void toArray_givenCollection_willReturnArrayContainingElements() {
        assert$(Arrays.asList(Utils.toArray(COLLECTION, ARRAY_CONSTRUCTOR)).equals(COLLECTION), "Array contains unexpected content");
    }

    @Test
    void toArray_givenIterable_willReturnStreamContainingElements() {
        assert$(Arrays.asList(Utils.toArray(ITERABLE, ARRAY_CONSTRUCTOR)).equals(COLLECTION), "Array contains unexpected content");
    }

    @Test
    void countingCollector_shouldCountStreamElementsCorrectly() {
        final var count = ThreadLocalRandom.current().nextInt(1000);
        final var collectedCount =
            Collections
                .nCopies(count, COLLECTION.iterator().next())
                .parallelStream() // Ensure correctness under concurrency.
                .collect(Utils.countingCollector());
        assert$(collectedCount == count, "Unexpected count: expected=%d, given=%d".formatted(count, collectedCount));
    }

    @Test
    void releaseResources_shouldCloseAllGivenResources() {
        final var count = ThreadLocalRandom.current().nextInt(1000);
        final var closeCount = new AtomicInteger();
        final AutoCloseable resource = closeCount::getAndIncrement;
        Utils.releaseResources(Collections.nCopies(count, resource).toArray());
        assert$(closeCount.get() == count, "Resource should have been closed");
    }
}
