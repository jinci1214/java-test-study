package database;

import database.model.TestProject;
import report.AllureAttachmentUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * test_projects 表的数据访问层。
 */
public final class TestProjectRepository {

    private static final String INSERT_SQL = """
            INSERT INTO test_projects (id, name)
            VALUES (?, ?)
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT id, name
            FROM test_projects
            WHERE id = ?
            """;

    private static final String DELETE_BY_ID_SQL = """
            DELETE FROM test_projects
            WHERE id = ?
            """;

    private TestProjectRepository() {
    }

    public static void create(TestProject project) {
        validateProject(project);

        String parameters = "id=%d, name=%s".formatted(
                project.id(),
                project.name()
        );

        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(INSERT_SQL)
        ) {
            statement.setLong(1, project.id());
            statement.setString(2, project.name());

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new IllegalStateException(
                        "新增测试项目失败，影响行数：" + affectedRows
                );
            }

            AllureAttachmentUtil.attachDatabaseOperation(
                    "新增测试项目",
                    INSERT_SQL,
                    parameters,
                    "影响行数=" + affectedRows
            );
        } catch (SQLException exception) {
            AllureAttachmentUtil.attachDatabaseOperation(
                    "新增测试项目（失败）",
                    INSERT_SQL,
                    parameters,
                    "异常类型=%s%nSQLState=%s%n错误信息=%s".formatted(
                            exception.getClass().getSimpleName(),
                            exception.getSQLState(),
                            exception.getMessage()
                    )
            );

            throw new IllegalStateException(
                    "新增测试项目失败：name=" + project.name(),
                    exception
            );
        }
    }

    public static Optional<TestProject> findById(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "查询测试项目的id必须大于0"
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
                            "按ID查询测试项目",
                            FIND_BY_ID_SQL,
                            "id=" + id,
                            "未查询到数据"
                    );
                    return Optional.empty();
                }

                TestProject project = new TestProject(
                        resultSet.getLong("id"),
                        resultSet.getString("name")
                );

                AllureAttachmentUtil.attachDatabaseOperation(
                        "按ID查询测试项目",
                        FIND_BY_ID_SQL,
                        "id=" + id,
                        "查询到项目：" + project
                );

                return Optional.of(project);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "查询测试项目失败：id=" + id,
                    exception
            );
        }
    }

    public static int deleteById(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "删除测试项目的id必须大于0"
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
                    "删除测试项目",
                    DELETE_BY_ID_SQL,
                    "id=" + id,
                    "影响行数=" + affectedRows
            );

            return affectedRows;
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "删除测试项目失败：id=" + id,
                    exception
            );
        }
    }

    private static void validateProject(TestProject project) {
        if (project == null) {
            throw new IllegalArgumentException("测试项目不能为空");
        }

        if (project.id() <= 0) {
            throw new IllegalArgumentException("测试项目id必须大于0");
        }

        if (project.name() == null || project.name().isBlank()) {
            throw new IllegalArgumentException("测试项目名称不能为空");
        }
    }
}
