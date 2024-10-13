import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;

/**
 * This interface defines common constants used within the program.
 */
interface Constants {
    // Paths
    Path WORKING_DIRECTORY = Path.of("");
    Path DATA_DIRECTORY = WORKING_DIRECTORY.resolve("data");
    Path INPUT_PATH = DATA_DIRECTORY.resolve("input.csv");
    Path OUTPUT_PATH = DATA_DIRECTORY.resolve("output.csv");
    Path FLOW_LOG_PATH = DATA_DIRECTORY.resolve("flow_log.csv");
    Path LOOKUP_TABLE_PATH = DATA_DIRECTORY.resolve("lookup_table.csv");

    // Miscellaneous
    String COMMA = ",";
    String UNKNOWN = "?";
    OpenOption[] WRITE_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND};
    Map<String, Protocol> IANA_PROTOCOLS =
        Collections.unmodifiableMap(new IANAProtocols(new CSVReader(DATA_DIRECTORY.resolve("iana_protocols.csv"))));
}
