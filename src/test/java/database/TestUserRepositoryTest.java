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
}
