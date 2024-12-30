import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class reads a tabular data file lazily into a {@link Stream} of columns as string arrays.
 *
 * @apiNote {@link Stream}s created by instances of this class must be used with a
 *          <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">try-with-resources
 *          statement</a> to ensure any underlying resources are properly closed.
 */
non-sealed class TableFileReader extends AbstractTableFileProcessor implements TableSupplier {
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
     *                  {@link TableFileReader} to infer this information based on {@code path}'s file characteristics
     * @param hasHeaderRow Whether the file being read from {@code path} has a header row; may be {@code null} to allow
     *                     {@link TableFileReader} to infer this information as the first line of the file is read
     */
    TableFileReader(Path path, String separator, Boolean hasHeaderRow) {
        super(path, separator);
        this.hasHeaderRow = Objects.requireNonNullElseGet(hasHeaderRow, () -> hasHeaderRow(path));
    }

    //==================================================================================================================
    // Supplier Implementation Methods
    //==================================================================================================================

    /**
     * @implNote This method returns a lazy {@link Stream}.
     */
    @Override
    public Stream<String[]> get() {
        Loggers.INFO.accept("[>] Reading file: " + path);

        return lines(path)
            .skip(hasHeaderRow ? 1L : 0L) // Skip the first header row if explicitly specified.
            .map(Settings.FAST ? line -> line.split(separator) : line -> line.strip().split(separator));
    }

    //==================================================================================================================
    // Iterable Implementation Methods
    //==================================================================================================================

    @Override
    public Spliterator<String[]> spliterator() {
        // Use Files.lines()'s spliterator directly rather than wrapping over the stream and its iterator/spliterator.
        return get().spliterator();
    }

    //==================================================================================================================
    // Package-private Helper Methods
    //==================================================================================================================

    /**
     * Read a given {@link Path}'s file lazily into a {@link Stream} of non-empty lines.
     *
     * @param path The {@link Path} of the file to read lazily into a {@link Stream} of non-empty lines
     *
     * @see Files#lines(Path)
     */
    @SuppressWarnings("resource") // The stream must be closed by the caller.
    static Stream<String> lines(Path path) {
        try {
            final var lines = Files.lines(path);
            return Settings.FAST ? lines : lines.filter(Predicate.not(String::isEmpty));
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to open file for reading: " + path, exception);
        }
    }

    //==================================================================================================================
    // Private Helper Methods
    //==================================================================================================================

    /**
     * Determine whether a given {@link Path}'s file has a header row by comparing its first line's character types.
     *
     * @param path The {@link Path} of the file to read the first line of to determine whether it is the header row
     */
    private static boolean hasHeaderRow(Path path) {
        try (var firstLine = lines(path).limit(1L)) {
            return firstLine.allMatch(TableFileReader::isHeaderRow);
        }
    }

    /**
     * Determine whether a given line is a header row by comparing its character types.
     *
     * @param line The line of a file to determine whether it is the header row
     */
    private static boolean isHeaderRow(String line) {
        return line
            .codePoints()
            .boxed()
            .collect(Collectors.teeing(
                Collectors.filtering(Character::isAlphabetic, Collectors.counting()),
                Collectors.filtering(Character::isDigit, Collectors.counting()),
                TableFileReader::isHeaderRow
            ));
    }

    /**
     * Determine whether a line in a file is a header row by comparing its given
     *   {@linkplain Character#isAlphabetic(int) letter} and {@linkplain Character#isDigit(int) digit} counts.
     * <br/><br/>
     *
     * We use rudimentary but intuitive logic that, for the first {@code n} consecutive lines in a file, if its number
     *   of {@linkplain Character#isAlphabetic(int) letters} far outweigh its number of
     *   {@linkplain Character#isDigit(int) digits}, it would be considered a header row.
     *
     * @param letters The number of {@linkplain Character#isAlphabetic(int) letters} in the line of a file
     * @param digits The number of {@linkplain Character#isDigit(int) digits} in the line of a file
     */
    private static boolean isHeaderRow(long letters, long digits) {
        return letters >= digits * 5L;
    }
}
