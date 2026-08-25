package database;

import database.model.TestUser;
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
@Feature("数据库约束")
public class TestUserRepositoryConstraintTest extends DatabaseTestBase {

    @Test(groups = "database")
    @Story("拒绝重复用户名")
    @Description("新增同名用户时，验证 MySQL 的唯一约束会拒绝重复数据")
    public void shouldRejectDuplicateUsername() {
        TestUser originalUser =
                TestUserTestDataFactory.createTemporaryUser();

        TestUser duplicateUser = new TestUser(
                originalUser.id() + 1,
                originalUser.username(),
                "tester"
        );

        TestUserRepository.create(originalUser);
        registerTemporaryUserForCleanup(originalUser.id());

        IllegalStateException exception = expectThrows(
                IllegalStateException.class,
                () -> TestUserRepository.create(duplicateUser)
        );

        assertEquals(
                exception.getMessage(),
                "新增测试用户失败：username=" + originalUser.username()
        );

        assertTrue(
                exception.getCause() instanceof SQLException,
                "底层应是数据库抛出的 SQL 异常"
        );

        SQLException sqlException = (SQLException) exception.getCause();

        assertEquals(
                sqlException.getSQLState(),
                "23000",
                "MySQL 应返回完整性约束冲突状态码"
        );
    }

    @Test(groups = "database")
    @Story("拒绝重复用户ID")
    @Description("新增不同用户名但相同ID的用户，验证MySQL主键约束会拒绝重复数据")
    public void shouldRejectDuplicateId() {
        TestUser originalUser =
                TestUserTestDataFactory.createTemporaryUser();
        TestUser duplicateIdUser =
                TestUserTestDataFactory.createTemporaryUser();

        TestUser duplicateUser = new TestUser(
                originalUser.id(),
                duplicateIdUser.username(),
                duplicateIdUser.role()
        );

        TestUserRepository.create(originalUser);
        registerTemporaryUserForCleanup(originalUser.id());

        IllegalStateException exception = expectThrows(
                IllegalStateException.class,
                () -> TestUserRepository.create(duplicateUser)
        );

        assertTrue(
                exception.getCause() instanceof SQLException,
                "底层应是数据库抛出的 SQL 异常"
        );

        SQLException sqlException = (SQLException) exception.getCause();

        assertEquals(
                sqlException.getSQLState(),
                "23000",
                "MySQL 应返回完整性约束冲突状态码"
        );
    }
}
