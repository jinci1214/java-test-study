package common;

/**
 * 当前测试流程使用的 Token 状态。
 */
public final class TokenUtil {

    private static String token;

    private TokenUtil() {
    }

    public static void setToken(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Token不能为空");
        }
        token = value;
    }

    public static String getToken() {
        return token;
    }

    public static boolean hasToken() {
        return token != null && !token.isBlank();
    }

    public static void clear() {
        token = null;
    }
}
