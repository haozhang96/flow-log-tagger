import java.nio.file.Path;

class AbstractTableFileProcessorTest extends BaseUnitTest {
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
        final var path = Constants.DATA_DIRECTORY;
        final var separator = Constants.SEPARATORS.get("csv");
        final var toString = new Target(path, separator).toString();
        assert$(toString.contains(path.toString()), "toString() should contain the path: " + path);
        assert$(toString.contains(separator), "toString() should contain the separator: " + separator);
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
