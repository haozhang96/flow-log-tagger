import sun.misc.Unsafe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * This class is a minimal implementation of a testing harness that does not require external testing dependencies
 *   (e.g., JUnit, AssertJ, Mockito).
 */
abstract class BaseUnitTest {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Class<?> CLASS = LOOKUP.lookupClass();
    private static final ClassLoader CLASS_LOADER = CLASS.getClassLoader();
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final Unsafe UNSAFE = getUnsafe();
    private static final String HORIZONTAL_RULE = "=".repeat(100);

    //==================================================================================================================
    // Test Support
    //==================================================================================================================

    /**
     * This annotation marks a method as a test method that will be invoked by {@link #run()}.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Test { }

    /**
     * Run the current test class.
     *
     * @apiNote This method should be called from the main bootstrap method.
     *
     * @see Test @Test
     */
    static void run() {
        STACK_WALKER
            .walk(stackFrames ->
                stackFrames
                    .map(StackWalker.StackFrame::getDeclaringClass)
                    .dropWhile(CLASS::equals)
                    .findFirst()
            )
            .ifPresentOrElse(BaseUnitTest::run, () -> assert$(false, "Unable to determine test class."));
    }

    /**
     * Run the test class that constructed a given {@link MethodHandles.Lookup}.
     *
     * @param testClass The test class that constructed the {@link MethodHandles.Lookup} to run the tests for
     *
     * @apiNote This method should be called from the main bootstrap method.
     *
     * @see Test @Test
     */
    static void run(MethodHandles.Lookup testClass) {
        run(testClass.lookupClass());
    }

    /**
     * Run a given test class.
     *
     * @param testClass The test class that to run the tests for
     *
     * @apiNote This method should be called from the main bootstrap method.
     *
     * @see Test @Test
     */
    static void run(Class<?> testClass) {
        final var testInstance = instantiate(testClass);
        final var failures = new ArrayList<Throwable>();
        Loggers.INFO.accept(HORIZONTAL_RULE);

        for (final var method : testClass.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Test.class)) {
                continue;
            }

            try {
                Loggers.INFO.accept("[@] Running test: " + method);
                MethodHandles
                    .privateLookupIn(testClass, LOOKUP)
                    .unreflect(method)
                    .bindTo(testInstance)
                    .invoke();
                Loggers.INFO.accept("[^] Test passed: " + method);
            } catch (Throwable cause) {
                final var status = cause instanceof AssertionError ? "assertion failure" : "error";
                Loggers.ERROR.accept("[!] Test %s: %s".formatted(status, method), cause);
                failures.add(cause);
            } finally {
                Loggers.INFO.accept(HORIZONTAL_RULE);
            }
        }

        switch (failures.size()) {
            case 0 -> Loggers.INFO.accept("[^] All tests passed: " + testClass.getSimpleName());
            case 1 -> UNSAFE.throwException(failures.getFirst());
            default -> throw new AssertionError("[!] Multiple test failures: " + failures);
        }
    }

    /**
     * Assert that a given {@code boolean} is {@code true}, throwing an {@link AssertionError} with a given message
     *   otherwise.
     *
     * @param state The {@link boolean} that must be {@code true} for no {@link AssertionError} to be thrown
     * @param message The message to use for constructing the {@link AssertionError} if the given {@link boolean} is
     *                {@code false}
     *
     * @apiNote This method is functionally equivalent to the Java assertion statement {@code assert state : message;}
     *          except that it does not require a JVM flag.
     */
    static void assert$(boolean state, Object message) {
        assert$(state, () -> message);
    }

    /**
     * Assert that a given {@code boolean} is {@code true}, throwing an {@link AssertionError} with a message supplied
     *   by a given message {@link Supplier} otherwise.
     *
     * @param state The {@link boolean} that must be {@code true} for no {@link AssertionError} to be thrown
     * @param message The message {@link Supplier} to use for constructing the {@link AssertionError} if the given
     *                {@link boolean} is {@code false}
     *
     * @apiNote This method is functionally equivalent to the Java assertion statement
     *          {@code assert state : message.get();} except that it does not require a JVM flag.
     */
    static void assert$(boolean state, Supplier<?> message) {
        assert$(() -> state, message);
    }

    /**
     * Assert that a given {@link BooleanSupplier} returns {@code true}, throwing an {@link AssertionError} with a
     *   message supplied by a given message {@link Supplier} otherwise.
     *
     * @param state The {@link BooleanSupplier} that must return {@code true} for no {@link AssertionError} to be thrown
     * @param message The message {@link Supplier} to use for constructing the {@link AssertionError} if the given
     *                {@link BooleanSupplier} returns {@code false}
     *
     * @apiNote This method is functionally equivalent to the Java assertion statement
     *          {@code assert state.getAsBoolean() : message.get();} except that it does not require a JVM flag.
     */
    static void assert$(BooleanSupplier state, Supplier<?> message) {
        if (!state.getAsBoolean()) {
            throw new AssertionError(message.get());
        }
    }

    /**
     * Construct an instance of a given type.
     *
     * @param type The type to construct the instance of
     * @param <T> The type to construct the instance of
     */
    static <T> T instantiate(Class<T> type) {
        if (type.isPrimitive()) {
            // Use default values for primitive types.
            return type.cast(Array.get(Array.newInstance(type, 1), 0));
        } else if (type.isInterface()) {
            // Use reflective proxies for interface types.
            return mock(type, (proxy, method, args) -> {
                try {
                    return instantiate(method.getReturnType());
                } catch (Throwable cause) {
                    return null;
                }
            });
        }

        // Try invoking the default constructor.
        try {
            final var constructor = type.getDeclaredConstructor();
            if (constructor.trySetAccessible()) {
                return constructor.newInstance();
            }
        } catch (ReflectiveOperationException exception) {
            // Fall through to other instantiation methods.
        }

        // Try using JDK's Unsafe instance allocator.
        try {
            return type.cast(UNSAFE.allocateInstance(type));
        } catch (ReflectiveOperationException exception) {
            // Propagate the exception.
            UNSAFE.throwException(Objects.requireNonNullElse(exception.getCause(), exception));
        }

        // This is unreachable, but we must satisfy the compiler.
        return type.cast(UNSAFE);
    }

    /**
     * Construct a mock instance of a given interface type, using a given {@link InvocationHandler} to handle its method
     *   calls.
     *
     * @param type The interface type to construct the mock instance of
     * @param handler The {@link InvocationHandler} to use to handle calls for the mock instance
     * @param <T> The interface type to construct the mock instance of
     */
    static <T> T mock(Class<T> type, InvocationHandler handler) {
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Only interfaces can be mocked: " + type.getTypeName());
        }

        return type.cast(Proxy.newProxyInstance(CLASS_LOADER, new Class<?>[] {type}, (proxy, method, args) ->
            switch (method.getName()) {
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> Objects.toIdentityString(proxy);
                case String ignored when method.isDefault() -> InvocationHandler.invokeDefault(proxy, method, args);
                default -> handler != null ? handler.invoke(proxy, method, args) : null;
            }
        ));
    }

    /**
     * @see Arrays#deepToString(Object[])
     */
    static String toString(Object[] array) {
        return Arrays.deepToString(array);
    }

    /**
     * @see Arrays#deepEquals(Object[], Object[])
     */
    static boolean equals(Object[] a, Object[] b) {
        return Arrays.deepEquals(a, b);
    }

    //==================================================================================================================
    // Private Helper Methods
    //==================================================================================================================

    private static Unsafe getUnsafe() {
        try {
            final var field = Unsafe.class.getDeclaredField("theUnsafe");
            return field.trySetAccessible() ? (Unsafe) field.get(null) : Unsafe.getUnsafe();
        } catch (ReflectiveOperationException | SecurityException exception) {
            throw new IllegalStateException("Cannot continue testing without setting up unsafe mechanics", exception);
        }
    }
}
