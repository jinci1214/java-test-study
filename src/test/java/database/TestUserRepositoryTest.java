package database;

import database.model.TestUser;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

@Epic("数据库测试")
@Feature("用户数据查询")
public class TestUserRepositoryTest extends DatabaseTestBase {

    @Test(groups = "database")
    @Story("查询测试用户")
    @Description("按用户名查询 test_users 表，验证测试数据正确")
    public void shouldFindAdminUser() {
        Optional<TestUser> optionalUser =
                TestUserRepository.findByUsername("admin");

        assertTrue(
                optionalUser.isPresent(),
                "数据库中应该存在 admin 测试用户"
        );

        TestUser user = optionalUser.get();

        assertEquals(user.id(), 1L);
        assertEquals(user.username(), "admin");
        assertEquals(user.role(), "tester");
    }

    @Test(groups = "database")
    @Story("查询不存在的测试用户")
    @Description("查询从未插入过的随机用户名，验证Repository返回空结果")
    public void shouldReturnEmptyWhenUsernameDoesNotExist() {
        TestUser notCreatedUser =
                TestUserTestDataFactory.createTemporaryUser();

        Optional<TestUser> optionalUser = TestUserRepository
                .findByUsername(notCreatedUser.username());

        assertTrue(
                optionalUser.isEmpty(),
                "未创建的随机用户名不应该查询到用户"
        );
    }

    @Test(groups = "database")
    @Story("按ID查询不存在的测试用户")
    @Description("查询从未插入过的随机ID，验证Repository返回空结果")
    public void shouldReturnEmptyWhenIdDoesNotExist() {
        TestUser notCreatedUser =
                TestUserTestDataFactory.createTemporaryUser();

        Optional<TestUser> optionalUser = TestUserRepository
                .findById(notCreatedUser.id());

        assertTrue(
                optionalUser.isEmpty(),
                "未创建的随机ID不应该查询到用户"
        );
    }

    @Test(groups = "database")
    @Story("拒绝更新不存在的测试用户")
    @Description("更新从未插入过的随机用户，验证Repository拒绝影响0行的更新")
    public void shouldRejectUpdatingUserThatDoesNotExist() {
        TestUser notCreatedUser =
                TestUserTestDataFactory.createTemporaryUser();

        IllegalStateException exception = expectThrows(
                IllegalStateException.class,
                () -> TestUserRepository.updateRole(
                        notCreatedUser.id(),
                        "updated"
                )
        );

        assertEquals(
                exception.getMessage(),
                "更新测试用户失败，影响行数：0，id="
                        + notCreatedUser.id()
        );
    }

    @Test(groups = "database")
    @Story("删除不存在的测试用户")
    @Description("删除从未插入过的随机用户，验证Repository返回影响0行")
    public void shouldReturnZeroWhenDeletingUserDoesNotExist() {
        TestUser notCreatedUser =
                TestUserTestDataFactory.createTemporaryUser();

        int deletedRows = TestUserRepository
                .deleteById(notCreatedUser.id());

        assertEquals(deletedRows, 0);
    }
}
