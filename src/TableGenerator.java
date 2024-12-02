import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This class generates tabular data lazily into a {@link Stream} of columns as string arrays.
 */
class TableGenerator implements TableSupplier {
    private final long rows;
    private final Supplier<?>[] columnGenerators;

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    TableGenerator(Supplier<?>... columnGenerators) {
        this(Long.MAX_VALUE, columnGenerators);
    }

    TableGenerator(long rows, Supplier<?>... columnGenerators) {
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
            .map(columnGenerators -> columnGenerators.map(Supplier::get).map(String::valueOf).toArray(String[]::new));
    }
}
