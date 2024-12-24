import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * This interface defines simple loggers used within the program.
 */
interface Loggers {
    Consumer<Object> INFO = System.out::println;
    BiConsumer<Object, Throwable> ERROR =
        (message, cause) -> { System.err.println(message); cause.printStackTrace(System.err); };
}
