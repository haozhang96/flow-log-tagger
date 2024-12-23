import java.util.Spliterators;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

class TableGeneratorTest extends BaseUnitTest {
    private static final long ROWS = ThreadLocalRandom.current().nextLong(1000L);
    private static final Supplier<?> COLUMN_GENERATOR = () -> ThreadLocalRandom.current().nextDouble();
    private static final TableGenerator TARGET = new TableGenerator(ROWS, COLUMN_GENERATOR, COLUMN_GENERATOR);

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
    void get_shouldReturnStreamWithExpectedRowCount() {
        assertRows(TARGET.get().count());
    }

    @Test
    void iterator_shouldReturnStreamWithExpectedRowCount() {
        assertRows(Utils.stream(Spliterators.spliteratorUnknownSize(TARGET.iterator(), 0)).count());
    }

    @Test
    void spliterator_shouldReturnStreamWithExpectedRowCount() {
        assertRows(Utils.stream(TARGET.spliterator()).count());
    }

    //==================================================================================================================
    // Assertion Helpers
    //==================================================================================================================

    private static void assertRows(long rows) {
        assert$(rows != 0L, "Expected at least one row");
    }
}
