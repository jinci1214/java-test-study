package validation;

/**
 * 分页参数的通用校验。
 */
public final class PaginationValidator {

    private PaginationValidator() {
    }

    public static void validate(int page, int pageSize) {
        if (page <= 0) {
            throw new IllegalArgumentException("页码必须大于0");
        }

        if (pageSize <= 0) {
            throw new IllegalArgumentException("每页数量必须大于0");
        }
    }
}
