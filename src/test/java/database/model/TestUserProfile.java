package database.model;

/**
 * test_user_profiles 表的一条用户资料数据。
 */
public record TestUserProfile(
        long userId,
        String nickname,
        String email
) {
}
