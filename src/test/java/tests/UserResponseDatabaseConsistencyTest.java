package tests;

import api.UserApi;
import base.BaseTest;
import config.DatabaseConfig;
import database.TestUserRepository;
import database.model.TestUser;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.SkipException;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * 验证 Mock 接口响应与 MySQL 测试基线数据一致。
 *
 * <p>WireMock 本身不写入 MySQL，因此这里验证的是接口响应契约与
 * 测试数据的一致性；接入真实服务后，才能进一步验证真实落库结果。</p>
 */
@Epic("接口与数据库联合测试")
@Feature("用户信息一致性")
public class UserResponseDatabaseConsistencyTest extends BaseTest {

    @Test(groups = "database")
    @Story("查询用户后校验数据库")
    @Description("验证用户接口返回的id、用户名和角色与MySQL数据一致")
    public void shouldMatchUserResponseWithDatabase() {
        if (!DatabaseConfig.hasPassword()) {
            throw new SkipException(
                    "未配置 DB_PASSWORD，跳过接口与数据库联合测试"
            );
        }

        Response response = UserApi.getUser();

        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getInt("code"), 0);

        int apiUserId = response.jsonPath().getInt("data.id");
        String apiUsername = response.jsonPath()
                .getString("data.username");
        String apiRole = response.jsonPath()
                .getString("data.role");

        assertNotNull(apiUsername, "接口响应中的用户名不能为空");
        assertNotNull(apiRole, "接口响应中的角色不能为空");

        TestUser databaseUser = TestUserRepository
                .findByUsername(apiUsername)
                .orElseThrow(
                        () -> new AssertionError(
                                "数据库中不存在用户：" + apiUsername
                        )
                );

        Allure.parameter("接口用户名", apiUsername);
        Allure.addAttachment("数据库用户", databaseUser.toString());

        assertEquals((long) apiUserId, databaseUser.id());
        assertEquals(apiUsername, databaseUser.username());
        assertEquals(apiRole, databaseUser.role());
    }
}
