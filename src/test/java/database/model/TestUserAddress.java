package database.model;

/**
 * test_user_addresses 表的一条用户地址数据。
 */
public record TestUserAddress(
        long id,
        long userId,
        String label,
        String address
) {
}
