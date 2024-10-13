import java.io.Serial;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is a {@link CSVMap} of IANA-assigned protocol numbers mapping from their unique decimal values to their
 *   corresponding keywords.
 *
 * @see <a href="https://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml">IANA Protocol Numbers (Spec)</a>
 * @see <a href="https://www.iana.org/assignments/protocol-numbers/protocol-numbers-1.csv">IANA Protocol Numbers (CSV)</a>
 *
 */
class IANAProtocols extends CSVMap<String, Protocol> {
    static Map<String, Protocol> DEFAULT = Collections.unmodifiableMap(new IANAProtocols(CSVReader.IANA_PROTOCOLS));

    private static final @Serial long serialVersionUID = 1L;
    private static final int DECIMAL = 0;
    private static final int KEYWORD = 1;

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    IANAProtocols(CSVReader reader) {
        super(
            reader,
            rows -> rows.filter(IANAProtocols::isNumeric),
            Collectors.toMap(columns -> columns[DECIMAL], columns -> new Protocol(columns[DECIMAL], columns[KEYWORD]))
        );
    }

    //==================================================================================================================
    // Private Helper Methods
    //==================================================================================================================

    private static boolean isNumeric(String[] columns) {
        try {
            // I could technically use Character.isDigit() with a loop over the characters but this is more compact.
            Integer.parseInt(columns[DECIMAL]);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}
