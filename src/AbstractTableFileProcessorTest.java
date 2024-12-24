import java.nio.file.Path;

class AbstractTableFileProcessorTest extends BaseUnitTest {
    private static final String PATH = Constants.IANA_PROTOCOLS_PATH.toString();
    private static final String SEPARATOR = Constants.SEPARATORS.get(PATH.substring(PATH.lastIndexOf('.') + 1));
    private static final AbstractTableFileProcessor TARGET = new Target(Path.of(PATH), SEPARATOR);

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
    void inferSeparator_shouldInferColumnSeparatorCorrectly() {
        assert$(
            TARGET.separator.equals(SEPARATOR),
            "Unexpected separator: expected=%s, given=%s".formatted(SEPARATOR, TARGET.separator)
        );
    }

    @Test
    void toString_shouldContainPathAndSeparator() {
        final var toString = TARGET.toString();
        assert$(toString.contains(PATH), "toString() should contain the path: " + PATH);
        assert$(toString.contains(SEPARATOR), "toString() should contain the separator: " + SEPARATOR);
    }

    //==================================================================================================================
    // Assertion Helpers
    //==================================================================================================================

    static final class Target extends AbstractTableFileProcessor {
        private Target(Path path, String separator) {
            super(path, separator);
        }
    }
}
