package database;

import database.model.TestUserAddress;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class TestUserAddressRepositoryValidationTest {

    @DataProvider(name = "invalidAddresses")
    public Object[][] invalidAddresses() {
        return new Object[][]{
                {null, "用户地址不能为空"},
                {
                        new TestUserAddress(
                                0L,
                                1L,
                                "家庭",
                                "有效测试地址"
                        ),
                        "用户地址id必须大于0"
                },
                {
                        new TestUserAddress(
                                1L,
                                0L,
                                "家庭",
                                "有效测试地址"
                        ),
                        "用户地址的userId必须大于0"
                },
                {
                        new TestUserAddress(
                                1L,
                                1L,
                                " ",
                                "有效测试地址"
                        ),
                        "用户地址标签不能为空"
                },
                {
                        new TestUserAddress(
                                1L,
                                1L,
                                "家庭",
                                " "
                        ),
                        "用户地址内容不能为空"
                }
        };
    }

    @Test(dataProvider = "invalidAddresses", groups = "database")
    public void shouldRejectInvalidAddressBeforeAccessingDatabase(
            TestUserAddress address,
            String expectedMessage
    ) {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> TestUserAddressRepository.create(address)
        );

        assertEquals(exception.getMessage(), expectedMessage);
    }
}
