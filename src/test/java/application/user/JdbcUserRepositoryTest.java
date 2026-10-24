package application.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class JdbcUserRepositoryTest {

    private JdbcUserRepository userRepository;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        String databaseName = "user_repository_"
                + UUID.randomUUID().toString().replace("-", "");
        DriverManagerDataSource dataSource =
                new DriverManagerDataSource(
                        "jdbc:h2:mem:" + databaseName
                                + ";MODE=MySQL;DB_CLOSE_DELAY=-1",
                        "sa",
                        ""
                );
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
                CREATE TABLE app_users (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    role VARCHAR(30) NOT NULL
                )
                """);
        userRepository = new JdbcUserRepository(jdbcTemplate);
    }

    @Test(groups = {"spring", "regression"})
    public void shouldCompleteCrudWithJdbc() {
        UserResponse admin = userRepository.create(
                "admin",
                "administrator"
        );
        UserResponse tester = userRepository.create("tester", "tester");

        assertEquals(admin.id(), 1L);
        assertEquals(tester.id(), 2L);
        assertEquals(
                userRepository.findAll(),
                List.of(admin, tester)
        );
        assertEquals(
                userRepository.findById(admin.id()).orElseThrow(),
                admin
        );
        assertEquals(
                userRepository
                        .findByUsername("tester")
                        .orElseThrow(),
                tester
        );

        UserResponse updatedTester = userRepository.update(
                tester.id(),
                "updated-tester",
                "administrator"
        ).orElseThrow();
        assertEquals(updatedTester.username(), "updated-tester");
        assertEquals(updatedTester.role(), "administrator");

        assertTrue(userRepository.delete(admin.id()));
        assertTrue(userRepository.findById(admin.id()).isEmpty());
    }

    @Test(groups = {"spring", "regression"})
    public void shouldReturnEmptyResultWhenUserDoesNotExist() {
        assertTrue(userRepository.findById(999L).isEmpty());
        assertTrue(userRepository.findByUsername("missing").isEmpty());
        assertTrue(
                userRepository
                        .update(999L, "missing", "tester")
                        .isEmpty()
        );
        assertFalse(userRepository.delete(999L));
    }
}
