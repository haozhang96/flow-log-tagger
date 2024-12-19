import java.util.Spliterators;

class TableFileReaderTest extends BaseUnitTest {
    private static final TableFileReader TARGET = new TableFileReader(Constants.INPUT_PATH);

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
