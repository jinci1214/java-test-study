package database;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import report.AllureAttachmentUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Epic("数据库测试")
@Feature("数据库联合索引")
public class TestUserProjectIndexTest extends DatabaseTestBase {

    private static final String PROJECT_ID_INDEX =
            "idx_test_user_projects_project_id";

    private static final String FIND_INDEX_COLUMNS_SQL = """
            SELECT COLUMN_NAME AS column_name
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'test_user_projects'
              AND index_name = ?
            ORDER BY SEQ_IN_INDEX
            """;

    private static final String EXPLAIN_BY_USER_ID_SQL = """
            EXPLAIN
            SELECT user_id, project_id
            FROM test_user_projects
            WHERE user_id = ?
            """;

    private static final String EXPLAIN_BY_PROJECT_ID_SQL = """
            EXPLAIN
            SELECT user_id, project_id
            FROM test_user_projects
            WHERE project_id = ?
            """;

    @Test(groups = "database")
    @Story("验证中间表索引结构")
    @Description("验证联合主键字段顺序及project_id独立索引")
    public void shouldHaveExpectedIndexesOnUserProjectTable()
            throws SQLException {
        assertEquals(
                findIndexColumns("PRIMARY"),
                List.of("user_id", "project_id")
        );
        assertEquals(
                findIndexColumns(PROJECT_ID_INDEX),
                List.of("project_id")
        );
    }

    @Test(groups = "database")
    @Story("按用户查询使用联合主键候选索引")
    @Description("验证只按联合主键最左字段user_id查询时，PRIMARY是候选索引")
    public void shouldExposePrimaryKeyForUserIdQuery()
            throws SQLException {
        assertExplainContainsPossibleKey(
                EXPLAIN_BY_USER_ID_SQL,
                "PRIMARY"
        );
    }

    @Test(groups = "database")
    @Story("按项目查询使用独立候选索引")
    @Description("验证只按project_id查询时，使用为反向查询创建的独立索引")
    public void shouldExposeProjectIdIndexForProjectIdQuery()
            throws SQLException {
        assertExplainContainsPossibleKey(
                EXPLAIN_BY_PROJECT_ID_SQL,
                PROJECT_ID_INDEX
        );
    }

    private List<String> findIndexColumns(String indexName)
            throws SQLException {
        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        FIND_INDEX_COLUMNS_SQL
                )
        ) {
            statement.setString(1, indexName);
            List<String> columns = new ArrayList<>();

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    columns.add(resultSet.getString("column_name"));
                }
            }

            return List.copyOf(columns);
        }
    }

    private void assertExplainContainsPossibleKey(
            String explainSql,
            String expectedIndex
    ) throws SQLException {
        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(explainSql)
        ) {
            statement.setLong(1, 1L);

            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next(), "EXPLAIN应返回查询计划");

                String possibleKeys = resultSet.getString("possible_keys");
                String selectedKey = resultSet.getString("key");
                String accessType = resultSet.getString("type");
                long estimatedRows = resultSet.getLong("rows");

                AllureAttachmentUtil.attachDatabaseOperation(
                        "EXPLAIN用户项目关系查询",
                        explainSql,
                        "查询参数=1",
                        "possible_keys=%s%nkey=%s%ntype=%s%nrows=%d"
                                .formatted(
                                        possibleKeys,
                                        selectedKey,
                                        accessType,
                                        estimatedRows
                                )
                );

                assertNotNull(possibleKeys, "查询应存在候选索引");
                assertTrue(
                        possibleKeys.contains(expectedIndex),
                        "候选索引中应包含：" + expectedIndex
                );
            }
        }
    }
}
