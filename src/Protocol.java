import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
record Protocol(String port, String name) implements Serializable {
    private static final @Serial long serialVersionUID = 1L;
    private static final ConcurrentMap<Integer, Protocol> CACHE = new ConcurrentHashMap<>(1 << 16);

    /**
     * A sentinel value for an unknown protocol
     */
    static final Protocol UNKNOWN = of(Constants.UNKNOWN, Constants.UNKNOWN);

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    /**
     * @apiNote Use {@link #of(String, String)} instead to take advantage of caching.
     */
    Protocol(String port, String name) {
        this.port = port;
        this.name = name.toLowerCase(); // Implicit null check
    }

    //==================================================================================================================
    // Factory Methods
    //==================================================================================================================

    /**
     * Retrieve an instance of {@link Protocol} corresponding to a given port and name.
     *
     * @param port The port of the {@link Protocol} to retrieve
     * @param name The name of the {@link Protocol} to retrieve
     * @return A potentially cached instance of {@link Protocol} corresponding to the given port and name
     */
    static Protocol of(String port, String name) {
        // Cache instances using the hash of the arguments, if allowed; see Map.Entry.hashCode() for the algorithm used.
        return Settings.FAST
            ? new Protocol(port, name)
            : CACHE.computeIfAbsent(port.hashCode() ^ name.toLowerCase().hashCode(), hash -> new Protocol(port, name));

        // TODO: Implement soft-reference-value-based caching or an eviction policy for the current cache instead. This
        //   would require weighing memory constraints against processing time constraints. Currently, we prefer faster
        //   processing times.
    }
}
