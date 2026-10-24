package database;

import database.model.TestUserProfile;
import database.model.TestUserWithProfile;
import report.AllureAttachmentUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * test_user_profiles 表的数据访问层。
 */
public final class TestUserProfileRepository {

    private static final String INSERT_SQL = """
            INSERT INTO test_user_profiles (
                user_id,
                nickname,
                email
            ) VALUES (?, ?, ?)
            """;

    private static final String FIND_BY_USER_ID_SQL = """
            SELECT user_id, nickname, email
            FROM test_user_profiles
            WHERE user_id = ?
            """;

    private static final String DELETE_BY_USER_ID_SQL = """
            DELETE FROM test_user_profiles
            WHERE user_id = ?
            """;

    private static final String FIND_USER_WITH_PROFILE_SQL = """
            SELECT
                u.id AS user_id,
                u.username,
                u.role,
                p.nickname,
                p.email
            FROM test_users u
            INNER JOIN test_user_profiles p
                ON p.user_id = u.id
            WHERE u.id = ?
            """;

    private TestUserProfileRepository() {
    }

    public static void create(TestUserProfile profile) {
        validateProfile(profile);

        if (TestUserRepository.findById(profile.userId()).isEmpty()) {
            throw new IllegalStateException(
                    "新增用户资料失败：关联用户不存在，userId="
                            + profile.userId()
            );
        }

        String parameters = "userId=%d, nickname=%s, email=%s".formatted(
                profile.userId(),
                profile.nickname(),
                profile.email()
        );

        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(INSERT_SQL)
        ) {
            statement.setLong(1, profile.userId());
            statement.setString(2, profile.nickname());
            statement.setString(3, profile.email());

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new IllegalStateException(
                        "新增用户资料失败，影响行数：" + affectedRows
                );
            }

            AllureAttachmentUtil.attachDatabaseOperation(
                    "新增用户资料",
                    INSERT_SQL,
                    parameters,
                    "影响行数=" + affectedRows
            );
        } catch (SQLException exception) {
            AllureAttachmentUtil.attachDatabaseOperation(
                    "新增用户资料（失败）",
                    INSERT_SQL,
                    parameters,
                    "异常类型=%s%nSQLState=%s%n错误信息=%s".formatted(
                            exception.getClass().getSimpleName(),
                            exception.getSQLState(),
                            exception.getMessage()
                    )
            );

            throw new IllegalStateException(
                    "新增用户资料失败：userId=" + profile.userId(),
                    exception
            );
        }
    }

    public static Optional<TestUserProfile> findByUserId(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "查询用户资料的userId必须大于0"
            );
        }

        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(FIND_BY_USER_ID_SQL)
        ) {
            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    AllureAttachmentUtil.attachDatabaseOperation(
                            "按用户ID查询资料",
                            FIND_BY_USER_ID_SQL,
                            "userId=" + userId,
                            "未查询到数据"
                    );
                    return Optional.empty();
                }

                TestUserProfile profile = new TestUserProfile(
                        resultSet.getLong("user_id"),
                        resultSet.getString("nickname"),
                        resultSet.getString("email")
                );

                AllureAttachmentUtil.attachDatabaseOperation(
                        "按用户ID查询资料",
                        FIND_BY_USER_ID_SQL,
                        "userId=" + userId,
                        "查询到资料：" + profile
                );

                return Optional.of(profile);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "查询用户资料失败：userId=" + userId,
                    exception
            );
        }
    }

    public static int deleteByUserId(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "删除用户资料的userId必须大于0"
            );
        }

        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(DELETE_BY_USER_ID_SQL)
        ) {
            statement.setLong(1, userId);
            int affectedRows = statement.executeUpdate();

            AllureAttachmentUtil.attachDatabaseOperation(
                    "删除用户资料",
                    DELETE_BY_USER_ID_SQL,
                    "userId=" + userId,
                    "影响行数=" + affectedRows
            );

            return affectedRows;
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "删除用户资料失败：userId=" + userId,
                    exception
            );
        }
    }

    public static Optional<TestUserWithProfile> findUserWithProfileByUserId(
            long userId
    ) {
        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "关联查询的userId必须大于0"
            );
        }

        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                                FIND_USER_WITH_PROFILE_SQL
                        )
        ) {
            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    AllureAttachmentUtil.attachDatabaseOperation(
                            "查询用户及其资料",
                            FIND_USER_WITH_PROFILE_SQL,
                            "userId=" + userId,
                            "未查询到关联数据"
                    );
                    return Optional.empty();
                }

                TestUserWithProfile userWithProfile =
                        new TestUserWithProfile(
                                resultSet.getLong("user_id"),
                                resultSet.getString("username"),
                                resultSet.getString("role"),
                                resultSet.getString("nickname"),
                                resultSet.getString("email")
                        );

                AllureAttachmentUtil.attachDatabaseOperation(
                        "查询用户及其资料",
                        FIND_USER_WITH_PROFILE_SQL,
                        "userId=" + userId,
                        "查询到关联数据：" + userWithProfile
                );

                return Optional.of(userWithProfile);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "查询用户及其资料失败：userId=" + userId,
                    exception
            );
        }
    }

    private static void validateProfile(TestUserProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("用户资料不能为空");
        }

        if (profile.userId() <= 0) {
            throw new IllegalArgumentException(
                    "用户资料的userId必须大于0"
            );
        }

        if (profile.nickname() == null || profile.nickname().isBlank()) {
            throw new IllegalArgumentException("用户昵称不能为空");
        }

        if (profile.email() == null || profile.email().isBlank()) {
            throw new IllegalArgumentException("用户邮箱不能为空");
        }
    }
}
