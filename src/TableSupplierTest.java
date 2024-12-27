import java.util.Spliterators;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Stream;

class TableSupplierTest extends BaseUnitTest {
    private static final long ROWS = ThreadLocalRandom.current().nextLong(1000L);
    private static final AtomicLong CLOSE_COUNT = new AtomicLong();
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
    @SuppressWarnings("resource") // We are testing the iteration-based self-closing behavior.
    void iterator_shouldNotCloseUnderlyingStreamAfterIncompleteIteration() {
        final var startCount = CLOSE_COUNT.get();
        TARGET
            .iterator()
            .next();
        assert$(CLOSE_COUNT.get() == startCount, "Expected the underlying stream to remain open");
    }

    @Test
    @SuppressWarnings("resource") // We are testing the iteration-based self-closing behavior.
    void iterator_shouldCloseUnderlyingStreamAfterCompleteIteration() {
        final var startCount = CLOSE_COUNT.get();
        TARGET
            .iterator()
            .forEachRemaining(TableSupplierTest::assertRow);
        assert$(CLOSE_COUNT.get() > startCount, "Expected the underlying stream to be closed");
    }

    @Test
    @SuppressWarnings({"resource", "removal"}) // We are testing the garbage-collection-based self-closing behavior.
    void iterator_shouldCloseUnderlyingStreamAfterGarbageCollection() {
        final var startCount = CLOSE_COUNT.get();
        TARGET.iterator(); // Create a stray iterator but never iterate over or close it.
        System.runFinalization(); // Hint to the JVM to run the stray iterator's finalizer.
        System.gc(); // Hint to the JVM to garbage collect the stray iterator.
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1L)); // Allow the stray iterator's cleaner to run.
        assert$(CLOSE_COUNT.get() > startCount, "Expected the underlying stream to be closed");
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

    private static void assertRow(String[] columns) {
        final var columnCount = generateRow().length;
        assert$(
            columns.length == columnCount,
            "Unexpected column count: expected=%d, given=%d".formatted(columnCount, columns.length)
        );
    }

    private static Stream<String[]> generateRows() {
        return Stream
            .generate(TableSupplierTest::generateRow)
            .limit(ROWS)
            .onClose(CLOSE_COUNT::getAndIncrement);
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
