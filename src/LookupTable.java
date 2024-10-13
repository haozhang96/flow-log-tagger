import java.io.Serial;
import java.util.stream.Collectors;

/**
 * This class is a {@link CSVMap} of the lookup table mapping destination port and protocol combinations to their named
 *   tags.
 * <br/><br/>
 *
 * An example of a lookup table is as follows:
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
class LookupTable extends CSVMap<Protocol, String> {
    private static final @Serial long serialVersionUID = 1L;
    private static final int PORT = 0;
    private static final int PROTOCOL = 1;
    private static final int TAG = 2;

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    LookupTable(CSVReader reader) {
        super(
            reader,
            Collectors.toMap(columns -> new Protocol(columns[PORT], columns[PROTOCOL]), columns -> columns[TAG])
        );
    }
}
