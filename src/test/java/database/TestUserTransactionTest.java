package database;

import database.model.TestUser;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Epic("数据库测试")
@Feature("事务数据隔离")
public class TestUserTransactionTest extends DatabaseTestBase {

    @Test(groups = "database")
    @Story("回滚临时用户")
    @Description("在事务内新增并查询临时用户，测试结束后回滚数据")
    public void shouldRollbackTemporaryUser() throws SQLException {
        TestUser temporaryUser = TestUserTestDataFactory.createTemporaryUser();

        Allure.parameter("事务临时用户ID", temporaryUser.id());

        try (Connection connection = JdbcUtil.getConnection()) {
            connection.setAutoCommit(false);

            try {
                TestUserRepository.create(connection, temporaryUser);

                TestUser actualUser = TestUserRepository
                        .findByUsername(connection, temporaryUser.username())
                        .orElseThrow(
                                () -> new AssertionError(
                                        "事务内未查询到临时用户"
                                )
                        );

                assertEquals(actualUser, temporaryUser);
            } finally {
                connection.rollback();
            }
        }

        assertTrue(
                TestUserRepository
                        .findByUsername(temporaryUser.username())
                        .isEmpty(),
                "事务回滚后，数据库中不应该保留临时用户"
        );
    }
}
