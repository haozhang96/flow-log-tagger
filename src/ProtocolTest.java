import java.util.stream.Stream;

class ProtocolTest extends BaseUnitTest {
    private static final TableSupplier DATA =
        () -> Stream.of(
            "25,tcp,sv_P1".split(","),
            "68,udp,sv_P2".split(","),
            "23,tcp,sv_P1".split(",")
        );
    private static final Tags TARGET = new Tags(DATA);

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
    void shouldMapTabularDataCorrectly() {
        assert$(TARGET.remove(Protocol.of("25", "tcp"), "sv_P1"), "Expected mapping to exist");
        assert$(TARGET.remove(Protocol.of("68", "udp"), "sv_P2"), "Expected mapping to exist");
        assert$(TARGET.remove(Protocol.of("23", "tcp"), "sv_P1"), "Expected mapping to exist");
    }
}
