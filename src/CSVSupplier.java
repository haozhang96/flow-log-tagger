import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This interface defines an object that supplies CSV (comma-separated values) data lazily into a {@link Stream} of
 *   columns as string arrays.
 */
@FunctionalInterface
interface CSVSupplier extends Supplier<Stream<String[]>> {
    /**
     * A {@link CSVConsumer} that generates no CSV data; mainly used for testing
     */
    CSVSupplier NOOP = Stream::empty;

    //==================================================================================================================
    // Supplier Implementation Methods
    //==================================================================================================================

    /**
     * Retrieve a {@link Stream} of CSV data as string column arrays.
     */
    @Override
    Stream<String[]> get();
}
