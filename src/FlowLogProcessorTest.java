import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

class FlowLogProcessorTest extends BaseUnitTest {
    private static final long ROWS = ThreadLocalRandom.current().nextLong(1000L);
    private static final AtomicLong ROW_COUNTER = new AtomicLong();
    private static final FlowLogProcessor TARGET =
        new FlowLogProcessor(new FlowLogGenerator(ROWS), FlowLogProcessorTest::assertRows);

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
    void run_shouldProcessFlowLogCorrectly() {
        assert$(ROW_COUNTER.get() == 0L, "Unexpected row count before processing: " + ROW_COUNTER);
        TARGET.run(); // Run the processor to call assertRows().
        assert$(ROW_COUNTER.get() != 0L, "Unexpected row count after processing: " + ROW_COUNTER);
    }

    //==================================================================================================================
    // Assertion Helpers
    //==================================================================================================================

    private static void assertRows(Iterable<String[]> rows) {
        assert$(
            Utils
                .stream(rows)
                .peek(row -> ROW_COUNTER.incrementAndGet())
                .flatMap(Stream::of)
                .noneMatch(String::isEmpty),
            () -> "Unexpected rows: " + rows
        );
    }
}
