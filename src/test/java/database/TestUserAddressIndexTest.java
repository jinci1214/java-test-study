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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Epic("数据库测试")
@Feature("数据库索引")
public class TestUserAddressIndexTest extends DatabaseTestBase {

    private static final String ADDRESS_INDEX_NAME =
            "idx_test_user_addresses_user_id";

    private static final String FIND_INDEX_METADATA_SQL = """
            SELECT
                NON_UNIQUE AS non_unique,
                COLUMN_NAME AS column_name,
                SEQ_IN_INDEX AS seq_in_index
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = ?
              AND index_name = ?
            ORDER BY seq_in_index
            """;

    private static final String EXPLAIN_ADDRESS_QUERY_SQL = """
            EXPLAIN
            SELECT id, user_id, label, address
            FROM test_user_addresses
            WHERE user_id = ?
            ORDER BY id
            """;

    @Test(groups = "database")
    @Story("验证地址逻辑外键索引")
    @Description("验证user_id普通索引存在，且索引字段和唯一性配置正确")
    public void shouldHaveUserIdIndexOnAddressTable() throws SQLException {
        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        FIND_INDEX_METADATA_SQL
                )
        ) {
            statement.setString(1, "test_user_addresses");
            statement.setString(2, ADDRESS_INDEX_NAME);

            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(
                        resultSet.next(),
                        "地址表应存在user_id索引"
                );
                assertEquals(
                        resultSet.getString("column_name"),
                        "user_id"
                );
                assertEquals(
                        resultSet.getInt("non_unique"),
                        1,
                        "user_id索引应允许同一用户拥有多条地址"
                );
                assertEquals(
                        resultSet.getInt("seq_in_index"),
                        1
                );
                assertFalse(
                        resultSet.next(),
                        "该索引当前应只包含user_id一个字段"
                );
            }
        }
    }

    @Test(groups = "database")
    @Story("分析地址查询计划")
    @Description("使用EXPLAIN验证按user_id查询时，地址索引属于候选索引")
    public void shouldExposeUserIdIndexInExplainPlan() throws SQLException {
        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        EXPLAIN_ADDRESS_QUERY_SQL
                )
        ) {
            statement.setLong(1, 1L);

            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next(), "EXPLAIN应返回查询计划");

                String possibleKeys = resultSet.getString("possible_keys");
                String selectedKey = resultSet.getString("key");
                String accessType = resultSet.getString("type");
                long estimatedRows = resultSet.getLong("rows");

                AllureAttachmentUtil.attachDatabaseOperation(
                        "EXPLAIN地址查询",
                        EXPLAIN_ADDRESS_QUERY_SQL,
                        "userId=1",
                        "possible_keys=%s%nkey=%s%ntype=%s%nrows=%d"
                                .formatted(
                                        possibleKeys,
                                        selectedKey,
                                        accessType,
                                        estimatedRows
                                )
                );

                assertNotNull(
                        possibleKeys,
                        "按user_id查询时应存在候选索引"
                );
                assertTrue(
                        possibleKeys.contains(ADDRESS_INDEX_NAME),
                        "候选索引中应包含地址user_id索引"
                );
            }
        }
    }
}
