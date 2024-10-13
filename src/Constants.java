import java.nio.file.Path;

/**
 * This interface defines common constants used within the program.
 */
interface Constants {
    // Miscellaneous
    String COMMA = ",";
    String UNKNOWN = "?";

    // Paths
    Path DATA_DIRECTORY = Path.of("").resolve("data");
    Path INPUT_PATH = DATA_DIRECTORY.resolve("input.csv");
    Path OUTPUT_PATH = DATA_DIRECTORY.resolve("output.csv");
    Path FLOW_LOG_PATH = DATA_DIRECTORY.resolve("flow_log.csv");
    Path LOOKUP_TABLE_PATH = DATA_DIRECTORY.resolve("lookup_table.csv");
    Path IANA_PROTOCOLS_PATH = DATA_DIRECTORY.resolve("iana_protocols.csv");
}
