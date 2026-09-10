package validation;

/**
 * 用户 ID 的通用参数校验。
 */
public final class UserIdValidator {

    private UserIdValidator() {
    }

    public static void validate(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
    }
}
