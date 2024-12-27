import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This abstract class implements the foundational logic shared between {@link TableFileReader} and
 *   {@link TableFileWriter}.
 */
sealed abstract class AbstractTableFileProcessor permits TableFileReader, TableFileWriter, AbstractTableFileProcessorTest.Target {
    final Path path;
    final String separator;

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    /**
     * Initialize this {@link AbstractTableFileProcessor} with a given {@link Path} and column separator.
     *
     * @param path The {@link Path} that the constructed {@link AbstractTableFileProcessor} will process
     * @param separator The column separator to use for processing; may be {@code null} to allow
     *                  {@link AbstractTableFileProcessor} to infer this information based on {@code path}'s file
     *                  characteristics
     */
    AbstractTableFileProcessor(Path path, String separator) {
        this.path = Objects.requireNonNull(path);
        this.separator = Objects.requireNonNullElseGet(separator, () -> inferSeparator(path));
    }

    //==================================================================================================================
    // Object Implementation Methods
    //==================================================================================================================

    @Override
    public String toString() {
        return "%s[path=%s, separator=%s]".formatted(super.toString(), path, separator);
    }

    //==================================================================================================================
    // Private Helper Methods
    //==================================================================================================================

    /**
     * Infer the column separator to use for a given {@link Path}'s file.
     *
     * @param path The {@link Path} of the file to infer the column separator to use
     */
    private static String inferSeparator(Path path) {
        return inferSeparatorByExtension(path)
            .or(() -> inferSeparatorByFirstLine(path))
            .orElseGet(() -> Constants.SEPARATORS.get(Constants.UNKNOWN)); // Fall back to the default separator.
    }

    /**
     * Determine the column separator to use by using a given {@link Path}'s file extension.
     *
     * @param path The {@link Path} whose file extension is to be used to determine the column separator to use
     *
     * @see Constants#SEPARATORS
     */
    private static Optional<String> inferSeparatorByExtension(Path path) {
        return Optional
            .ofNullable(path.getFileName())
            .map(Path::toString)
            .filter(fileName -> fileName.contains("."))
            .map(fileName -> fileName.substring(fileName.lastIndexOf('.') + 1))
            .map(String::toLowerCase)
            .map(Constants.SEPARATORS::get);
    }

    /**
     * Determine the column separator to use by using a given {@link Path}'s file's first line, using the most common
     *   non-alphanumeric character occurrence.
     *
     * @param path The {@link Path} whose file's first line is to be used to determine the column separator to use
     */
    private static Optional<String> inferSeparatorByFirstLine(Path path) {
        try (var firstLine = TableFileReader.lines(path).limit(1L)) {
            return firstLine
                .flatMapToInt(String::codePoints)
                .filter(codePoint -> !(Character.isAlphabetic(codePoint) || Character.isDigit(codePoint)))
                .boxed()
                .collect(Collectors.groupingByConcurrent(Character::toString, Utils.countingCollector()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
        }
    }
}
