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
    void stub_givenInterface_willCreateStubCorrectly() {
        final var table = TableConsumerTest.STRING_ROWS;
        final InvocationHandler handler =
            (proxy, method, args) -> method.getName().equals("get") ? Stream.of(table) : stub(method.getReturnType());

        try (var rows = stub(TableSupplier.class, handler).iterator()) {
            rows.forEachRemaining(columns -> {
                System.out.println(Arrays.toString(columns));

                assert$(
                    Stream
                        .of(table)
                        .map(Arrays::asList)
                        .anyMatch(Arrays.asList(columns)::equals),
                    () ->
                        "A slice of %s should be produced: %s".formatted(
                            Arrays.deepToString(table),
                            Arrays.toString(columns)
                        )
                );
            });
        }
    }

    @Test
    void stub_givenConcreteClass_willCreateStubCorrectly() {
        assert$(stub(TableGenerator.class).get().findAny().isEmpty(), "An empty but valid stream should be produced.");
    }
}
