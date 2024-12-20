import java.nio.file.Path;

class AbstractTableFileProcessorTest extends BaseUnitTest {
    private static final Path PATH = Constants.DATA_DIRECTORY;
    private static final String SEPARATOR = Constants.SEPARATORS.get("csv");
    private static final AbstractTableFileProcessor TARGET = new Target(PATH, SEPARATOR);

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
    void toString_shouldContainPathAndSeparator() {
        final var toString = TARGET.toString();
        assert$(toString.contains(PATH.toString()), "toString() should contain the path: " + PATH);
        assert$(toString.contains(SEPARATOR), "toString() should contain the separator: " + SEPARATOR);
    }

    //==================================================================================================================
    // Assertion Helpers
    //==================================================================================================================

    static final class Target extends AbstractTableFileProcessor {
        Target(Path path, String separator) {
            super(path, separator);
        }
    }
}
