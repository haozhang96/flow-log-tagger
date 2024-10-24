import java.util.concurrent.ThreadLocalRandom;

/**
 * This class is a {@link TableGenerator} that generates a flow log with a defined number of rows using randomly generated
 *   data, and is mainly used for testing.
 */
class FlowLogGenerator extends TableGenerator {
    private static final String VERSION = String.valueOf(2L);
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
        return new FlowLogGenerator(b * Constants.FLOW_LOG_RECORD_SIZE);
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

    private static String version() {
        return VERSION;
    }

    private static String accountID() {
        return String.valueOf(ThreadLocalRandom.current().nextLong(0L, 999_999_999_999L));
    }

    private static String interfaceID() {
        return "eni-" + Integer.toHexString(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE));
    }

    private static String sourceAddress() {
        return "%d.%d.%d.%d".formatted(
            ThreadLocalRandom.current().nextInt(0, 256),
            ThreadLocalRandom.current().nextInt(0, 256),
            ThreadLocalRandom.current().nextInt(0, 256),
            ThreadLocalRandom.current().nextInt(0, 256)
        );
    }

    private static String destinationAddress() {
        return "%d.%d.%d.%d".formatted(
            ThreadLocalRandom.current().nextInt(0, 256),
            ThreadLocalRandom.current().nextInt(0, 256),
            ThreadLocalRandom.current().nextInt(0, 256),
            ThreadLocalRandom.current().nextInt(0, 256)
        );
    }

    private static String sourcePort() {
        return String.valueOf(PORTS[ThreadLocalRandom.current().nextInt(PORTS.length)]);
    }

    private static String destinationPort() {
        return String.valueOf(PORTS[ThreadLocalRandom.current().nextInt(PORTS.length)]);
    }

    private static String protocol() {
        return String.valueOf(PROTOCOLS[ThreadLocalRandom.current().nextInt(PROTOCOLS.length)]);
    }

    private static String packets() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE));
    }

    private static String bytes() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE));
    }

    private static String start() {
        return String.valueOf(System.currentTimeMillis());
    }

    private static String end() {
        return String.valueOf(System.currentTimeMillis());
    }

    private static String action() {
        return ThreadLocalRandom.current().nextBoolean() ? "ACCEPT" : "REJECT";
    }

    private static String logStatus() {
        return "OK";
    }
}
