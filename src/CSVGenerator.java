import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This class generates CSV (comma-separated values) data lazily into a {@link Stream} of columns as string arrays.
 */
class CSVGenerator implements CSVSupplier {
    private final long rows;
    private final Supplier<String>[] columnGenerators;

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    @SafeVarargs
    CSVGenerator(Supplier<String>... columnGenerators) {
        this(Long.MAX_VALUE, columnGenerators);
    }

    @SafeVarargs
    CSVGenerator(long rows, Supplier<String>... columnGenerators) {
        this.rows = rows;
        this.columnGenerators = columnGenerators;
    }

    //==================================================================================================================
    // Supplier Implementation Methods
    //==================================================================================================================

    @Override
    public Stream<String[]> get() {
        return Stream
            .generate(() -> columnGenerators)
            .limit(rows)
            .map(Stream::of)
            .map(columnGenerators -> columnGenerators.map(Supplier::get).toArray(String[]::new));
    }
}
