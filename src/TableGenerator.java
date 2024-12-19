import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
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
        this.columnGenerators = Objects.requireNonNull(columnGenerators);
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
            .map(TableGenerator::generate);
    }

    //==================================================================================================================
    // Iterable Implementation Methods
    //==================================================================================================================

    @Override
    public Spliterator<String[]> spliterator() {
        return Spliterators
            .spliterator(iterator(), rows, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
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

    private static String[] generate(Supplier<?>... columnGenerators) {
        var columns = new String[columnGenerators.length];

        // Use a simple for-loop instead of a stream to avoid a stream creation for every row.
        for (int index = 0; index < columns.length; ++index) {
            columns[index] = String.valueOf(columnGenerators[index].get());
        }

        return columns;
    }
}
