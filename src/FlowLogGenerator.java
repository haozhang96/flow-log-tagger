import java.util.concurrent.ThreadLocalRandom;

/**
 * This class is a {@link TableGenerator} that generates a flow log with a defined number of rows using randomly
 *   generated data, and is mainly used for testing.
 * <br/><br/>
 *
 * An example row of generated flow log is as follows:
 * {@snippet lang="csv":
 *   2 123456789012 eni-0a1b2c3d 10.0.1.201 198.51.100.2 443 49153 6 25 20000 1620140761 1620140821 ACCEPT OK
 * }
 */
class FlowLogGenerator extends TableGenerator {
    private static final int[] PORTS = {23, 25, 80, 110, 143, 443, 993, 1024, 1030, 49152, 49153, 49154, 49321, 56000};
    private static final int[] PROTOCOLS = {1, 4, 6, 12, 17, 27, 41, 58, 115, 143};

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    /**
     * Construct a {@link FlowLogGenerator} that generates a flow log with a given number of rows.
     *
     * @param rows The number of rows of the simulated flow log file to construct the {@link FlowLogGenerator} for
     */
    FlowLogGenerator(long rows) {
        super(
            rows,
            FlowLogGenerator::version,
            FlowLogGenerator::accountID,
            FlowLogGenerator::interfaceID,
            FlowLogGenerator::sourceAddress,
            FlowLogGenerator::destinationAddress,
            FlowLogGenerator::sourcePort,
            FlowLogGenerator::destinationPort,
            FlowLogGenerator::protocol,
            FlowLogGenerator::packets,
            FlowLogGenerator::bytes,
            FlowLogGenerator::start,
            FlowLogGenerator::end,
            FlowLogGenerator::action,
            FlowLogGenerator::logStatus
        );
    }

    //==================================================================================================================
    // Factory Methods
    //==================================================================================================================

    /**
     * Construct a {@link FlowLogGenerator} that generates a flow log with a number of rows approximating a given file
     *   size in bytes.
     *
     * @param b The number of bytes of the simulated flow log file to construct the {@link FlowLogGenerator} for
     */
    static FlowLogGenerator ofBytes(long b) {
        return new FlowLogGenerator(b / Constants.FLOW_LOG_RECORD_SIZE);
    }

    /**
     * Construct a {@link FlowLogGenerator} that generates a flow log with a number of rows approximating a given file
     *   size in mebibytes.
     *
     * @param mib The number of mebibytes of the simulated flow log file to construct the {@link FlowLogGenerator} for
     */
    static FlowLogGenerator ofMebibytes(Number mib) {
        return ofBytes(Math.round(mib.doubleValue() * Constants.MEBIBYTE_SCALE));
    }

    //==================================================================================================================
    // Private Helper Methods
    //==================================================================================================================

    private static long version() {
        return 2L;
    }

    private static long accountID() {
        return ThreadLocalRandom.current().nextLong(999_999_999_999L);
    }

    private static String interfaceID() {
        return "eni-" + Integer.toHexString(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));
    }

    private static String sourceAddress() {
        return "%d.%d.%d.%d".formatted(
            ThreadLocalRandom.current().nextInt(256),
            ThreadLocalRandom.current().nextInt(256),
            ThreadLocalRandom.current().nextInt(256),
            ThreadLocalRandom.current().nextInt(256)
        );
    }

    private static String destinationAddress() {
        return "%d.%d.%d.%d".formatted(
            ThreadLocalRandom.current().nextInt(256),
            ThreadLocalRandom.current().nextInt(256),
            ThreadLocalRandom.current().nextInt(256),
            ThreadLocalRandom.current().nextInt(256)
        );
    }

    private static int sourcePort() {
        return PORTS[ThreadLocalRandom.current().nextInt(PORTS.length)];
    }

    private static int destinationPort() {
        return PORTS[ThreadLocalRandom.current().nextInt(PORTS.length)];
    }

    private static int protocol() {
        return PROTOCOLS[ThreadLocalRandom.current().nextInt(PROTOCOLS.length)];
    }

    private static int packets() {
        return ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
    }

    private static int bytes() {
        return ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
    }

    private static long start() {
        return System.currentTimeMillis();
    }

    private static long end() {
        return System.currentTimeMillis();
    }

    private static String action() {
        return ThreadLocalRandom.current().nextBoolean() ? "ACCEPT" : "REJECT";
    }

    private static String logStatus() {
        return "OK";
    }
}
