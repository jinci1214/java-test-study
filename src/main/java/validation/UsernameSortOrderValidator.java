package validation;

/**
 * 用户名排序方向的通用校验。
 */
public final class UsernameSortOrderValidator {

    private UsernameSortOrderValidator() {
    }

    public static void validate(String sortOrder) {
        if (!"asc".equals(sortOrder) && !"desc".equals(sortOrder)) {
            throw new IllegalArgumentException("用户名排序方向只能是 asc 或 desc");
        }
    }
}
