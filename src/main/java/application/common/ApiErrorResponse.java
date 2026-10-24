package application.common;

public record ApiErrorResponse(
        int status,
        String code,
        String message,
        String path
) {
}
