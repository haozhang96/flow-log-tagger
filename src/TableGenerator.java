import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This class generates tabular data lazily into a {@link Stream} of columns as string arrays.
 */
class TableGenerator implements TableSupplier {
    private static final Supplier<?>[] NO_COLUMN_GENERATORS = {};

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
        this.columnGenerators = Objects.requireNonNullElse(columnGenerators, NO_COLUMN_GENERATORS);
    }

    //==================================================================================================================
    // Supplier Implementation Methods
    //==================================================================================================================

    /**
     * @implNote This method returns a lazy {@link Stream}.
     */
    @Override
    public Stream<String[]> get() {
        return Stream
            .generate(() -> columnGenerators)
            .limit(rows)
            .map(Stream::of) // TODO: Can we optimize this to create less streams?
            .map(TableGenerator::generate);
    }

    //==================================================================================================================
    // Object Implementation Methods
    //==================================================================================================================

    @Override
    public String toString() {
        return "%s[rows=%d, columns=%d]".formatted(super.toString(), rows, columnGenerators.length);
    }

    //==================================================================================================================
    // Private Helper Methods
    //==================================================================================================================

    private static String[] generate(Stream<Supplier<?>> columnGenerators) {
        return columnGenerators
            .map(Supplier::get)
            .map(String::valueOf)
            .toArray(String[]::new);
    }
}
