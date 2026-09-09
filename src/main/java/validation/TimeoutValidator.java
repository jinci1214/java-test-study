package validation;

/**
 * HTTP 请求超时时间的通用校验。
 */
public final class TimeoutValidator {

    private TimeoutValidator() {
    }

    public static void validate(int timeoutMillis) {
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("超时时间必须是正数");
        }
    }
}
