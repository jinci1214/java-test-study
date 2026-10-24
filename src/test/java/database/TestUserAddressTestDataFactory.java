package database;

import database.model.TestUserAddress;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 统一创建数据库自动化测试所需的临时用户地址数据。
 */
public final class TestUserAddressTestDataFactory {

    private TestUserAddressTestDataFactory() {
    }

    public static TestUserAddress createTemporaryAddress(
            long userId,
            String label
    ) {
        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "临时用户地址的userId必须大于0"
            );
        }

        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException(
                    "临时用户地址标签不能为空"
            );
        }

        long addressId = ThreadLocalRandom.current()
                .nextLong(1, Long.MAX_VALUE);
        String uniqueSuffix = UUID.randomUUID()
                .toString()
                .replace("-", "");

        return new TestUserAddress(
                addressId,
                userId,
                label,
                "测试地址-" + uniqueSuffix
        );
    }
}
