package config;

/**
 * 数据库连接配置。
 *
 * <p>地址和用户名提供本地默认值，密码必须通过环境变量传入，
 * 防止敏感信息被写进代码或提交到 Git。</p>
 */
public final class DatabaseConfig {

    private static final String DEFAULT_URL =
            "jdbc:mysql://127.0.0.1:3306/java_test_study";
    private static final String DEFAULT_USERNAME =
            "test_automation";

    private DatabaseConfig() {
    }

    public static String getUrl() {
        return getOptionalEnvironmentVariable(
                "DB_URL",
                DEFAULT_URL
        );
    }

    public static String getUsername() {
        return getOptionalEnvironmentVariable(
                "DB_USERNAME",
                DEFAULT_USERNAME
        );
    }

    public static String getPassword() {
        String password = System.getenv("DB_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "缺少数据库密码，请配置环境变量 DB_PASSWORD"
            );
        }

        return password;
    }

    public static boolean hasPassword() {
        String password = System.getenv("DB_PASSWORD");
        return password != null && !password.isBlank();
    }

    private static String getOptionalEnvironmentVariable(
            String variableName,
            String defaultValue
    ) {
        String value = System.getenv(variableName);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }
}
