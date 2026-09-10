package validation;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class UsernameSortOrderValidatorTest {

    @Test(dataProvider = "validSortOrders")
    public void acceptsSupportedSortOrderTest(String sortOrder) {
        UsernameSortOrderValidator.validate(sortOrder);
    }

    @Test(dataProvider = "invalidSortOrders")
    public void rejectsUnsupportedSortOrderTest(String sortOrder) {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> UsernameSortOrderValidator.validate(sortOrder)
        );

        assertEquals(exception.getMessage(), "用户名排序方向只能是 asc 或 desc");
    }

    @DataProvider
    public Object[][] validSortOrders() {
        return new Object[][]{
                {"asc"},
                {"desc"}
        };
    }

    @DataProvider
    public Object[][] invalidSortOrders() {
        return new Object[][]{
                {null},
                {"ascending"}
        };
    }
}
