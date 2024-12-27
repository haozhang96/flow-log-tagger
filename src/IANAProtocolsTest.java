import java.util.stream.Stream;

class IANAProtocolsTest extends BaseUnitTest {
    private static final TableSupplier DATA =
        () -> Stream.of(
            "0,HOPOPT,IPv6 Hop-by-Hop Option,Y,[RFC8200]".split(","),
            "1,ICMP,Internet Control Message,,[RFC792]".split(","),
            "2,IGMP,Internet Group Management,,[RFC1112]".split(",")
        );
    private static final IANAProtocols TARGET = new IANAProtocols(DATA);

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
        assert$(TARGET.remove("0", Protocol.of("0", "HOPOPT")), "Expected mapping to exist");
        assert$(TARGET.remove("1", Protocol.of("1", "ICMP")), "Expected mapping to exist");
        assert$(TARGET.remove("2", Protocol.of("2", "IGMP")), "Expected mapping to exist");
    }
}
