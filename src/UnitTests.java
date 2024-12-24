import java.util.List;
import java.util.SequencedCollection;

public class UnitTests extends BaseUnitTest {
    private static final SequencedCollection<Class<? extends BaseUnitTest>> TESTS =
        List.of(
            BaseUnitTestTest.class,
            UtilsTest.class,
            ProtocolTest.class,
            IANAProtocolsTest.class,
            TagsTest.class,
            TableConsumerTest.class,
            TableSupplierTest.class,
            TableMapTest.class,
            AbstractTableFileProcessorTest.class,
            TableFileReaderTest.class,
            TableFileWriterTest.class,
            TableGeneratorTest.class,
            FlowLogGeneratorTest.class,
            FlowLogProcessorTest.class
        );

    //==================================================================================================================
    // Bootstrap
    //==================================================================================================================

    public static void main(String[] args) {
        TESTS.forEach(BaseUnitTest::run);
    }
}
