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
import java.sql.Statement;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

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

    @Test(groups = "database")
    @Story("读未提交允许脏读")
    @Description("写连接尚未提交时，READ_UNCOMMITTED读取连接可以看到临时用户")
    public void shouldAllowDirtyReadUnderReadUncommitted()
            throws SQLException {
        TestUser temporaryUser = TestUserTestDataFactory
                .createTemporaryUser();

        Allure.parameter("隔离级别", "READ_UNCOMMITTED");
        Allure.parameter("事务临时用户ID", temporaryUser.id());

        try (
                Connection writer = JdbcUtil.getConnection();
                Connection reader = JdbcUtil.getConnection()
        ) {
            writer.setAutoCommit(false);
            reader.setTransactionIsolation(
                    Connection.TRANSACTION_READ_UNCOMMITTED
            );
            reader.setAutoCommit(false);

            try {
                TestUserRepository.create(writer, temporaryUser);

                assertTrue(
                        TestUserRepository
                                .findByUsername(
                                        reader,
                                        temporaryUser.username()
                                )
                                .isPresent(),
                        "READ_UNCOMMITTED应能读到其他事务未提交的数据"
                );
            } finally {
                writer.rollback();
                reader.rollback();
            }
        }

        assertTrue(
                TestUserRepository
                        .findByUsername(temporaryUser.username())
                        .isEmpty(),
                "写事务回滚后不应保留脏读测试数据"
        );
    }

    @Test(groups = "database")
    @Story("读已提交隔离未提交数据")
    @Description("READ_COMMITTED读取连接在提交前看不到数据，提交后可以看到数据")
    public void shouldHideUncommittedDataUnderReadCommitted()
            throws SQLException {
        TestUser temporaryUser = TestUserTestDataFactory
                .createTemporaryUser();

        Allure.parameter("隔离级别", "READ_COMMITTED");
        Allure.parameter("事务临时用户ID", temporaryUser.id());

        try (
                Connection writer = JdbcUtil.getConnection();
                Connection reader = JdbcUtil.getConnection()
        ) {
            writer.setAutoCommit(false);
            reader.setTransactionIsolation(
                    Connection.TRANSACTION_READ_COMMITTED
            );
            reader.setAutoCommit(false);

            try {
                TestUserRepository.create(writer, temporaryUser);

                assertFalse(
                        TestUserRepository
                                .findByUsername(
                                        reader,
                                        temporaryUser.username()
                                )
                                .isPresent(),
                        "READ_COMMITTED不应读到其他事务未提交的数据"
                );

                writer.commit();
                registerTemporaryUserForCleanup(temporaryUser.id());

                assertTrue(
                        TestUserRepository
                                .findByUsername(
                                        reader,
                                        temporaryUser.username()
                                )
                                .isPresent(),
                        "写事务提交后，READ_COMMITTED应能读到数据"
                );
            } finally {
                writer.rollback();
                reader.rollback();
            }
        }
    }

    @Test(groups = "database")
    @Story("读已提交允许不可重复读")
    @Description("READ_COMMITTED事务两次读取之间，其他事务提交的更新可以被看到")
    public void shouldAllowNonRepeatableReadUnderReadCommitted()
            throws SQLException {
        TestUser temporaryUser = TestUserTestDataFactory
                .createTemporaryUser("initial-role");
        String updatedRole = "read-committed-updated";

        TestUserRepository.create(temporaryUser);
        registerTemporaryUserForCleanup(temporaryUser.id());

        try (
                Connection writer = JdbcUtil.getConnection();
                Connection reader = JdbcUtil.getConnection()
        ) {
            writer.setAutoCommit(false);
            reader.setTransactionIsolation(
                    Connection.TRANSACTION_READ_COMMITTED
            );
            reader.setAutoCommit(false);

            try {
                TestUser firstRead = TestUserRepository
                        .findByUsername(
                                reader,
                                temporaryUser.username()
                        )
                        .orElseThrow();
                assertEquals(firstRead.role(), temporaryUser.role());

                TestUserRepository.updateRole(
                        writer,
                        temporaryUser.id(),
                        updatedRole
                );
                writer.commit();

                TestUser secondRead = TestUserRepository
                        .findByUsername(
                                reader,
                                temporaryUser.username()
                        )
                        .orElseThrow();
                assertEquals(
                        secondRead.role(),
                        updatedRole,
                        "READ_COMMITTED第二次读取应看到已提交更新"
                );
            } finally {
                writer.rollback();
                reader.rollback();
            }
        }
    }

    @Test(groups = "database")
    @Story("可重复读保持事务快照")
    @Description("REPEATABLE_READ事务两次读取之间，保持第一次读取时的数据快照")
    public void shouldKeepSameSnapshotUnderRepeatableRead()
            throws SQLException {
        TestUser temporaryUser = TestUserTestDataFactory
                .createTemporaryUser("initial-role");
        String updatedRole = "repeatable-read-updated";

        TestUserRepository.create(temporaryUser);
        registerTemporaryUserForCleanup(temporaryUser.id());

        try (
                Connection writer = JdbcUtil.getConnection();
                Connection reader = JdbcUtil.getConnection()
        ) {
            writer.setAutoCommit(false);
            reader.setTransactionIsolation(
                    Connection.TRANSACTION_REPEATABLE_READ
            );
            reader.setAutoCommit(false);

            try {
                TestUser firstRead = TestUserRepository
                        .findByUsername(
                                reader,
                                temporaryUser.username()
                        )
                        .orElseThrow();
                assertEquals(firstRead.role(), temporaryUser.role());

                TestUserRepository.updateRole(
                        writer,
                        temporaryUser.id(),
                        updatedRole
                );
                writer.commit();

                TestUser secondRead = TestUserRepository
                        .findByUsername(
                                reader,
                                temporaryUser.username()
                        )
                        .orElseThrow();
                assertEquals(
                        secondRead.role(),
                        temporaryUser.role(),
                        "REPEATABLE_READ第二次读取应保持原事务快照"
                );
            } finally {
                writer.rollback();
                reader.rollback();
            }
        }
    }

    @Test(groups = "database")
    @Story("可重复读保持新增行快照")
    @Description("REPEATABLE_READ事务重复统计时，不应看到其他事务后来提交的新增行")
    public void shouldHideNewRowsUnderRepeatableRead()
            throws SQLException {
        String uniqueRole = "phantom-" + UUID.randomUUID()
                .toString()
                .substring(0, 8);
        TestUser newUser = TestUserTestDataFactory
                .createTemporaryUser(uniqueRole);

        try (
                Connection writer = JdbcUtil.getConnection();
                Connection reader = JdbcUtil.getConnection()
        ) {
            writer.setAutoCommit(false);
            reader.setTransactionIsolation(
                    Connection.TRANSACTION_REPEATABLE_READ
            );
            reader.setAutoCommit(false);

            try {
                long firstCount = TestUserRepository.countByRole(
                        reader,
                        uniqueRole
                );
                assertEquals(firstCount, 0L);

                TestUserRepository.create(writer, newUser);
                writer.commit();
                registerTemporaryUserForCleanup(newUser.id());

                long secondCount = TestUserRepository.countByRole(
                        reader,
                        uniqueRole
                );
                assertEquals(
                        secondCount,
                        firstCount,
                        "REPEATABLE_READ重复统计应保持原事务快照"
                );
            } finally {
                writer.rollback();
                reader.rollback();
            }
        }

        assertEquals(
                TestUserRepository.countByRole(uniqueRole),
                1L,
                "新事务应能看到已经提交的新增用户"
        );
    }

    @Test(groups = "database")
    @Story("并发更新触发行锁等待")
    @Description("一个事务持有用户行锁时，另一个事务更新同一行应等待并超时")
    public void shouldTimeOutWhenUpdatingLockedUser()
            throws SQLException {
        TestUser temporaryUser = TestUserTestDataFactory
                .createTemporaryUser("initial-role");
        TestUserRepository.create(temporaryUser);
        registerTemporaryUserForCleanup(temporaryUser.id());

        try (
                Connection firstWriter = JdbcUtil.getConnection();
                Connection secondWriter = JdbcUtil.getConnection()
        ) {
            firstWriter.setAutoCommit(false);
            secondWriter.setAutoCommit(false);

            try (Statement statement = secondWriter.createStatement()) {
                statement.execute(
                        "SET SESSION innodb_lock_wait_timeout = 1"
                );
            }

            try {
                TestUserRepository.updateRole(
                        firstWriter,
                        temporaryUser.id(),
                        "first-writer-role"
                );

                IllegalStateException exception = expectThrows(
                        IllegalStateException.class,
                        () -> TestUserRepository.updateRole(
                                secondWriter,
                                temporaryUser.id(),
                                "second-writer-role"
                        )
                );

                assertTrue(
                        exception.getCause() instanceof SQLException,
                        "底层应是数据库抛出的SQL异常"
                );
                SQLException sqlException =
                        (SQLException) exception.getCause();
                assertEquals(
                        sqlException.getErrorCode(),
                        1205,
                        "应触发MySQL锁等待超时"
                );
            } finally {
                firstWriter.rollback();
                secondWriter.rollback();
            }
        }

        TestUser savedUser = TestUserRepository
                .findById(temporaryUser.id())
                .orElseThrow();
        assertEquals(
                savedUser.role(),
                temporaryUser.role(),
                "两个写事务回滚后应保留原角色"
        );
    }
}
