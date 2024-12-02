import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This interface defines an object that supplies tabular data lazily into a {@link Stream} of columns as string arrays.
 */
@FunctionalInterface
interface TableSupplier extends Supplier<Stream<String[]>>, Iterable<String[]> {
    /**
     * A {@link TableConsumer} that generates no tabular data; mainly used for testing
     */
    TableSupplier NOOP = Stream::empty;

    //==================================================================================================================
    // Supplier Implementation Methods
    //==================================================================================================================

    /**
     * Retrieve a {@link Stream} of the tabular data as string column arrays.
     */
    @Override
    Stream<String[]> get();

    //==================================================================================================================
    // Iterable Implementation Methods
    //==================================================================================================================

    /**
     * Retrieve an {@link Iterator} of the tabular data as string column arrays.
     */
    @Override
    default Iterator iterator() {
        return new Iterator(this);
    }

    //==================================================================================================================
    // Iterator Support
    //==================================================================================================================

    /**
     * This class is an {@link java.util.Iterator Iterator} that wraps around a {@link TableSupplier}'s
     *   {@linkplain TableSupplier#get() Stream}s to allow them to be used with a
     *   <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">try-with-resources
     *   statement</a> to ensure any underlying resources are properly closed.
     */
    class Iterator implements java.util.Iterator<String[]>, AutoCloseable {
        private final Stream<String[]> data;
        private final java.util.Iterator<String[]> iterator;

        //==============================================================================================================
        // Constructors
        //==============================================================================================================

        Iterator(TableSupplier table) {
            iterator = (data = table.get()).iterator();
        }

        //==============================================================================================================
        // Iterator Implementation Methods
        //==============================================================================================================

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public String[] next() {
            try (this) {
                return iterator.next();
            }
        }

        //==============================================================================================================
        // AutoCloseable Implementation Methods
        //==============================================================================================================

        @Override
        public void close() {
            if (!hasNext()) {
                data.close();
            }
        }
    }
}
