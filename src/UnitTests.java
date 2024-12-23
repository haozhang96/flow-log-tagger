import java.util.Collection;
import java.util.List;

public class UnitTests extends BaseUnitTest {
    private static final Collection<Class<? extends BaseUnitTest>> TARGETS =
        List.of(
            BaseUnitTestTest.class,
            TableConsumerTest.class,
            TableSupplierTest.class,
            TableMapTest.class,
            AbstractTableFileProcessorTest.class,
            TableFileReaderTest.class,
            TableFileWriterTest.class,
            TableGeneratorTest.class,
            ProtocolTest.class,
            IANAProtocolsTest.class,
            TagsTest.class,
            UtilsTest.class,
            FlowLogGeneratorTest.class
        );

    //==================================================================================================================
    // Bootstrap
    //==================================================================================================================

    public static void main(String[] args) {
        TARGETS.forEach(BaseUnitTest::run);
    }
}
