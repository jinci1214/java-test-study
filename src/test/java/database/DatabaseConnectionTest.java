package database;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Epic("数据库测试")
@Feature("MySQL连接")
public class DatabaseConnectionTest extends DatabaseTestBase {

    private static final String CURRENT_DATABASE_SQL =
            "SELECT DATABASE()";

    @Test(groups = "database")
    @Story("验证数据库连接")
    @Description("验证测试框架可以连接MySQL，并确认数据库产品和名称")
    public void shouldConnectToMySql() throws SQLException {
        try (
                Connection connection = JdbcUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(CURRENT_DATABASE_SQL);
                ResultSet resultSet = statement.executeQuery()
        ) {
            assertFalse(
                    connection.isClosed(),
                    "数据库连接不应该处于关闭状态"
            );
            assertTrue(
                    connection.isValid(3),
                    "数据库连接应该在3秒内验证成功"
            );
            assertEquals(
                    connection.getMetaData().getDatabaseProductName(),
                    "MySQL"
            );
            assertTrue(
                    resultSet.next(),
                    "SELECT DATABASE() 应该返回一行结果"
            );
            assertEquals(
                    resultSet.getString(1),
                    "java_test_study"
            );
        }
    }
}
