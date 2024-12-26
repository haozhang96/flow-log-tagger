import java.lang.reflect.InvocationHandler;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

class BaseUnitTestTest extends BaseUnitTest {
    private static final String MESSAGE = "Unexpected assertion failure";
    private static final Supplier<String> MESSAGE_SUPPLIER = () -> MESSAGE;

    //==================================================================================================================
    // Bootstrap
    //==================================================================================================================

    public static void main(String[] args) {
        run();
    }

    //==================================================================================================================
    // Tests
    //==================================================================================================================

    @Test
    void assert_givenBooleanStateAndStringMessage_willAssertCorrectly() {
        assert$(true, MESSAGE);

        try {
            assert$(false, MESSAGE);
        } catch (AssertionError error) {
            return;
        }

        // We should never get to this point.
        assert$(false, MESSAGE);
    }

    @Test
    void assert_givenBooleanStateAndStringMessageSupplier_willAssertCorrectly() {
        assert$(true, MESSAGE_SUPPLIER);

        try {
            assert$(false, MESSAGE_SUPPLIER);
        } catch (AssertionError error) {
            return;
        }

        // We should never get to this point.
        assert$(false, MESSAGE_SUPPLIER);
    }

    @Test
    void assert_givenBooleanStateSupplierAndStringMessageSupplier_willAssertCorrectly() {
        assert$(() -> true, MESSAGE_SUPPLIER);

        try {
            assert$(() -> false, MESSAGE_SUPPLIER);
        } catch (AssertionError error) {
            return;
        }

        // We should never get to this point.
        assert$(() -> false, MESSAGE_SUPPLIER);
    }

    @Test
    void instantiate_givenConcreteClass_willCreateInstanceCorrectly() {
        assert$(instantiate(TableGenerator.class).get().findAny().isEmpty(), "Expected an empty but valid stream");
    }

    @Test
    void instantiate_givenInterface_willCreateInstanceCorrectly() {
        assert$(instantiate(TableSupplier.class).get().findAny().isEmpty(), "Expected an empty but valid stream");
    }

    @Test
    void mock_givenConcreteClass_willThrowIllegalArgumentException() {
        try {
            mock(TableGenerator.class, InvocationHandler::invokeDefault);
        } catch (IllegalArgumentException exception) {
            return;
        }

        assert$(false, "Expected an exception to be thrown for invalid interface type");
    }

    @Test
    void mock_givenInterface_willCreateMockInstanceCorrectly() {
        final var table = TableConsumerTest.STRING_ROWS;
        final InvocationHandler handler =
            (proxy, method, args) ->
                method.getName().equals("get") ? Stream.of(table) : instantiate(method.getReturnType());

        try (var rows = mock(TableSupplier.class, handler).iterator()) {
            rows.forEachRemaining(columns -> assert$(
                Stream
                    .of(table)
                    .map(Arrays::asList)
                    .anyMatch(Arrays.asList(columns)::equals),
                () -> "Expected a subsequence of %s: %s".formatted(toString(table), toString(columns))
            ));
        }
    }
}
