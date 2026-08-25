package database;

import database.model.TestUser;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 统一创建数据库自动化测试所需的临时用户数据。
 */
public final class TestUserTestDataFactory {

    private TestUserTestDataFactory() {
    }

    public static TestUser createTemporaryUser() {
        return createTemporaryUser("temporary");
    }

    public static TestUser createTemporaryUser(String role) {
        String uniqueSuffix = UUID.randomUUID()
                .toString()
                .replace("-", "");

        return createTemporaryUser(
                "db-test-" + uniqueSuffix,
                role
        );
    }

    public static TestUser createTemporaryUser(
            String username,
            String role
    ) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("测试用户名不能为空");
        }

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("测试用户角色不能为空");
        }

        long userId = ThreadLocalRandom.current()
                .nextLong(1, Long.MAX_VALUE);

        return new TestUser(
                userId,
                username,
                role
        );
    }

    public static String createUsernameWithLength(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException(
                    "测试用户名长度必须大于0"
            );
        }

        return "a".repeat(length);
    }
}
