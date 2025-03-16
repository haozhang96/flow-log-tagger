import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

/**
 * This interface defines common constants used within the program.
 */
interface Constants {
    // Miscellaneous
    String UNKNOWN = "?";
    Map<String, String> SEPARATORS = Map.of("csv", ",", "tsv", "\t", UNKNOWN, " ");

    // Paths
    Path ROOT_DIRECTORY = Path.of("");
    Path RESOURCE_DIRECTORY = ROOT_DIRECTORY.resolve("res");
    Path OUTPUT_DIRECTORY = ROOT_DIRECTORY.resolve("out");
    Path INPUT_PATH = RESOURCE_DIRECTORY.resolve("input.log");
    Path OUTPUT_PATH = OUTPUT_DIRECTORY.resolve("output.csv");
    Path DEBUG_PATH = OUTPUT_DIRECTORY.resolve("debug.log");
    Path LOOKUP_TABLE_PATH = RESOURCE_DIRECTORY.resolve("lookup_table.csv");
    Path IANA_PROTOCOLS_PATH = RESOURCE_DIRECTORY.resolve("iana_protocols.csv");

    // Tables
    Tags TAGS = new Tags(new TableFileReader(LOOKUP_TABLE_PATH));
    IANAProtocols IANA_PROTOCOLS = new IANAProtocols(new TableFileReader(IANA_PROTOCOLS_PATH));

    // Runtime support
    int PARALLELISM = ForkJoinPool.getCommonPoolParallelism();
    long FLOW_LOG_RECORD_SIZE = 129L; // The approximate size in bytes for a single line of record in a flow log file
    long KIBIBYTE_SCALE = 1L << 10;
    long MEBIBYTE_SCALE = KIBIBYTE_SCALE << 10;
}
