import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ThreadLocalRandom;

class MainTest extends BaseUnitTest {
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
    void main_givenNumberAsFirstArgument_willRunProgramUsingRandomlyGeneratedData() throws IOException {
        final var mib = ThreadLocalRandom.current().nextDouble(Constants.MEBIBYTE_SCALE) / Constants.MEBIBYTE_SCALE;
        final var rows = Math.round(mib * Constants.MEBIBYTE_SCALE) / Constants.FLOW_LOG_RECORD_SIZE;

        Files.deleteIfExists(Constants.DEBUG_PATH);
        Main.main(String.valueOf(mib));

        try (var lines = Files.lines(Constants.DEBUG_PATH)) {
            final var lineCount = lines.count();
            assert$(lineCount == rows, "Unexpected row count: expected=%d, given=%d".formatted(rows, lineCount));
        }
    }

    @Test
    void main_givenPathAsFirstArgument_willRunProgramUsingGivenPath() throws IOException {
        Files.deleteIfExists(Constants.OUTPUT_PATH);
        Main.main(Constants.INPUT_PATH.toString());
        assert$(Files.exists(Constants.OUTPUT_PATH), "Expected output file to exist: " + Constants.OUTPUT_PATH);
    }
}
