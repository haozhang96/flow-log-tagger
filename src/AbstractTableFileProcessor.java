import java.io.UncheckedIOException;
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
        this.separator = Objects.requireNonNullElseGet(separator, this::inferSeparator);
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
     * Infer the column separator to use for this {@link AbstractTableFileProcessor}'s file.
     */
    private String inferSeparator() {
        return inferSeparatorByExtension()
            .or(this::inferSeparatorByFirstLine)
            .orElseGet(() -> Constants.SEPARATORS.get(Constants.UNKNOWN)); // Fall back to the default separator.
    }

    /**
     * Determine the column separator to use by using this {@link AbstractTableFileProcessor}'s {@link Path}'s
     *   extension.
     *
     * @see Constants#SEPARATORS
     */
    private Optional<String> inferSeparatorByExtension() {
        return Optional
            .ofNullable(path.getFileName())
            .map(Path::toString)
            .filter(fileName -> fileName.contains("."))
            .map(fileName -> fileName.substring(fileName.lastIndexOf('.') + 1))
            .map(String::toLowerCase)
            .map(Constants.SEPARATORS::get);
    }

    /**
     * Determine the column separator to use by using this {@link AbstractTableFileProcessor}'s file's first line, using
     *   the most common non-alphanumeric character occurrence.
     */
    private Optional<String> inferSeparatorByFirstLine() {
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
        } catch (UncheckedIOException exception) {
            // Do not fail if we can't read the file to infer the separator; this is an optional feature.
            return Optional.empty();
        }
    }
}
