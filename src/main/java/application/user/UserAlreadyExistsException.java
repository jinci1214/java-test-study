package application.user;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String username) {
        super("用户名已存在：" + username);
    }

    public UserAlreadyExistsException(
            String username,
            Throwable cause
    ) {
        super("用户名已存在：" + username, cause);
    }
}
