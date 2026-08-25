package database;

import database.model.TestUser;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

@Epic("数据库测试")
@Feature("测试数据生命周期")
public class TestUserRepositoryCrudTest extends DatabaseTestBase {

    @Test(groups = "database")
    @Story("新增并查询临时用户")
    @Description("新增唯一测试用户，查询验证后由AfterMethod清理")
    public void shouldCreateAndFindTemporaryUser() {
        TestUser expectedUser = createTemporaryUser();

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

    @Test(groups = "database")
    @Story("按ID查询临时用户")
    @Description("新增临时用户后按主键ID查询，验证数据正确返回")
    public void shouldCreateAndFindTemporaryUserById() {
        TestUser expectedUser = createTemporaryUser();

        TestUser actualUser = TestUserRepository
                .findById(expectedUser.id())
                .orElseThrow(
                        () -> new AssertionError(
                                "新增后未按ID查询到临时用户："
                                        + expectedUser.id()
                        )
                );

        assertEquals(actualUser, expectedUser);
    }

    @Test(groups = "database")
    @Story("更新临时用户角色")
    @Description("新增临时用户后更新角色，验证数据库保存更新结果")
    public void shouldUpdateTemporaryUserRole() {
        TestUser temporaryUser = createTemporaryUser();
        String updatedRole = "updated";

        TestUserRepository.updateRole(
                temporaryUser.id(),
                updatedRole
        );

        TestUser updatedUser = TestUserRepository
                .findByUsername(temporaryUser.username())
                .orElseThrow(
                        () -> new AssertionError(
                                "更新后未查询到临时用户："
                                        + temporaryUser.username()
                        )
                );

        assertEquals(updatedUser.id(), temporaryUser.id());
        assertEquals(updatedUser.username(), temporaryUser.username());
        assertEquals(updatedUser.role(), updatedRole);
    }

    @Test(groups = "database")
    @Story("按角色创建临时用户")
    @Description("创建指定角色的临时用户，验证角色正确保存")
    public void shouldCreateTemporaryUserWithSpecifiedRole() {
        TestUser expectedUser = createTemporaryUser("tester");

        TestUser actualUser = TestUserRepository
                .findByUsername(expectedUser.username())
                .orElseThrow(
                        () -> new AssertionError(
                                "未查询到指定角色的临时用户："
                                        + expectedUser.username()
                        )
                );

        assertEquals(actualUser.role(), expectedUser.role());
    }

    @Test(groups = "database")
    @Story("保存最大长度用户名")
    @Description("新增恰好50个字符的用户名，验证数据库可以正常保存和查询")
    public void shouldCreateUserWithFiftyCharacterUsername() {
        String maxLengthUsername = TestUserTestDataFactory
                .createUsernameWithLength(50);
        TestUser expectedUser = TestUserTestDataFactory
                .createTemporaryUser(maxLengthUsername, "temporary");

        Allure.parameter("边界用户名长度", maxLengthUsername.length());

        TestUserRepository.create(expectedUser);
        registerTemporaryUserForCleanup(expectedUser.id());

        TestUser actualUser = TestUserRepository
                .findByUsername(maxLengthUsername)
                .orElseThrow(
                        () -> new AssertionError(
                                "新增后未查询到50字符用户名："
                                        + maxLengthUsername
                        )
                );

        assertEquals(actualUser, expectedUser);
    }

    @Test(groups = "database")
    @Story("拒绝超长用户名")
    @Description("新增51个字符的用户名，验证MySQL严格模式拒绝超出字段长度的数据")
    public void shouldRejectUserWithFiftyOneCharacterUsername() {
        String tooLongUsername = TestUserTestDataFactory
                .createUsernameWithLength(51);
        TestUser invalidUser = TestUserTestDataFactory
                .createTemporaryUser(tooLongUsername, "temporary");

        Allure.parameter("超长用户名长度", tooLongUsername.length());

        try {
            IllegalStateException exception = expectThrows(
                    IllegalStateException.class,
                    () -> TestUserRepository.create(invalidUser)
            );

            assertTrue(
                    exception.getCause() instanceof SQLException,
                    "底层应是数据库抛出的 SQL 异常"
            );

            SQLException sqlException =
                    (SQLException) exception.getCause();

            assertEquals(
                    sqlException.getSQLState(),
                    "22001",
                    "超出 VARCHAR(50) 长度时应返回数据截断状态码"
            );

            assertTrue(
                    TestUserRepository
                            .findByUsername(tooLongUsername)
                            .isEmpty(),
                    "插入被拒绝后不应留下临时用户"
            );
        } finally {
            TestUserRepository.deleteById(invalidUser.id());
        }
    }

    @Test(groups = "database")
    @Story("删除临时用户")
    @Description("新增临时用户后显式删除，验证删除影响1行且查询不到该用户")
    public void shouldDeleteTemporaryUser() {
        TestUser temporaryUser =
                TestUserTestDataFactory.createTemporaryUser();

        try {
            TestUserRepository.create(temporaryUser);

            int deletedRows = TestUserRepository
                    .deleteById(temporaryUser.id());

            assertEquals(deletedRows, 1);
            assertTrue(
                    TestUserRepository
                            .findByUsername(temporaryUser.username())
                            .isEmpty(),
                    "删除后不应该查询到临时用户"
            );
        } finally {
            TestUserRepository.deleteById(temporaryUser.id());
        }
    }

    private TestUser createTemporaryUser() {
        return createTemporaryUser("temporary");
    }

    private TestUser createTemporaryUser(String role) {
        TestUser user = TestUserTestDataFactory.createTemporaryUser(role);

        Allure.parameter("临时用户ID", user.id());
        Allure.parameter("临时用户名", user.username());
        Allure.parameter("临时用户角色", user.role());

        TestUserRepository.create(user);
        registerTemporaryUserForCleanup(user.id());

        return user;
    }
}
