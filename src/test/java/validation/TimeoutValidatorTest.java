package validation;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class TimeoutValidatorTest {

    @Test
    public void acceptsPositiveTimeoutTest() {
        TimeoutValidator.validate(100);
    }

    @Test(dataProvider = "invalidTimeouts")
    public void rejectsNonPositiveTimeoutTest(int timeoutMillis) {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> TimeoutValidator.validate(timeoutMillis)
        );

        assertEquals(exception.getMessage(), "超时时间必须是正数");
    }

    @DataProvider
    public Object[][] invalidTimeouts() {
        return new Object[][]{
                {0},
                {-1}
        };
    }
}
