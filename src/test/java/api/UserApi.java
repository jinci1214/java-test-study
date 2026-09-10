package api;


import common.RequestUtil;
import config.Config;
import io.restassured.response.Response;
import model.request.CreateUserRequest;
import validation.PaginationValidator;
import validation.RoleValidator;
import validation.UserIdValidator;
import validation.UsernameSortOrderValidator;

import java.io.File;

public class UserApi {


    private UserApi(){}

    public static Response getUser(){
        return getUserById(1);
    }

    public static Response getUserById(long userId) {
        UserIdValidator.validate(userId);

        return RequestUtil.get(
                Config.getBaseUrl(),
                "/users/" + userId,
                true
        );
    }

    public static Response getUserByIdWithTimeout(
            long userId,
            int timeoutMillis
    ) {
        UserIdValidator.validate(userId);

        return RequestUtil.getWithTimeout(
                Config.getBaseUrl(),
                "/users/" + userId,
                true,
                timeoutMillis
        );
    }

    public static Response createUser(CreateUserRequest request) {
        return RequestUtil.post(
                Config.getBaseUrl(),
                "/users",
                request,
                true
        );
    }

    public static Response updateUser(long userId, CreateUserRequest request) {
        UserIdValidator.validate(userId);

        return RequestUtil.put(
                Config.getBaseUrl(),
                "/users/" + userId,
                request,
                true
        );
    }

    public static Response deleteUser(long userId) {
        UserIdValidator.validate(userId);

        return RequestUtil.delete(
                Config.getBaseUrl(),
                "/users/" + userId,
                true
        );
    }

    public static Response uploadUserFile(File file) {
        return uploadUserFile(file, "text/plain");
    }

    public static Response uploadUserFile(File file, String contentType) {
        return RequestUtil.postMultipart(
                Config.getBaseUrl(),
                "/files",
                "file",
                file,
                contentType,
                true
        );
    }

    public static Response downloadUserFile(String fileId) {
        return RequestUtil.get(
                Config.getBaseUrl(),
                "/files/" + fileId + "/download",
                true
        );
    }

    public static Response deleteUserFile(String fileId) {
        return RequestUtil.delete(
                Config.getBaseUrl(),
                "/files/" + fileId,
                true
        );
    }

    public static Response getUsers(int page, int pageSize) {
        PaginationValidator.validate(page, pageSize);

        return RequestUtil.get(
                Config.getBaseUrl(),
                "/users?page=%d&pageSize=%d".formatted(page, pageSize),
                true
        );
    }

    public static Response getUsersByRole(
            int page,
            int pageSize,
            String role
    ) {
        PaginationValidator.validate(page, pageSize);
        RoleValidator.validate(role);

        return RequestUtil.get(
                Config.getBaseUrl(),
                "/users?page=%d&pageSize=%d&role=%s".formatted(
                        page,
                        pageSize,
                        role
                ),
                true
        );
    }

    public static Response getUsersSortedByUsername(
            int page,
            int pageSize,
            String sortOrder
    ) {
        PaginationValidator.validate(page, pageSize);
        UsernameSortOrderValidator.validate(sortOrder);

        return RequestUtil.get(
                Config.getBaseUrl(),
                "/users?page=%d&pageSize=%d&sortBy=username&sortOrder=%s"
                        .formatted(page, pageSize, sortOrder),
                true
        );
    }

    public static Response getUsersByRoleSortedByUsername(
            int page,
            int pageSize,
            String role,
            String sortOrder
    ) {
        PaginationValidator.validate(page, pageSize);
        RoleValidator.validate(role);
        UsernameSortOrderValidator.validate(sortOrder);

        return RequestUtil.get(
                Config.getBaseUrl(),
                "/users?page=%d&pageSize=%d&role=%s&sortBy=username&sortOrder=%s"
                        .formatted(page, pageSize, role, sortOrder),
                true
        );
    }

}
