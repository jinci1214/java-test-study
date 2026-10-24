package database;

import database.model.TestUser;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

@Epic("数据库测试")
@Feature("批量操作与事务原子性")
public class TestUserBatchTransactionTest extends DatabaseTestBase {

    @Test(groups = "database")
    @Story("批量新增成功后提交")
    @Description("同一事务批量新增两个用户，提交后两条数据都应存在")
    public void shouldCommitAllUsersWhenBatchSucceeds()
            throws SQLException {
        TestUser firstUser = TestUserTestDataFactory
                .createTemporaryUser("batch-user");
        TestUser secondUser = TestUserTestDataFactory
                .createTemporaryUser("batch-user");
        List<TestUser> users = List.of(firstUser, secondUser);

        try (Connection connection = JdbcUtil.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int[] affectedRows = TestUserRepository.createBatch(
                        connection,
                        users
                );
                assertEquals(
                        affectedRows.length,
                        users.size(),
                        "批量执行结果数量应与用户数量一致"
                );

                connection.commit();
                registerTemporaryUserForCleanup(firstUser.id());
                registerTemporaryUserForCleanup(secondUser.id());
            } finally {
                connection.rollback();
            }
        }

        assertEquals(
                TestUserRepository
                        .findByUsername(firstUser.username())
                        .orElseThrow(),
                firstUser
        );
        assertEquals(
                TestUserRepository
                        .findByUsername(secondUser.username())
                        .orElseThrow(),
                secondUser
        );
    }

    @Test(groups = "database")
    @Story("批量新增中途失败后回滚")
    @Description("批次中出现重复用户名时，回滚后本批次不应留下任何新用户")
    public void shouldRollbackAllUsersWhenBatchFails()
            throws SQLException {
        TestUser existingUser = TestUserTestDataFactory
                .createTemporaryUser("existing-user");
        TestUser firstBatchUser = TestUserTestDataFactory
                .createTemporaryUser("batch-user");
        TestUser duplicateUsernameUser = new TestUser(
                TestUserTestDataFactory.createTemporaryUser().id(),
                existingUser.username(),
                "batch-user"
        );

        TestUserRepository.create(existingUser);
        registerTemporaryUserForCleanup(existingUser.id());

        try (Connection connection = JdbcUtil.getConnection()) {
            connection.setAutoCommit(false);

            try {
                IllegalStateException exception = expectThrows(
                        IllegalStateException.class,
                        () -> TestUserRepository.createBatch(
                                connection,
                                List.of(
                                        firstBatchUser,
                                        duplicateUsernameUser
                                )
                        )
                );

                assertTrue(
                        exception.getCause() instanceof SQLException,
                        "批量写入失败的底层原因应为SQLException"
                );
                SQLException sqlException =
                        (SQLException) exception.getCause();
                assertEquals(
                        sqlException.getSQLState(),
                        "23000",
                        "重复用户名应触发完整性约束冲突"
                );
            } finally {
                connection.rollback();
            }
        }

        assertTrue(
                TestUserRepository
                        .findByUsername(firstBatchUser.username())
                        .isEmpty(),
                "事务回滚后，批次中原本成功的数据也不应保留"
        );
        assertEquals(
                TestUserRepository
                        .findByUsername(existingUser.username())
                        .orElseThrow(),
                existingUser,
                "事务外原本存在的用户不应受回滚影响"
        );
    }
}
