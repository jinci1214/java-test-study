package database;

import database.model.TestUserProfile;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class TestUserProfileRepositoryValidationTest {

    @DataProvider(name = "invalidProfiles")
    public Object[][] invalidProfiles() {
        return new Object[][]{
                {null, "用户资料不能为空"},
                {
                        new TestUserProfile(
                                0L,
                                "有效昵称",
                                "valid@example.test"
                        ),
                        "用户资料的userId必须大于0"
                },
                {
                        new TestUserProfile(
                                1L,
                                " ",
                                "valid@example.test"
                        ),
                        "用户昵称不能为空"
                },
                {
                        new TestUserProfile(
                                1L,
                                "有效昵称",
                                " "
                        ),
                        "用户邮箱不能为空"
                }
        };
    }

    @Test(dataProvider = "invalidProfiles", groups = "database")
    public void shouldRejectInvalidProfileBeforeAccessingDatabase(
            TestUserProfile profile,
            String expectedMessage
    ) {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> TestUserProfileRepository.create(profile)
        );

        assertEquals(exception.getMessage(), expectedMessage);
    }
}
