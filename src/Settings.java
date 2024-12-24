/**
 * This interface defines settings used within the program with the assumption that it is short-lived and does not hold
 *   a state beyond its lifetime.
 */
interface Settings {
    boolean SEQUENTIAL = Boolean.getBoolean("sequential");
    boolean DEBUG = Boolean.getBoolean("debug");
}
