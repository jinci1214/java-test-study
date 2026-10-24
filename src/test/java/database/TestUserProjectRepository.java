package database;

import database.model.TestProject;
import database.model.TestUser;
import database.model.TestUserProject;
import report.AllureAttachmentUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * test_user_projects 中间表的数据访问层。
 */
public final class TestUserProjectRepository {

    private static final String INSERT_SQL = """
            INSERT INTO test_user_projects (user_id, project_id)
            VALUES (?, ?)
            """;

    private static final String FIND_PROJECTS_BY_USER_ID_SQL = """
            SELECT p.id, p.name
            FROM test_projects p
            INNER JOIN test_user_projects up
                ON up.project_id = p.id
            WHERE up.user_id = ?
            ORDER BY p.id
            """;

    private static final String FIND_USERS_BY_PROJECT_ID_SQL = """
            SELECT u.id, u.username, u.role
            FROM test_users u
            INNER JOIN test_user_projects up
                ON up.user_id = u.id
            WHERE up.project_id = ?
            ORDER BY u.id
            """;

    private static final String DELETE_SQL = """
            DELETE FROM test_user_projects
            WHERE user_id = ? AND project_id = ?
            """;

    private TestUserProjectRepository() {
    }

    public static void create(TestUserProject relation) {
        validateRelation(relation);

        if (TestUserRepository.findById(relation.userId()).isEmpty()) {
            throw new IllegalStateException(
                    "建立用户项目关系失败：关联用户不存在，userId="
                            + relation.userId()
            );
        }

        if (TestProjectRepository.findById(relation.projectId()).isEmpty()) {
            throw new IllegalStateException(
                    "建立用户项目关系失败：关联项目不存在，projectId="
                            + relation.projectId()
            );
        }

        String parameters = "userId=%d, projectId=%d".formatted(
                relation.userId(),
                relation.projectId()
        );

        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(INSERT_SQL)
        ) {
            statement.setLong(1, relation.userId());
            statement.setLong(2, relation.projectId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new IllegalStateException(
                        "建立用户项目关系失败，影响行数："
                                + affectedRows
                );
            }

            AllureAttachmentUtil.attachDatabaseOperation(
                    "建立用户项目关系",
                    INSERT_SQL,
                    parameters,
                    "影响行数=" + affectedRows
            );
        } catch (SQLException exception) {
            AllureAttachmentUtil.attachDatabaseOperation(
                    "建立用户项目关系（失败）",
                    INSERT_SQL,
                    parameters,
                    "异常类型=%s%nSQLState=%s%n错误信息=%s".formatted(
                            exception.getClass().getSimpleName(),
                            exception.getSQLState(),
                            exception.getMessage()
                    )
            );

            throw new IllegalStateException(
                    "建立用户项目关系失败：userId="
                            + relation.userId()
                            + "，projectId=" + relation.projectId(),
                    exception
            );
        }
    }

    public static List<TestProject> findProjectsByUserId(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "查询用户项目的userId必须大于0"
            );
        }

        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        FIND_PROJECTS_BY_USER_ID_SQL
                )
        ) {
            statement.setLong(1, userId);
            List<TestProject> projects = new ArrayList<>();

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    projects.add(new TestProject(
                            resultSet.getLong("id"),
                            resultSet.getString("name")
                    ));
                }
            }

            AllureAttachmentUtil.attachDatabaseOperation(
                    "按用户ID查询项目",
                    FIND_PROJECTS_BY_USER_ID_SQL,
                    "userId=" + userId,
                    "查询到项目数量=" + projects.size()
            );

            return List.copyOf(projects);
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "查询用户项目失败：userId=" + userId,
                    exception
            );
        }
    }

    public static List<TestUser> findUsersByProjectId(long projectId) {
        if (projectId <= 0) {
            throw new IllegalArgumentException(
                    "查询项目用户的projectId必须大于0"
            );
        }

        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        FIND_USERS_BY_PROJECT_ID_SQL
                )
        ) {
            statement.setLong(1, projectId);
            List<TestUser> users = new ArrayList<>();

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(new TestUser(
                            resultSet.getLong("id"),
                            resultSet.getString("username"),
                            resultSet.getString("role")
                    ));
                }
            }

            AllureAttachmentUtil.attachDatabaseOperation(
                    "按项目ID查询用户",
                    FIND_USERS_BY_PROJECT_ID_SQL,
                    "projectId=" + projectId,
                    "查询到用户数量=" + users.size()
            );

            return List.copyOf(users);
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "查询项目用户失败：projectId=" + projectId,
                    exception
            );
        }
    }

    public static int delete(long userId, long projectId) {
        validateRelation(new TestUserProject(userId, projectId));

        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(DELETE_SQL)
        ) {
            statement.setLong(1, userId);
            statement.setLong(2, projectId);
            int affectedRows = statement.executeUpdate();

            AllureAttachmentUtil.attachDatabaseOperation(
                    "删除用户项目关系",
                    DELETE_SQL,
                    "userId=%d, projectId=%d".formatted(
                            userId,
                            projectId
                    ),
                    "影响行数=" + affectedRows
            );

            return affectedRows;
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "删除用户项目关系失败：userId="
                            + userId
                            + "，projectId=" + projectId,
                    exception
            );
        }
    }

    private static void validateRelation(TestUserProject relation) {
        if (relation == null) {
            throw new IllegalArgumentException(
                    "用户项目关系不能为空"
            );
        }

        if (relation.userId() <= 0) {
            throw new IllegalArgumentException(
                    "用户项目关系的userId必须大于0"
            );
        }

        if (relation.projectId() <= 0) {
            throw new IllegalArgumentException(
                    "用户项目关系的projectId必须大于0"
            );
        }
    }
}
