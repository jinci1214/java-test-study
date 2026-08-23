package database;

import config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC 连接入口。
 *
 * <p>调用方负责使用 try-with-resources 关闭返回的连接。</p>
 */
public final class JdbcUtil {

    private static final Logger log =
            LoggerFactory.getLogger(JdbcUtil.class);

    private JdbcUtil() {
    }

    public static Connection getConnection() {
        String url = DatabaseConfig.getUrl();
        String username = DatabaseConfig.getUsername();

        log.info(
                "正在连接数据库：url={}, username={}",
                url,
                username
        );

        try {
            return DriverManager.getConnection(
                    url,
                    username,
                    DatabaseConfig.getPassword()
            );
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "数据库连接失败，请检查MySQL服务、数据库名称和账号配置。"
                            + " url=" + url
                            + ", username=" + username,
                    exception
            );
        }
    }
}
