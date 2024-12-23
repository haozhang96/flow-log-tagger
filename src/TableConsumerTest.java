import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Stream;

class TableConsumerTest extends BaseUnitTest {
    private static final TableConsumer TARGET = TableConsumerTest::assertRows;

    static final Object[][] OBJECT_ROWS =
        {{"column 1", 2, 3L, 4D, 5F, (byte) 6, (char) 7, BigInteger.valueOf(8L), BigDecimal.valueOf(9D), true}};
    static final String[][] STRING_ROWS =
        Stream
            .of(OBJECT_ROWS)
            .map(columns -> Stream.of(columns).map(String::valueOf).toArray(String[]::new))
            .toArray(String[][]::new);

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
    void row_givenObjectArray_willInvokeAcceptWithCorrectArguments() {
        TARGET.row(OBJECT_ROWS[0]);
    }

    @Test
    void row_givenStringArray_willInvokeAcceptWithCorrectArguments() {
        TARGET.row(STRING_ROWS[0]);
    }

    @Test
    void row_givenStream_willInvokeAcceptWithCorrectArguments() {
        TARGET.row(Stream.of(STRING_ROWS[0]));
    }

    @Test
    void row_givenIterable_willInvokeAcceptWithCorrectArguments() {
        TARGET.row(Arrays.asList(STRING_ROWS[0]));
    }

    @Test
    void rows_givenObjectArray_willInvokeAcceptWithCorrectArguments() {
        TARGET.rows(OBJECT_ROWS);
    }

    @Test
    void rows_givenStringArray_willInvokeAcceptWithCorrectArguments() {
        TARGET.rows(STRING_ROWS);
    }

    @Test
    void rows_givenStream_willInvokeAcceptWithCorrectArguments() {
        TARGET.rows(Stream.of(STRING_ROWS));
    }

    @Test
    void rows_givenIterable_willInvokeAcceptWithCorrectArguments() {
        TARGET.rows(Arrays.asList(STRING_ROWS));
    }

    //==================================================================================================================
    // Assertion Helpers
    //==================================================================================================================

    private static void assertRows(Iterable<String[]> rows) {
        final var rows$ = Utils.toArray(rows, String[][]::new);
        assert$(
            equals(rows$, STRING_ROWS),
            () -> "Unexpected rows: expected=%s, given=%s".formatted(toString(STRING_ROWS), toString(rows$))
        );
    }
}
