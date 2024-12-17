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
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * This class is a minimal implementation of a testing harness that does not require external testing dependencies
 *   (e.g., JUnit, AssertJ, Mockito).
 */
abstract class BaseUnitTest {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final String HORIZONTAL_RULE = "=".repeat(100);

    static final ClassLoader CLASS_LOADER = LOOKUP.lookupClass().getClassLoader();
    static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    static final Unsafe UNSAFE = getUnsafe();

    final Logger logger = Logger.getLogger(getClass().getName()); // For test classes to use, if they wish

    //==================================================================================================================
    // Test Support
    //==================================================================================================================

    /**
     * This annotation marks a method as a test method that will be invoked by {@link #run()}.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Test { }

    static void run() {
        STACK_WALKER
            .walk(stackFrames ->
                stackFrames
                    .map(StackWalker.StackFrame::getDeclaringClass)
                    .dropWhile(LOOKUP.lookupClass()::equals)
                    .findFirst()
            )
            .ifPresentOrElse(BaseUnitTest::run, () -> assert$(false, "Unable to determine test class."));
    }

    static void run(MethodHandles.Lookup testClass) {
        run(testClass.lookupClass());
    }

    static void run(Class<?> testClass) {
        final var testInstance = stub(testClass);
        final var assertionFailures = new ArrayList<AssertionError>();
        System.out.println(HORIZONTAL_RULE);

        for (final var method : testClass.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Test.class)) {
                continue;
            }

            try {
                System.out.format("[@] Running test: %s%n", method);
                MethodHandles
                    .privateLookupIn(testClass, LOOKUP)
                    .unreflect(method)
                    .bindTo(testInstance)
                    .invoke();
                System.out.format("[^] Test passed: %s%n", method);
            } catch (AssertionError error) {
                System.err.format("[!] Test assertion failure: %s%n", method);
                assertionFailures.add(error);
            } catch (Throwable cause) {
                System.err.format("[!] Test error: %s%n", method);
                cause.printStackTrace(System.err);
            } finally {
                System.out.println(HORIZONTAL_RULE);
            }
        }

        switch (assertionFailures.size()) {
            case 0 -> System.out.println("[^] All tests passed: " + testClass.getSimpleName());
            case 1 -> throw assertionFailures.getFirst();
            default -> throw new AssertionError("[!] Multiple test assertion failures: " + assertionFailures);
        }
    }

    static void assert$(boolean state, Object message) {
        assert$(state, () -> message);
    }

    static void assert$(boolean state, Supplier<?> message) {
        assert$(() -> state, message);
    }

    static void assert$(BooleanSupplier state, Supplier<?> message) {
        if (!state.getAsBoolean()) {
            throw new AssertionError(message.get());
        }
    }

    static <T> T stub(Class<T> type) {
        return stub(type, !type.isInterface() ? null : (proxy, method, args) -> {
            try {
                return stub(method.getReturnType());
            } catch (Throwable cause) {
                return null;
            }
        });
    }

    static <T> T stub(Class<T> type, InvocationHandler handler) {
        if (!type.isInterface() && handler != null) {
            throw new IllegalArgumentException("Only interfaces can use an invocation handler: " + type.getTypeName());
        } else if (type.isPrimitive()) {
            // Use default values for primitive types.
            return type.cast(Array.get(Array.newInstance(type, 1), 0));
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
            if (!type.isInterface()) {
                UNSAFE.throwException(Objects.requireNonNullElse(exception.getCause(), exception));
            }
        }

        // Try using a proxy for interfaces.
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
