import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

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
record Protocol(int port, String name) implements Comparable<Protocol> {
    /**
     * A sentinel value for an unknown protocol
     */
    static final Protocol UNKNOWN = new Protocol(0, Constants.UNKNOWN);

    private static final Comparator<Protocol> COMPARATOR =
        Comparator
            .comparingInt(Protocol::port)
            .thenComparing(Protocol::name);

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    Protocol(String port, String name) {
        this(Integer.parseInt(port), name);
    }

    Protocol(int port, String name) {
        this.port = port;
        this.name = name.toLowerCase();
    }

    //==================================================================================================================
    // Comparable Implementation Methods
    //==================================================================================================================

    @Override
    public int compareTo(Protocol other) {
        return COMPARATOR.compare(this, other);
    }
}
