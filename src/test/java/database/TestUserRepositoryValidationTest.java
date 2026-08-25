package database;

import database.model.TestUser;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class TestUserRepositoryValidationTest {

    @Test(groups = "database")
    public void shouldRejectBlankRoleInTestDataFactory() {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> TestUserTestDataFactory.createTemporaryUser("  ")
        );

        assertEquals(exception.getMessage(), "测试用户角色不能为空");
    }

    @Test(groups = "database")
    public void shouldRejectBlankUsernameInTestDataFactory() {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> TestUserTestDataFactory.createTemporaryUser(
                        " ",
                        "temporary"
                )
        );

        assertEquals(exception.getMessage(), "测试用户名不能为空");
    }

    @Test(groups = "database")
    public void shouldCreateTemporaryUserWithSpecifiedUsername() {
        TestUser user = TestUserTestDataFactory.createTemporaryUser(
                "custom-username",
                "tester"
        );

        assertTrue(user.id() > 0, "测试用户ID必须为正数");
        assertEquals(user.username(), "custom-username");
        assertEquals(user.role(), "tester");
    }

    @Test(groups = "database")
    public void shouldRejectNonPositiveIdBeforeAccessingDatabase() {
        TestUser invalidUser = new TestUser(
                0L,
                "valid-username",
                "tester"
        );

        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> TestUserRepository.create(invalidUser)
        );

        assertEquals(exception.getMessage(), "测试用户id必须大于0");
    }

    @Test(groups = "database")
    public void shouldRejectBlankRoleBeforeAccessingDatabase() {
        TestUser invalidUser = new TestUser(
                1L,
                "valid-username",
                " "
        );

        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> TestUserRepository.create(invalidUser)
        );

        assertEquals(exception.getMessage(), "测试用户角色不能为空");
    }

    @Test(groups = "database")
    public void shouldRejectBlankUsernameWhenQueryingBeforeAccessingDatabase() {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> TestUserRepository.findByUsername(" ")
        );

        assertEquals(exception.getMessage(), "查询用户名不能为空");
    }

    @Test(groups = "database")
    public void shouldRejectNonPositiveIdWhenQueryingBeforeAccessingDatabase() {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> TestUserRepository.findById(0L)
        );

        assertEquals(exception.getMessage(), "查询用户的id必须大于0");
    }

    @Test(groups = "database")
    public void shouldCreateUsernameWithSpecifiedLength() {
        String username = TestUserTestDataFactory
                .createUsernameWithLength(50);

        assertEquals(username.length(), 50);
    }

    @Test(groups = "database")
    public void shouldRejectNonPositiveUsernameLength() {
        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> TestUserTestDataFactory.createUsernameWithLength(0)
        );

        assertEquals(exception.getMessage(), "测试用户名长度必须大于0");
    }

    @Test(groups = "database")
    public void shouldRejectBlankUsernameBeforeAccessingDatabase() {
        TestUser invalidUser = new TestUser(
                1L,
                " ",
                "tester"
        );

        IllegalArgumentException exception = expectThrows(
                IllegalArgumentException.class,
                () -> TestUserRepository.create(invalidUser)
        );

        assertEquals(exception.getMessage(), "测试用户名不能为空");
    }
}
