/**
 * This interface defines settings used within the program with the assumption that it is short-lived and does not hold
 *   a state beyond its lifetime.
 */
interface Settings {
    boolean DEBUG = isEnabled("debug");
    boolean PARALLEL = isEnabled("parallel");

    //==================================================================================================================
    // Private Helper Methods
    //==================================================================================================================

    private static boolean isEnabled(String property) {
        return !Boolean.toString(false).equalsIgnoreCase(System.getProperty(property)); // True-by-default semantic
    }
}
