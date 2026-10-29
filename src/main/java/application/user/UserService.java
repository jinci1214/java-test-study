package application.user;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll();
    }

    public UserResponse findById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public UserResponse create(String username, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException(username);
        }
        try {
            return userRepository.create(username, role);
        } catch (DuplicateKeyException exception) {
            throw new UserAlreadyExistsException(username, exception);
        }
    }

    public UserResponse update(
            long id,
            String username,
            String role
    ) {
        findById(id);
        userRepository.findByUsername(username)
                .filter(user -> user.id() != id)
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException(username);
                });
        try {
            return userRepository.update(id, username, role)
                    .orElseThrow(() -> new UserNotFoundException(id));
        } catch (DuplicateKeyException exception) {
            throw new UserAlreadyExistsException(username, exception);
        }
    }

    public void delete(long id) {
        if (!userRepository.delete(id)) {
            throw new UserNotFoundException(id);
        }
    }
}
