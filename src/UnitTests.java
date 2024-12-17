import java.util.Collection;
import java.util.Set;

public class UnitTests extends BaseUnitTest {
    private static final Collection<Class<? extends BaseUnitTest>> UNIT_TEST_CLASSES =
        Set.of(
            BaseUnitTestTest.class,
            TableConsumerTest.class
        );

    //==================================================================================================================
    // Bootstrap
    //==================================================================================================================

    public static void main(String[] args) {
        UNIT_TEST_CLASSES
            .parallelStream()
            .forEach(BaseUnitTest::run);
    }
}
