import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.ThreadLocalRandom;
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
}
