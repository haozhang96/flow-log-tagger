import java.io.Serial;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * This class is a {@link TableMap} of the lookup table mapping destination port and protocol combinations to their
 *   named tags.
 * <br/><br/>
 *
 * An example of the CSV file is as follows:
 * {@snippet lang="csv":
 *   dstport,protocol,tag
 *   25,tcp,sv_P1
 *   68,udp,sv_P2
 *   23,tcp,sv_P1
 *   31,udp,SV_P3
 *   443,tcp,sv_P2
 *   22,tcp,sv_P4
 *   3389,tcp,sv_P5
 *   0,icmp,sv_P5
 *   110,tcp,email
 *   993,tcp,email
 *   143,tcp,email
 * }
 */
class Tags extends TableMap<Protocol, String> {
    private static final @Serial long serialVersionUID = 1L;
    private static final int PORT = 0;
    private static final int PROTOCOL = 1;
    private static final int TAG = 2;
    private static final Collector<String[], ?, ? extends Map<Protocol, String>> COLLECTOR =
        Collectors.toConcurrentMap(columns -> Protocol.of(columns[PORT], columns[PROTOCOL]), columns -> columns[TAG]);

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    /**
     * Construct an empty instance of {@link Tags}.
     */
    Tags() {
        super(COLLECTOR);
    }

    /**
     * Construct an instance of {@link Tags} with its initial mappings built using a given {@link TableSupplier}.
     *
     * @param data The {@link TableSupplier} to use to build the constructed {@link Tags}' initial mappings
     */
    Tags(TableSupplier data) {
        super(data, COLLECTOR);
    }
}
