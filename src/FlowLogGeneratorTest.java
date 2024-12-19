import java.util.concurrent.ThreadLocalRandom;

class FlowLogGeneratorTest extends BaseUnitTest {
    private static final long ROWS = ThreadLocalRandom.current().nextLong(1000L);
    private static final long COLUMNS = 14L; // The constant number of columns defined in FlowLogGenerator
    private static final FlowLogGenerator TARGET = new FlowLogGenerator(ROWS);

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
    void get_shouldReturnStreamWithExpectedRowAndColumnCount() {
        assert$(
            TARGET.get().allMatch(columns -> columns.length == COLUMNS),
            "Every row must contain %d columns.".formatted(COLUMNS)
        );
    }
}
