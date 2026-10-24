package application.user;

public record UserResponse(
        long id,
        String username,
        String role
) {
}
