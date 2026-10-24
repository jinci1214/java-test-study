package application.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    List<UserResponse> findAll();

    Optional<UserResponse> findById(long id);

    Optional<UserResponse> findByUsername(String username);

    UserResponse create(String username, String role);

    Optional<UserResponse> update(
            long id,
            String username,
            String role
    );

    boolean delete(long id);
}
