package validation;

/**
 * 用户角色筛选参数的通用校验。
 */
public final class RoleValidator {

    private RoleValidator() {
    }

    public static void validate(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("筛选角色不能为空");
        }
    }
}
