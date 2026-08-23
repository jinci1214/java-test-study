package database;

import database.model.TestUser;

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

    private static final String DELETE_BY_ID_SQL = """
            DELETE FROM test_users
            WHERE id = ?
            """;

    private TestUserRepository() {
    }

    public static void create(TestUser user) {
        validateUser(user);

        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(INSERT_SQL)
        ) {
            statement.setLong(1, user.id());
            statement.setString(2, user.username());
            statement.setString(3, user.role());

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new IllegalStateException(
                        "新增测试用户失败，影响行数：" + affectedRows
                );
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "新增测试用户失败：username=" + user.username(),
                    exception
            );
        }
    }

    public static Optional<TestUser> findByUsername(
            String username
    ) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "查询用户名不能为空"
            );
        }

        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(FIND_BY_USERNAME_SQL)
        ) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        new TestUser(
                                resultSet.getLong("id"),
                                resultSet.getString("username"),
                                resultSet.getString("role")
                        )
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
            return statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "删除测试用户失败：id=" + id,
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
}
