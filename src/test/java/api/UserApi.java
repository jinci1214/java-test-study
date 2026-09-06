package api;


import common.RequestUtil;
import config.Config;
import io.restassured.response.Response;
import model.request.CreateUserRequest;

import java.io.File;

public class UserApi {


    private UserApi(){}

    public static Response getUser(){
        return getUserById(1);
    }

    public static Response getUserById(long userId) {
        validateUserId(userId);

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
        validateUserId(userId);

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
        validateUserId(userId);

        return RequestUtil.put(
                Config.getBaseUrl(),
                "/users/" + userId,
                request,
                true
        );
    }

    public static Response deleteUser(long userId) {
        validateUserId(userId);

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
        validatePaging(page, pageSize);

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
        validatePaging(page, pageSize);
        validateRole(role);

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
        validatePaging(page, pageSize);
        validateUsernameSortOrder(sortOrder);

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
        validatePaging(page, pageSize);
        validateRole(role);
        validateUsernameSortOrder(sortOrder);

        return RequestUtil.get(
                Config.getBaseUrl(),
                "/users?page=%d&pageSize=%d&role=%s&sortBy=username&sortOrder=%s"
                        .formatted(page, pageSize, role, sortOrder),
                true
        );
    }

    private static void validatePaging(int page, int pageSize) {
        if (page <= 0) {
            throw new IllegalArgumentException("页码必须大于0");
        }

        if (pageSize <= 0) {
            throw new IllegalArgumentException("每页数量必须大于0");
        }
    }

    private static void validateUserId(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
    }

    private static void validateRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("筛选角色不能为空");
        }
    }

    private static void validateUsernameSortOrder(String sortOrder) {
        if (!"asc".equals(sortOrder) && !"desc".equals(sortOrder)) {
            throw new IllegalArgumentException("用户名排序方向只能是 asc 或 desc");
        }
    }

}
