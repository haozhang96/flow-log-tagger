import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class TableMapTest extends BaseUnitTest {
    private static final long ROWS = ThreadLocalRandom.current().nextLong(1000L);
    private static final Supplier<?> COLUMN_GENERATOR = () -> ThreadLocalRandom.current().nextDouble();
    private static final TableMap<String, String> TARGET =
        new TableMap<>(
            new TableGenerator(ROWS, COLUMN_GENERATOR, COLUMN_GENERATOR), // Every key (column 0) should be unique
            Collectors.toMap(columns -> columns[0], columns -> columns[1])
        );

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
    void shouldBuildMappingsCorrectly() {
        assert$(TARGET.size() == ROWS, "Unexpected mapping count: expected=%d, given=%d".formatted(ROWS, TARGET.size()));
        TARGET.row(COLUMN_GENERATOR.get(), COLUMN_GENERATOR.get()); // Add a new unique mapping.
        assert$(TARGET.size() == ROWS + 1L, "Unexpected mapping count: expected=%d, given=%d".formatted(ROWS + 1L, TARGET.size()));
    }
}
