package database;

import database.model.TestUser;
import report.AllureAttachmentUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * test_users 表的数据访问层。
 */
public final class TestUserRepository {

    private static final String INSERT_SQL = """
            INSERT INTO test_users (id, username, role)
            VALUES (?, ?, ?)
            """;

    private static final String FIND_BY_USERNAME_SQL = """
            SELECT id, username, role
            FROM test_users
            WHERE username = ?
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT id, username, role
            FROM test_users
            WHERE id = ?
            """;

    private static final String DELETE_BY_ID_SQL = """
            DELETE FROM test_users
            WHERE id = ?
            """;

    private static final String UPDATE_ROLE_SQL = """
            UPDATE test_users
            SET role = ?
            WHERE id = ?
            """;

    private TestUserRepository() {
    }

    public static void create(TestUser user) {
        validateUser(user);

        try (Connection connection = JdbcUtil.getConnection()) {
            create(connection, user);
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "新增测试用户失败：username=" + user.username(),
                    exception
            );
        }
    }

    public static void create(Connection connection, TestUser user) {
        validateConnection(connection);
        validateUser(user);

        String parameters = "id=%d, username=%s, role=%s".formatted(
                user.id(),
                user.username(),
                user.role()
        );

        try (PreparedStatement statement =
                     connection.prepareStatement(INSERT_SQL)) {
            statement.setLong(1, user.id());
            statement.setString(2, user.username());
            statement.setString(3, user.role());

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new IllegalStateException(
                        "新增测试用户失败，影响行数：" + affectedRows
                );
            }

            AllureAttachmentUtil.attachDatabaseOperation(
                    "新增测试用户",
                    INSERT_SQL,
                    parameters,
                    "影响行数=" + affectedRows
            );
        } catch (SQLException exception) {
            AllureAttachmentUtil.attachDatabaseOperation(
                    "新增测试用户（失败）",
                    INSERT_SQL,
                    parameters,
                    "异常类型=%s%nSQLState=%s%n错误信息=%s".formatted(
                            exception.getClass().getSimpleName(),
                            exception.getSQLState(),
                            exception.getMessage()
                    )
            );

            throw new IllegalStateException(
                    "新增测试用户失败：username=" + user.username(),
                    exception
            );
        }
    }

    public static Optional<TestUser> findByUsername(
            String username
    ) {
        validateUsername(username);

        try (Connection connection = JdbcUtil.getConnection()) {
            return findByUsername(connection, username);
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "查询测试用户失败：username=" + username,
                    exception
            );
        }
    }

    public static Optional<TestUser> findById(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "查询用户的id必须大于0"
            );
        }

        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(FIND_BY_ID_SQL)
        ) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    AllureAttachmentUtil.attachDatabaseOperation(
                            "按ID查询测试用户",
                            FIND_BY_ID_SQL,
                            "id=" + id,
                            "未查询到数据"
                    );
                    return Optional.empty();
                }

                TestUser user = new TestUser(
                        resultSet.getLong("id"),
                        resultSet.getString("username"),
                        resultSet.getString("role")
                );

                AllureAttachmentUtil.attachDatabaseOperation(
                        "按ID查询测试用户",
                        FIND_BY_ID_SQL,
                        "id=" + id,
                        "查询到用户：" + user
                );

                return Optional.of(user);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "按ID查询测试用户失败：id=" + id,
                    exception
            );
        }
    }

    public static Optional<TestUser> findByUsername(
            Connection connection,
            String username
    ) {
        validateConnection(connection);

        validateUsername(username);

        try (PreparedStatement statement =
                     connection.prepareStatement(FIND_BY_USERNAME_SQL)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    AllureAttachmentUtil.attachDatabaseOperation(
                            "按用户名查询测试用户",
                            FIND_BY_USERNAME_SQL,
                            "username=" + username,
                            "未查询到数据"
                    );
                    return Optional.empty();
                }

                TestUser user = new TestUser(
                        resultSet.getLong("id"),
                        resultSet.getString("username"),
                        resultSet.getString("role")
                );

                AllureAttachmentUtil.attachDatabaseOperation(
                        "按用户名查询测试用户",
                        FIND_BY_USERNAME_SQL,
                        "username=" + username,
                        "查询到用户：" + user
                );

                return Optional.of(
                        user
                );
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "查询测试用户失败：username=" + username,
                    exception
            );
        }
    }

    public static int deleteById(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "删除用户的id必须大于0"
            );
        }

        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(DELETE_BY_ID_SQL)
        ) {
            statement.setLong(1, id);
            int affectedRows = statement.executeUpdate();

            AllureAttachmentUtil.attachDatabaseOperation(
                    "删除测试用户",
                    DELETE_BY_ID_SQL,
                    "id=" + id,
                    "影响行数=" + affectedRows
            );

            return affectedRows;
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "删除测试用户失败：id=" + id,
                    exception
            );
        }
    }

    public static void updateRole(long id, String role) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "更新用户的id必须大于0"
            );
        }

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException(
                    "更新后的用户角色不能为空"
            );
        }

        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(UPDATE_ROLE_SQL)
        ) {
            statement.setString(1, role);
            statement.setLong(2, id);

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                AllureAttachmentUtil.attachDatabaseOperation(
                        "更新测试用户角色（失败）",
                        UPDATE_ROLE_SQL,
                        "id=%d, role=%s".formatted(id, role),
                        "影响行数=%d，期望影响行数=1".formatted(
                                affectedRows
                        )
                );

                throw new IllegalStateException(
                        "更新测试用户失败，影响行数：" + affectedRows
                                + "，id=" + id
                );
            }

            AllureAttachmentUtil.attachDatabaseOperation(
                    "更新测试用户角色",
                    UPDATE_ROLE_SQL,
                    "id=%d, role=%s".formatted(id, role),
                    "影响行数=" + affectedRows
            );
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "更新测试用户角色失败：id=" + id,
                    exception
            );
        }
    }

    private static void validateUser(TestUser user) {
        if (user == null) {
            throw new IllegalArgumentException("测试用户不能为空");
        }

        if (user.id() <= 0) {
            throw new IllegalArgumentException(
                    "测试用户id必须大于0"
            );
        }

        if (user.username() == null || user.username().isBlank()) {
            throw new IllegalArgumentException(
                    "测试用户名不能为空"
            );
        }

        if (user.role() == null || user.role().isBlank()) {
            throw new IllegalArgumentException(
                    "测试用户角色不能为空"
            );
        }
    }

    private static void validateConnection(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("数据库连接不能为空");
        }
    }

    private static void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "查询用户名不能为空"
            );
        }
    }
}
