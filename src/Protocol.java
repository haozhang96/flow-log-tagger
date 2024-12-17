import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
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
record Protocol(int port, String name) implements Comparable<Protocol> {
    private static final ConcurrentMap<Integer, Protocol> CACHE = new ConcurrentHashMap<>(1 << 16);
    private static final Comparator<Protocol> COMPARATOR =
        Comparator
            .comparingInt(Protocol::port)
            .thenComparing(Protocol::name);

    /**
     * A sentinel value for an unknown protocol
     */
    static final Protocol UNKNOWN = of(0, Constants.UNKNOWN);

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    /**
     * @apiNote Use {@link #of(int, String)} instead to take advantage of caching.
     */
    Protocol(int port, String name) {
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
        return of(Integer.parseInt(port), name);
    }

    /**
     * Retrieve an instance of {@link Protocol} corresponding to a given port and name.
     *
     * @param port The port of the {@link Protocol} to retrieve
     * @param name The name of the {@link Protocol} to retrieve
     * @return A potentially cached instance of {@link Protocol} corresponding to the given port and name
     */
    static Protocol of(int port, String name) {
        // Cache instances using the computed hash of the arguments.
        return CACHE.computeIfAbsent(Objects.hash(port, name.toLowerCase()), ignored -> new Protocol(port, name));

        // TODO: Implement soft-reference-value-based caching or an eviction policy for the current cache instead. This
        //   would require weighing memory constraints against processing time constraints. Currently, we prefer faster
        //   processing times.
    }

    //==================================================================================================================
    // Comparable Implementation Methods
    //==================================================================================================================

    @Override
    public int compareTo(Protocol other) {
        return COMPARATOR.compare(this, other);
    }
}
