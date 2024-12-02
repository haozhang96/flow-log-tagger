import java.nio.file.Path;
import java.util.Map;

/**
 * This interface defines common constants used within the program.
 */
interface Constants {
    // Miscellaneous
    String UNKNOWN = "?";
    Map<String, String> SEPARATORS = Map.of("csv", ",", "tsv", "\t", UNKNOWN, " ");

    // Paths
    Path DATA_DIRECTORY = Path.of("").resolve("data");
    Path INPUT_PATH = DATA_DIRECTORY.resolve("input.log");
    Path OUTPUT_PATH = DATA_DIRECTORY.resolve("output.csv");
    Path DEBUG_PATH = DATA_DIRECTORY.resolve("debug.log");
    Path LOOKUP_TABLE_PATH = DATA_DIRECTORY.resolve("lookup_table.csv");
    Path IANA_PROTOCOLS_PATH = DATA_DIRECTORY.resolve("iana_protocols.csv");

    // Sizes
    long MEBIBYTE_SCALE = 1L << 20;
    long FLOW_LOG_RECORD_SIZE = 129L; // The approximate size in bytes for a single line of record in a flow log file

    // Tables
    Tags TAGS = new Tags(new TableFileReader(LOOKUP_TABLE_PATH));
    IANAProtocols IANA_PROTOCOLS = new IANAProtocols(new TableFileReader(IANA_PROTOCOLS_PATH));
}
