package application.user;

import application.JavaTestStudyApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders
        .delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders
        .get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders
        .post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders
        .put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers
        .content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers
        .jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers
        .status;

@SpringBootTest(classes = JavaTestStudyApplication.class)
@AutoConfigureMockMvc
public class UserControllerTest
        extends AbstractTestNGSpringContextTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test(groups = {"spring", "regression"})
    public void shouldReturnUsersInIdOrder() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].username").value("tester"));
    }

    @Test(groups = {"spring", "regression"})
    public void shouldReturnUserWhenIdExists() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(
                        jsonPath("$.role").value("administrator")
                );
    }

    @Test(groups = {"spring", "regression"})
    public void shouldReturnNotFoundWhenIdDoesNotExist()
            throws Exception {
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(
                        jsonPath("$.message").value("用户不存在，id=999")
                )
                .andExpect(jsonPath("$.path").value("/api/users/999"));
    }

    @Test(groups = {"spring", "regression"})
    public void shouldCreateUserWhenRequestIsValid() throws Exception {
        String requestBody = """
                {
                  "username": "new-user",
                  "role": "tester"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("new-user"))
                .andExpect(jsonPath("$.role").value("tester"));
    }

    @Test(groups = {"spring", "regression"})
    public void shouldRejectBlankUsername() throws Exception {
        String requestBody = """
                {
                  "username": " ",
                  "role": "tester"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(
                        jsonPath("$.message").value("用户名不能为空")
                )
                .andExpect(jsonPath("$.path").value("/api/users"));
    }

    @Test(groups = {"spring", "regression"})
    public void shouldRejectDuplicateUsernameOnCreate()
            throws Exception {
        String requestBody = """
                {
                  "username": "admin",
                  "role": "tester"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(
                        jsonPath("$.code")
                                .value("USERNAME_ALREADY_EXISTS")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("用户名已存在：admin")
                )
                .andExpect(jsonPath("$.path").value("/api/users"));
    }

    @Test(groups = {"spring", "regression"})
    public void shouldUpdateUserWhenRequestIsValid() throws Exception {
        long userId = createUserAndReturnId();
        String requestBody = """
                {
                  "username": "updated-user",
                  "role": "administrator"
                }
                """;

        mockMvc.perform(
                        put("/api/users/{id}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(
                        jsonPath("$.username").value("updated-user")
                )
                .andExpect(
                        jsonPath("$.role").value("administrator")
                );

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(
                        jsonPath("$.username").value("updated-user")
                )
                .andExpect(
                        jsonPath("$.role").value("administrator")
                );
    }

    @Test(groups = {"spring", "regression"})
    public void shouldReturnNotFoundWhenUpdatingMissingUser()
            throws Exception {
        String requestBody = """
                {
                  "username": "missing-user",
                  "role": "tester"
                }
                """;

        mockMvc.perform(
                        put("/api/users/999999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(
                        jsonPath("$.path").value("/api/users/999999")
                );
    }

    @Test(groups = {"spring", "regression"})
    public void shouldRejectDuplicateUsernameOnUpdate()
            throws Exception {
        long userId = createUserAndReturnId();
        String requestBody = """
                {
                  "username": "admin",
                  "role": "tester"
                }
                """;

        mockMvc.perform(
                        put("/api/users/{id}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(
                        jsonPath("$.code")
                                .value("USERNAME_ALREADY_EXISTS")
                )
                .andExpect(
                        jsonPath("$.path").value("/api/users/" + userId)
                );
    }

    @Test(groups = {"spring", "regression"})
    public void shouldDeleteUserAndReturnNotFoundAfterDeletion()
            throws Exception {
        long userId = createUserAndReturnId();

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(
                        jsonPath("$.path").value("/api/users/" + userId)
                );
    }

    private long createUserAndReturnId() throws Exception {
        String username = "temporary-user-" + UUID.randomUUID();
        String requestBody = """
                {
                  "username": "%s",
                  "role": "tester"
                }
                """.formatted(username);

        String responseBody = mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper
                .readTree(responseBody)
                .get("id")
                .asLong();
    }
}
