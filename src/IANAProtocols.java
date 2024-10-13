import java.io.Serial;
import java.util.Collections;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is a {@link CSVMap} of IANA-assigned protocol numbers mapping from their unique decimal values to their
 *   corresponding keywords.
 * <br/><br/>
 *
 * An example of the CSV file is as follows:
 * {@snippet lang="csv":
 *   Decimal,Keyword,Protocol,IPv6 Extension Header,Reference
 *   0,HOPOPT,IPv6 Hop-by-Hop Option,Y,[RFC8200]
 *   1,ICMP,Internet Control Message,,[RFC792]
 *   2,IGMP,Internet Group Management,,[RFC1112]
 *   3,GGP,Gateway-to-Gateway,,[RFC823]
 *   4,IPv4,IPv4 encapsulation,,[RFC2003]
 *   5,ST,Stream,,[RFC1190][RFC1819]
 *   6,TCP,Transmission Control,,[RFC9293]
 *   7,CBT,CBT,,[Tony_Ballardie]
 *   8,EGP,Exterior Gateway Protocol,,[RFC888][David_Mills]
 * }
 *
 * @see <a href="https://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml">IANA Protocol Numbers (Spec)</a>
 * @see <a href="https://www.iana.org/assignments/protocol-numbers/protocol-numbers-1.csv">IANA Protocol Numbers (CSV)</a>
 */
class IANAProtocols extends CSVMap<Integer, Protocol> {
    private static final @Serial long serialVersionUID = 1L;
    private static final int DECIMAL = 0;
    private static final int KEYWORD = 1;
    private static final UnaryOperator<Stream<String[]>> FILTER =
        rows -> rows.filter(columns -> columns[DECIMAL].codePoints().allMatch(Character::isDigit));
    private static final Collector<String[], ?, Map<Integer, Protocol>> COLLECTOR =
        Collectors.toMap(
            columns -> Integer.parseInt(columns[DECIMAL]),
            columns -> new Protocol(columns[DECIMAL], columns[KEYWORD])
        );

    /**
     * A read-only view of the default {@link IANAProtocols}
     */
    static Map<Integer, Protocol> DEFAULT =
        Collections.unmodifiableMap(new IANAProtocols(new CSVFileReader(Constants.IANA_PROTOCOLS_PATH)));

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    IANAProtocols(CSVGenerator csv) {
        super(csv, FILTER, COLLECTOR);
    }
}
