package validation;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class UserIdValidatorTest {

    @Test
    public void acceptsPositiveUserIdTest() {
        UserIdValidator.validate(1L);
    }

    @Test(dataProvider = "invalidUserIds")
    public void rejectsNonPositiveUserIdTest(long userId) {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> UserIdValidator.validate(userId)
        );

        assertEquals(exception.getMessage(), "用户ID必须大于0");
    }

    @DataProvider
    public Object[][] invalidUserIds() {
        return new Object[][]{
                {0L},
                {-1L}
        };
    }
}
