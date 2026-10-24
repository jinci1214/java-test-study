package application.user;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Profile("!mysql")
public class InMemoryUserRepository implements UserRepository {

    private final ConcurrentMap<Long, UserResponse> users =
            new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(3L);

    public InMemoryUserRepository() {
        users.put(
                1L,
                new UserResponse(1L, "admin", "administrator")
        );
        users.put(
                2L,
                new UserResponse(2L, "tester", "tester")
        );
    }

    @Override
    public List<UserResponse> findAll() {
        return users.values()
                .stream()
                .sorted(Comparator.comparingLong(UserResponse::id))
                .toList();
    }

    @Override
    public Optional<UserResponse> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<UserResponse> findByUsername(String username) {
        return users.values()
                .stream()
                .filter(user -> user.username().equals(username))
                .findFirst();
    }

    @Override
    public UserResponse create(String username, String role) {
        long id = nextId.getAndIncrement();
        UserResponse user = new UserResponse(id, username, role);
        users.put(id, user);
        return user;
    }

    @Override
    public Optional<UserResponse> update(
            long id,
            String username,
            String role
    ) {
        UserResponse updatedUser = users.computeIfPresent(
                id,
                (userId, oldUser) -> new UserResponse(
                        userId,
                        username,
                        role
                )
        );
        return Optional.ofNullable(updatedUser);
    }

    @Override
    public boolean delete(long id) {
        return users.remove(id) != null;
    }
}
