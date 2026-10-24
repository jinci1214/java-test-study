package database.model;

/**
 * test_users 与 test_user_profiles 关联查询的结果。
 */
public record TestUserWithProfile(
        long userId,
        String username,
        String role,
        String nickname,
        String email
) {
}
