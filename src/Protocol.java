import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

/**
 * This class defines a protocol with a port number and a name.
 * <br/><br/>
 *
 * Instances of this class are compared with their port number and case-insensitive name and can be used within
 *   {@linkplain Object#hashCode() hash-based} data structures such as {@link HashMap}s and {@link HashSet}s.
 *
 * @param port The port number of the protocol
 * @param name The name of the protocol
 */
record Protocol(int port, String name) {
    /**
     * A sentinel value for an unknown protocol
     */
    static final Protocol UNKNOWN = new Protocol(0, Constants.UNKNOWN);

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    Protocol(String port, String name) {
        this(Integer.parseInt(port), name);
    }

    Protocol(int port, String name) {
        this.name = name.strip().toLowerCase();
        this.port = Objects.checkIndex(port, 1 << 16); // Range of [0, 2^16)
    }
}
