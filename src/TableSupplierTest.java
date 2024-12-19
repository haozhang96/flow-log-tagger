import java.util.Spliterators;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

class TableSupplierTest extends BaseUnitTest {
    private static final long ROWS = ThreadLocalRandom.current().nextLong(1000L);
    private static final TableSupplier TARGET = TableSupplierTest::generateRows;

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
    void noop_shouldReturnStreamWithNoRows() {
        assert$(TableSupplier.NOOP.get().findAny().isEmpty(), "Expected no rows");
    }

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
        assert$(rows == ROWS, "Unexpected row count: expected=%d, given=%d".formatted(ROWS, rows));
    }

    private static Stream<String[]> generateRows() {
        return Stream
            .generate(TableSupplierTest::generateRow)
            .limit(ROWS);
    }

    private static String[] generateRow() {
        final var random = ThreadLocalRandom.current();
        return new String[] {
            String.valueOf(random.nextLong()),
            String.valueOf(random.nextDouble()),
            String.valueOf(random.nextBoolean())
        };
    }
}
