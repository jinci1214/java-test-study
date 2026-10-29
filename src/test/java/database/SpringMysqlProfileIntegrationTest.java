package database;

import application.JavaTestStudyApplication;
import application.user.JdbcUserRepository;
import application.user.UserRepository;
import application.user.UserResponse;
import config.DatabaseConfig;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SpringMysqlProfileIntegrationTest {

    @Test(groups = "database")
    public void shouldPersistUserWithMysqlProfile() {
        requireDatabasePassword();

        String username = "spring_"
                + UUID.randomUUID().toString().replace("-", "");
        String updatedUsername = username + "_updated";
        ConfigurableApplicationContext context = null;
        Long temporaryUserId = null;

        try {
            context = new SpringApplicationBuilder(
                    JavaTestStudyApplication.class
            )
                    .profiles("mysql")
                    .web(WebApplicationType.NONE)
                    .run();

            UserRepository userRepository = context.getBean(
                    UserRepository.class
            );
            assertTrue(userRepository instanceof JdbcUserRepository);

            UserResponse createdUser = userRepository.create(
                    username,
                    "tester"
            );
            temporaryUserId = createdUser.id();

            assertEquals(
                    userRepository
                            .findByUsername(username)
                            .orElseThrow(),
                    createdUser
            );

            UserResponse updatedUser = userRepository.update(
                    temporaryUserId,
                    updatedUsername,
                    "administrator"
            ).orElseThrow();
            assertEquals(updatedUser.username(), updatedUsername);
            assertEquals(updatedUser.role(), "administrator");

            assertTrue(userRepository.delete(temporaryUserId));
            assertTrue(
                    userRepository.findById(temporaryUserId).isEmpty()
            );
            temporaryUserId = null;
        } finally {
            cleanUpTemporaryUser(context, temporaryUserId);
            if (context != null) {
                context.close();
            }
        }
    }

    private void requireDatabasePassword() {
        if (!DatabaseConfig.hasPassword()) {
            throw new SkipException(
                    "未配置 DB_PASSWORD，跳过 Spring MySQL 联调测试"
            );
        }
    }

    private void cleanUpTemporaryUser(
            ConfigurableApplicationContext context,
            Long temporaryUserId
    ) {
        if (context == null || temporaryUserId == null) {
            return;
        }

        UserRepository userRepository = context.getBean(
                UserRepository.class
        );
        if (!userRepository.delete(temporaryUserId)) {
            throw new IllegalStateException(
                    "Spring MySQL 临时用户清理失败，id="
                            + temporaryUserId
            );
        }
    }
}
