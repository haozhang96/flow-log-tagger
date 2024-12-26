import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

class TableFileWriterTest extends BaseUnitTest {
    private static final Path PATH = createTempFile();
    private static final String SEPARATOR = Constants.SEPARATORS.get("csv");
    private static final TableFileWriter TARGET = new TableFileWriter(PATH, SEPARATOR);

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
    void row_givenObjectArray_willWriteRowToFile() {
        TARGET.row(TableConsumerTest.OBJECT_ROWS[0]);
        flushRows();
        assertRows();
    }

    @Test
    void row_givenStringArray_willWriteRowToFile() {
        TARGET.row(TableConsumerTest.STRING_ROWS[0]);
        flushRows();
        assertRows();
    }

    @Test
    void row_givenStream_willWriteRowToFile() {
        TARGET.row(Stream.of(TableConsumerTest.STRING_ROWS[0]));
        flushRows();
        assertRows();
    }

    @Test
    void row_givenIterable_willWriteRowToFile() {
        TARGET.row(Arrays.asList(TableConsumerTest.STRING_ROWS[0]));
        flushRows();
        assertRows();
    }

    @Test
    void rows_givenObjectArray_willWriteRowsToFile() {
        TARGET.rows(TableConsumerTest.OBJECT_ROWS);
        flushRows();
        assertRows();
    }

    @Test
    void rows_givenStringArray_willWriteRowsToFile() {
        TARGET.rows(TableConsumerTest.STRING_ROWS);
        flushRows();
        assertRows();
    }

    @Test
    void rows_givenStream_willWriteRowsToFile() {
        TARGET.rows(Stream.of(TableConsumerTest.STRING_ROWS));
        flushRows();
        assertRows();
    }

    @Test
    void rows_givenIterable_willWriteRowsToFile() {
        TARGET.rows(Arrays.asList(TableConsumerTest.STRING_ROWS));
        flushRows();
        assertRows();
    }

    @Test
    void accept_shouldWriteRowsToFile() {
        TARGET.accept(Arrays.asList(TableConsumerTest.STRING_ROWS));
        flushRows();
        assertRows();
    }

    @Test
    void flush_shouldFlushRowsToDisk() {
        TARGET.accept(Arrays.asList(TableConsumerTest.STRING_ROWS));

        try {
            // This MAY fail because we haven't written the data to the disk; it is not deterministic.
            assertRows();
        } catch (AssertionError | ArrayIndexOutOfBoundsException error) {
            // This MUST succeed because we wrote the data to the disk.
            flushRows();
            assertRows();
        }
    }

    //==================================================================================================================
    // Assertion Helpers
    //==================================================================================================================

    private static void flushRows() {
        try {
            TARGET.flush();
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to write test data to temporary file", exception);
        }
    }

    private static void assertRows() {
        final var table = TableConsumerTest.STRING_ROWS;

        try (var lines = TableFileReader.lines(PATH)) {
            final var lines$ = lines.map(line -> line.split(SEPARATOR)).toArray(String[][]::new);
            final var rows = Arrays.copyOfRange(lines$, lines$.length - table.length, lines$.length);
            assert$(
                equals(rows, table),
                () -> "Unexpected rows: expected=%s, given=%s".formatted(toString(table), toString(rows))
            );
        }
    }

    private static Path createTempFile() {
        try {
            final var path = Files.createTempFile(null, null);
            path.toFile().deleteOnExit(); // Delete the file after the JVM terminates.
            return path;
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to create temporary file for testing", exception);
        }
    }
}
