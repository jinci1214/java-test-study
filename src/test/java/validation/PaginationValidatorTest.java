package validation;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class PaginationValidatorTest {

    @Test
    public void acceptsPositivePageAndPageSizeTest() {
        PaginationValidator.validate(1, 20);
    }

    @Test(dataProvider = "invalidPagingData")
    public void rejectsInvalidPagingTest(
            int page,
            int pageSize,
            String expectedMessage
    ) {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> PaginationValidator.validate(page, pageSize)
        );

        assertEquals(exception.getMessage(), expectedMessage);
    }

    @DataProvider
    public Object[][] invalidPagingData() {
        return new Object[][]{
                {0, 20, "页码必须大于0"},
                {1, 0, "每页数量必须大于0"}
        };
    }
}
