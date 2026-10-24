package application.user;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("mysql")
public class JdbcUserRepository implements UserRepository {

    private static final String FIND_ALL_SQL = """
            SELECT id, username, role
            FROM app_users
            ORDER BY id
            """;
    private static final String FIND_BY_ID_SQL = """
            SELECT id, username, role
            FROM app_users
            WHERE id = ?
            """;
    private static final String FIND_BY_USERNAME_SQL = """
            SELECT id, username, role
            FROM app_users
            WHERE username = ?
            """;
    private static final String CREATE_SQL = """
            INSERT INTO app_users (username, role)
            VALUES (?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE app_users
            SET username = ?, role = ?
            WHERE id = ?
            """;
    private static final String DELETE_SQL = """
            DELETE FROM app_users
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<UserResponse> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, this::mapUser);
    }

    @Override
    public Optional<UserResponse> findById(long id) {
        return jdbcTemplate
                .query(FIND_BY_ID_SQL, this::mapUser, id)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<UserResponse> findByUsername(String username) {
        return jdbcTemplate
                .query(FIND_BY_USERNAME_SQL, this::mapUser, username)
                .stream()
                .findFirst();
    }

    @Override
    public UserResponse create(String username, String role) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    CREATE_SQL,
                                    Statement.RETURN_GENERATED_KEYS
                            );
                    statement.setString(1, username);
                    statement.setString(2, role);
                    return statement;
                },
                keyHolder
        );
        long id = keyHolder.getKey().longValue();
        return new UserResponse(id, username, role);
    }

    @Override
    public Optional<UserResponse> update(
            long id,
            String username,
            String role
    ) {
        int affectedRows = jdbcTemplate.update(
                UPDATE_SQL,
                username,
                role,
                id
        );
        if (affectedRows == 0) {
            return Optional.empty();
        }
        return findById(id);
    }

    @Override
    public boolean delete(long id) {
        return jdbcTemplate.update(DELETE_SQL, id) == 1;
    }

    private UserResponse mapUser(ResultSet resultSet, int rowNumber)
            throws SQLException {
        return new UserResponse(
                resultSet.getLong("id"),
                resultSet.getString("username"),
                resultSet.getString("role")
        );
    }
}
