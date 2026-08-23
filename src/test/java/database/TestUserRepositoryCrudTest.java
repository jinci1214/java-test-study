package database;

import database.model.TestUser;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Epic("数据库测试")
@Feature("测试数据生命周期")
public class TestUserRepositoryCrudTest extends DatabaseTestBase {

    private long temporaryUserId;

    @Test(groups = "database")
    @Story("新增并查询临时用户")
    @Description("新增唯一测试用户，查询验证后由AfterMethod清理")
    public void shouldCreateAndFindTemporaryUser() {
        temporaryUserId = System.currentTimeMillis();
        TestUser expectedUser = new TestUser(
                temporaryUserId,
                "db-test-" + temporaryUserId,
                "temporary"
        );

        Allure.parameter("临时用户ID", temporaryUserId);
        Allure.parameter("临时用户名", expectedUser.username());

        TestUserRepository.create(expectedUser);
        registerTemporaryUserForCleanup(temporaryUserId);

        TestUser actualUser = TestUserRepository
                .findByUsername(expectedUser.username())
                .orElseThrow(
                        () -> new AssertionError(
                                "新增后未查询到临时用户："
                                        + expectedUser.username()
                        )
                );

        assertEquals(actualUser.id(), expectedUser.id());
        assertEquals(actualUser.username(), expectedUser.username());
        assertEquals(actualUser.role(), expectedUser.role());
    }
}
