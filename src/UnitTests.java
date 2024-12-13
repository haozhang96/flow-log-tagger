import java.util.Collection;
import java.util.List;

class UnitTests extends BaseUnitTest {
    private static final Collection<Class<? extends BaseUnitTest>> UNIT_TEST_CLASSES =
        List.of(
            BaseUnitTestTest.class,
            AbstractTableFileProcessorTest.class,
            TableConsumerTest.class
        );

    //==================================================================================================================
    // Bootstrap
    //==================================================================================================================

    public static void main(String[] args) {
        UNIT_TEST_CLASSES.forEach(BaseUnitTest::run);
    }
}
