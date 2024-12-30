import java.lang.ref.Cleaner;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This interface defines an object that supplies tabular data into a {@link Stream} of columns as string arrays.
 *
 * @apiNote A {@link TableSupplier} can be used multiple times. {@link Stream}s (and, consequently,
 *          {@link java.util.Iterator Iterator}s) retrieved from a {@link TableSupplier} are independent of one another.
 * @implNote This interface supplies {@link Stream}s instead of {@link Iterable}s to allow lazy data generation.
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
     *
     * @return A {@link Stream} of the tabular data as string column arrays
     *
     * @apiNote {@link Stream}s retrieved using this method must be used with a
     *          <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">try-with-resources
     *          statement</a> to ensure any underlying resources are properly closed.
     * @implSpec Implementers should specify whether this method will return a lazy {@link Stream} constructed from a
     *           buffering data source. This would help callers prepare for resource demands.
     */
    @Override
    Stream<String[]> get();

    //==================================================================================================================
    // Iterable Implementation Methods
    //==================================================================================================================

    /**
     * Retrieve an {@link java.util.Iterator Iterator} of the tabular data as string column arrays.
     *
     * @apiNote {@link java.util.Iterator Iterator}s retrieved using this method must be used with a
     *          <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">try-with-resources
     *          statement</a> to ensure any underlying resources are properly closed.
     */
    @Override
    default Iterator iterator() {
        return new Iterator(this);
    }

    /**
     * Retrieve a {@link Spliterator} of the tabular data as string column arrays.
     */
    @Override
    default Spliterator<String[]> spliterator() {
        // Use java.nio.file.FileChannelLinesSpliterator as reference for the spliterator's characteristics.
        return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED | Spliterator.NONNULL);
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
        private static final Cleaner CLEANER = Cleaner.create();

        private final java.util.Iterator<String[]> delegate;
        private final Cleaner.Cleanable cleanable;

        //==============================================================================================================
        // Constructors
        //==============================================================================================================

        private Iterator(TableSupplier table) {
            final var stream = table.get(); // Implicit null check
            delegate = stream.iterator();
            cleanable = CLEANER.register(this, stream::close); // Close the stream when we become garbage-collected.
        }

        //==============================================================================================================
        // Iterator Implementation Methods
        //==============================================================================================================

        @Override
        public boolean hasNext() {
            try {
                return delegate.hasNext();
            } catch (RuntimeException exception) {
                // Only swallow the exception if the underlying stream is closed.
                return switch (exception.getMessage()) {
                    case String message when message.endsWith("closed") || message.contains("EOF") -> false;
                    case null, default -> throw exception;
                };
            }
        }

        @Override
        public String[] next() {
            // Ensure that the underlying stream is closed at the end of iteration.
            try (this) {
                return delegate.next();
            }
        }

        //==============================================================================================================
        // AutoCloseable Implementation Methods
        //==============================================================================================================

        @Override
        public void close() {
            // Only close the underlying stream at the end of iteration; the cleaner will handle incomplete iterations.
            if (!hasNext()) {
                cleanable.clean();
            }
        }
    }
}
