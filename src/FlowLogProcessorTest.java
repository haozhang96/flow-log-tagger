import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class FlowLogProcessorTest extends BaseUnitTest {
    private static final TableSupplier DATA =
        () -> Stream.of(
            "2 123456789012 eni-5f6g7h8i 10.0.2.103 52.26.198.183 56000 23 6 15 7500 1620140661 1620140721 REJECT OK".split(" "),
            "2 123456789012 eni-9k10l11m 192.168.1.5 51.15.99.115 49321 25 6 20 10000 1620140661 1620140721 ACCEPT OK".split(" "),
            "2 123456789012 eni-1a2b3c4d 192.168.1.6 87.250.250.242 49152 110 6 5 2500 1620140661 1620140721 ACCEPT OK".split(" "),
            "2 123456789012 eni-1a2b3c4d 10.0.1.102 172.217.7.228 1030 443 6 8 4000 1620140661 1620140721 ACCEPT OK".split(" "),
            "2 123456789012 eni-2d2e2f3g 192.168.2.7 77.88.55.80 49153 993 6 7 3500 1620140661 1620140721 ACCEPT OK".split(" ")
        );
    private static final Tags TAGS =
        new Tags(() -> Stream.of(
            "23,tcp,sv_P1".split(","),
            "25,tcp,sv_P1".split(","),
            "443,tcp,sv_P2".split(",")
        ));
    private static final String[][] EXPECTED_OUTPUT =
        {
            {"Tag Counts:"},
            {"Tag", "Count"},
            {"Untagged", "2"},
            {"sv_P1", "2"},
            {"sv_P2", "1"},
            {},
            {"Port/Protocol Combination Counts:"},
            {"Port", "Protocol", "Count"},
            {"23", "tcp", "1"},
            {"25", "tcp", "1"},
            {"110", "tcp", "1"},
            {"443", "tcp", "1"},
            {"993", "tcp", "1"},
        };
    private static final List<String[]> OUTPUT = new ArrayList<>(EXPECTED_OUTPUT.length);
    private static final FlowLogProcessor TARGET = new FlowLogProcessor(DATA, TAGS, rows -> rows.forEach(OUTPUT::add));

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
    void run_shouldProcessFlowLogCorrectly() {
        TARGET.run();

        final var output = OUTPUT.toArray(String[][]::new);
        assert$(
            equals(output, EXPECTED_OUTPUT),
            () -> "Unexpected rows: expected=%s, given=%s".formatted(toString(EXPECTED_OUTPUT), toString(output))
        );
    }
}
