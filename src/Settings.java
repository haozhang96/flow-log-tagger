/**
 * This interface defines settings used within the program with the assumption that it is short-lived and does not hold
 *   a state beyond its lifetime.
 */
interface Settings {
    /**
     * Enable certain features to help with debugging the program, such as:
     * <ul>
     *   <li>Pipe {@link FlowLogProcessor}'s input to {@link Constants#DEBUG_PATH}.</li>
     *   <li>Print out {@link FlowLogProcessor}'s processed row count and approximate file size.</li>
     * </ul>
     */
    boolean DEBUG = Boolean.getBoolean("debug");

    /**
     * Minimize processing times by skipping certain processing steps that enable more lenient input data parsing.
     */
    boolean FAST = Boolean.getBoolean("fast");

    /**
     * Force {@link FlowLogProcessor} to process its input sequentially.
     */
    boolean SEQUENTIAL = Boolean.getBoolean("sequential");

    /**
     * Warm up the Java virtual runtime for {@link FlowLogGenerator} by repeatedly running through "hot spots" to hint
     *   to the just-in-time compiler to inline certain methods.
     */
    boolean WARM_UP = Boolean.getBoolean("warm-up");
}
