/**
 * This interface defines settings used within the program with the assumption that it is short-lived and does not hold
 *   a state beyond its lifetime.
 */
interface Settings {
    /**
     * Overwrite the previous output from {@link FlowLogProcessor} ({@link Constants#OUTPUT_PATH} by default) if it
     *   already exists.
     * <br/><br/>
     *
     * The default value is {@code true}.
     */
    boolean OVERWRITE = getProperty("overwrite", true);

    /**
     * Enable certain features to help with debugging the program, such as:
     * <ul>
     *   <li>Pipe {@link FlowLogProcessor}'s input to {@link Constants#DEBUG_PATH}.</li>
     *   <li>Print out {@link FlowLogProcessor}'s processed row count and approximate file size.</li>
     * </ul>
     * <br/>
     *
     * The default value is {@code false}.
     */
    boolean DEBUG = getProperty("debug", false);

    /**
     * Minimize processing times by skipping certain optional processing steps, such as:
     * <ul>
     *   <li>Enable more lenient input data parsing in {@link TableFileReader}.</li>
     *   <li>Disable {@link Protocol} caching to minimize bookkeeping overhead.</li>
     * </ul>
     * <br/>
     *
     * The default value is {@code true}.
     */
    boolean FAST = getProperty("fast", true);

    /**
     * Allow {@link FlowLogProcessor} to process its input concurrently.
     * <br/><br/>
     *
     * The default value is {@code true}; the {@link #FAST} option implies this option.
     */
    boolean PARALLEL = FAST || getProperty("parallel", true);

    /**
     * Warm up the Java virtual runtime for {@link FlowLogGenerator} by repeatedly running through "hot spots" to hint
     *   to the just-in-time compiler to inline certain methods.
     * <br/><br/>
     *
     * The default value is {@code false}.
     */
    boolean WARM_UP = getProperty("warmup", false);

    //==================================================================================================================
    // Private Helper Methods
    //==================================================================================================================

    private static boolean getProperty(String propertyName, boolean defaultValue) {
        return Boolean.parseBoolean(System.getProperty(propertyName, Boolean.toString(defaultValue)));
    }
}
