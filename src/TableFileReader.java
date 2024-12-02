import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class reads a tabular data file lazily into a {@link Stream} of columns as string arrays.
 * <br/><br/>
 *
 * <b>Note:</b> {@link Stream}s created by instances of this class must be used with a
 *   <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">try-with-resources
 *   statement</a> to ensure any underlying resources are properly closed.
 */
class TableFileReader implements TableSupplier {
    private final Path path;
    private final String separator;
    private final boolean hasHeaderRow;

    //==================================================================================================================
    // Constructors
    //==================================================================================================================

    /**
     * Construct an instance of {@link TableFileReader} that reads from a given {@link Path}, inferring the column
     *   separator and skipping the header row if possible.
     *
     * @param path The {@link Path} that the constructed {@link TableFileReader} will read from
     */
    TableFileReader(Path path) {
        this(path, null, null);
    }

    /**
     * Construct an instance of {@link TableFileReader} that reads from a given {@link Path}, using a given column
     *   separator and skipping the header row if necessary.
     *
     * @param path The {@link Path} that the constructed {@link TableFileReader} will read from
     * @param separator The column separator to use for splitting the lines into columns; may be {@code null} to allow
     *                  {@link TableFileReader} to infer this information based on {@code path}'s file extension
     * @param hasHeaderRow Whether the file being read from {@code path} has a header row; may be {@code null} to allow
     *                     {@link TableFileReader} to infer this information as the first line of the file is read
     */
    TableFileReader(Path path, String separator, Boolean hasHeaderRow) {
        this.path = path;
        this.separator = Pattern.quote(Objects.requireNonNullElseGet(separator, () -> inferSeparator(path)));
        this.hasHeaderRow = Objects.requireNonNullElseGet(hasHeaderRow, () -> hasHeaderRow(path));
    }

    //==================================================================================================================
    // Supplier Implementation Methods
    //==================================================================================================================

    @Override
    public Stream<String[]> get() {
        System.out.println("Reading file: " + path);

        return lines(path)
            .skip(hasHeaderRow ? 1L : 0L) // We may use Stream.dropWhile() in the future to skip multiple header rows.
            .map(line -> line.split(separator));
    }

    //==================================================================================================================
    // Package-private Helper Methods
    //==================================================================================================================

    /**
     * Infer the column separator to use for a given {@link Path}'s file.
     *
     * @param path The {@link Path} of the file to infer the column separator to use
     */
    static String inferSeparator(Path path) {
        return inferSeparatorByExtension(path)
            .or(() -> inferSeparatorByFirstLine(path))
            .orElseGet(() -> Constants.SEPARATORS.get(Constants.UNKNOWN));
    }

    //==================================================================================================================
    // Private Helper Methods
    //==================================================================================================================

    /**
     * Read a given {@link Path}'s file lazily into a {@link Stream} of non-empty lines.
     *
     * @param path The {@link Path} of the file to read lazily into a {@link Stream} of non-empty lines
     *
     * @see Files#lines(Path)
     */
    @SuppressWarnings("resource") // The stream must be closed by the caller.
    private static Stream<String> lines(Path path) {
        try {
            return Files
                .lines(path)
                .filter(Predicate.not(String::isEmpty));
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to open file for reading: " + path, exception);
        }
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
        try (var firstLine = lines(path).limit(1L)) {
            return firstLine
                .flatMapToInt(String::codePoints)
                .filter(codePoint -> !(Character.isAlphabetic(codePoint) || Character.isDigit(codePoint)))
                .boxed()
                .collect(Collectors.groupingByConcurrent(Character::toString, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .findAny()
                .map(Map.Entry::getKey);
        }
    }

    /**
     * Determine whether the given first line of a given {@link Path}'s file is the header row by comparing its
     *   character types.
     *
     * @param path The {@link Path} of the file to read the first line of to determine whether it is the header row
     */
    private static boolean hasHeaderRow(Path path) {
        try (var firstLine = lines(path).limit(1L)) {
            return firstLine
                .flatMapToInt(String::codePoints)
                .boxed()
                .collect(Collectors.teeing(
                    Collectors.filtering(Character::isAlphabetic, Collectors.counting()),
                    Collectors.filtering(Character::isDigit, Collectors.counting()),
                    (letters, digits) -> letters >= digits // Rudimentary but intuitive logic
                ));
        }
    }
}
