package application.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank(message = "用户名不能为空")
        String username,
        @NotBlank(message = "用户角色不能为空")
        String role
) {
}
