package tests;

import api.UserApi;
import assertions.ApiAssertions;
import base.BaseTest;

import common.TokenUtil;
import io.qameta.allure.*;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import mock.LoginMockServer;
import model.request.CreateUserRequest;
import model.response.ApiResponse;
import model.response.FileUploadData;
import model.response.UserData;
import model.response.UserPageData;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static io.qameta.allure.SeverityLevel.CRITICAL;
import static org.testng.Assert.*;


@Epic("接口自动化测试")
@Feature("用户管理")
public class UserTest extends BaseTest {

    @BeforeMethod
    public void resetMockScenarios() {
        LoginMockServer.resetScenarios();
    }



    @Test(groups = {
            "smoke",
            "regression"
    }

    )
    @Story("查询用户信息")
    @Severity(CRITICAL)
    @Description("携带正确Token查询用户信息")
    public void getUserTest(){

        assertTrue(TokenUtil.hasToken());

        Response response =
                UserApi.getUser();



        assertEquals(
                response.statusCode(),
                200
        );

        ApiResponse<UserData> userResponse = response.as(
                new TypeRef<ApiResponse<UserData>>() {
                }
        );

        assertEquals(
                userResponse.getCode(),
                0
        );
        assertEquals(
                userResponse.getData().getId(),
                1
        );
        assertEquals(
                userResponse.getData().getUsername(),
                "admin"
        );
        assertEquals(
                userResponse.getData().getRole(),
                "tester"
        );

        ApiAssertions.assertResponseMatchesSchema(
                response,
                "schemas/user-success-schema.json"
        );


    }

    @Test(groups = "regression")
    @Story("查询用户信息")
    @Severity(CRITICAL)
    @Description("查询不存在的用户，验证服务端返回未找到错误")
    public void getNonexistentUserTest() {
        Response response = UserApi.getUserById(999);

        ApiAssertions.assertErrorResponse(
                response,
                404,
                404,
                "用户不存在",
                "查询不存在的用户"
        );
    }

    @Test(groups = "regression")
    @Story("更新用户")
    @Severity(CRITICAL)
    @Description("携带正确Token更新用户，验证响应中的更新后数据")
    public void updateUserTest() {
        Response response = UserApi.updateUser(
                1,
                new CreateUserRequest("updated-admin", "tester")
        );

        assertEquals(response.statusCode(), 200);
        ApiResponse<UserData> userResponse = response.as(
                new TypeRef<ApiResponse<UserData>>() {
                }
        );

        assertEquals(userResponse.getCode(), 0);
        assertEquals(userResponse.getMessage(), "更新成功");
        assertEquals(userResponse.getData().getId(), 1);
        assertEquals(
                userResponse.getData().getUsername(),
                "updated-admin"
        );
        assertEquals(userResponse.getData().getRole(), "tester");

        ApiAssertions.assertResponseMatchesSchema(
                response,
                "schemas/user-success-schema.json"
        );
    }

    @Test(groups = "regression")
    @Story("更新用户")
    @Severity(CRITICAL)
    @Description("更新不存在的用户，验证服务端返回未找到错误")
    public void updateNonexistentUserTest() {
        Response response = UserApi.updateUser(
                999,
                new CreateUserRequest("updated-user", "tester")
        );

        ApiAssertions.assertErrorResponse(
                response,
                404,
                404,
                "用户不存在",
                "更新不存在的用户"
        );
    }

    @Test(
            dataProvider = "invalidTokenData",
            groups = {
                    "regression",
                    "auth"
            }
    )
    @Story("更新用户接口鉴权")
    @Severity(CRITICAL)
    @Description("验证缺少Token或Token错误时，更新用户接口拒绝访问")
    public void updateUserUnauthorizedTest(
            String token,
            String scenario
    ) {
        Response response = requestWithToken(
                token,
                () -> UserApi.updateUser(
                    1,
                    new CreateUserRequest("updated-admin", "tester")
                )
        );

        ApiAssertions.assertErrorResponse(
                response,
                401,
                401,
                "未授权访问",
                scenario
        );
    }

    @Test(groups = "regression")
    @Story("删除用户")
    @Severity(CRITICAL)
    @Description("携带正确Token删除用户，验证服务端返回无内容成功状态")
    public void deleteUserTest() {
        Response response = UserApi.deleteUser(1);

        assertEquals(response.statusCode(), 204);
        assertTrue(response.getBody().asString().isEmpty());
    }

    @Test(groups = "regression")
    @Story("删除用户")
    @Severity(CRITICAL)
    @Description("删除不存在的用户，验证服务端返回未找到错误")
    public void deleteNonexistentUserTest() {
        Response response = UserApi.deleteUser(999);

        ApiAssertions.assertErrorResponse(
                response,
                404,
                404,
                "用户不存在",
                "删除不存在的用户"
        );
    }

    @Test(
            dataProvider = "invalidTokenData",
            groups = {
                    "regression",
                    "auth"
            }
    )
    @Story("删除用户接口鉴权")
    @Severity(CRITICAL)
    @Description("验证缺少Token或Token错误时，删除用户接口拒绝访问")
    public void deleteUserUnauthorizedTest(
            String token,
            String scenario
    ) {
        Response response = requestWithToken(token, () -> UserApi.deleteUser(1));

        ApiAssertions.assertErrorResponse(
                response,
                401,
                401,
                "未授权访问",
                scenario
        );
    }

    @Test(groups = {"regression", "auth"})
    @Story("删除用户接口权限")
    @Severity(CRITICAL)
    @Description("普通测试人员Token删除用户时，验证服务端返回权限不足")
    public void deleteUserWithInsufficientPermissionTest() {
        Response response = requestWithToken(
                "tester-token",
                () -> UserApi.deleteUser(1)
        );

        ApiAssertions.assertErrorResponse(
                response,
                403,
                403,
                "权限不足",
                "普通测试人员删除用户"
        );
    }

    @Test(groups = "regression")
    @Story("创建用户")
    @Severity(CRITICAL)
    @Description("携带正确Token创建用户，验证响应中的新用户数据")
    public void createUserTest() {
        CreateUserRequest request = new CreateUserRequest("new-user", "tester");

        Response response = UserApi.createUser(request);

        assertEquals(response.statusCode(), 201);
        ApiResponse<UserData> userResponse = response.as(
                new TypeRef<ApiResponse<UserData>>() {
                }
        );

        assertEquals(userResponse.getCode(), 0);
        assertEquals(userResponse.getMessage(), "创建成功");
        assertEquals(userResponse.getData().getId(), 4);
        assertEquals(userResponse.getData().getUsername(), "new-user");
        assertEquals(userResponse.getData().getRole(), "tester");

        ApiAssertions.assertResponseMatchesSchema(
                response,
                "schemas/user-success-schema.json"
        );
    }

    @Test(dataProvider = "blankUsernameData", groups = "regression")
    @Story("创建用户")
    @Severity(CRITICAL)
    @Description("用户名为空时，验证服务端拒绝创建用户")
    public void createUserWithBlankUsernameTest(
            String username,
            String scenario
    ) {
        CreateUserRequest request = new CreateUserRequest(username, "tester");

        Response response = UserApi.createUser(request);

        ApiAssertions.assertErrorResponse(
                response,
                400,
                400,
                "用户名不能为空",
                scenario
        );
    }

    @Test(groups = "regression")
    @Story("创建用户")
    @Severity(CRITICAL)
    @Description("创建已存在用户名时，验证服务端返回冲突错误")
    public void createUserWithDuplicateUsernameTest() {
        Response response = UserApi.createUser(
                new CreateUserRequest("admin", "tester")
        );

        ApiAssertions.assertErrorResponse(
                response,
                409,
                409,
                "用户名已存在",
                "用户名重复"
        );
    }

    @Test(groups = "regression")
    @Story("创建用户")
    @Severity(CRITICAL)
    @Description("传入不支持的用户角色时，验证服务端拒绝创建用户")
    public void createUserWithUnsupportedRoleTest() {
        Response response = UserApi.createUser(
                new CreateUserRequest("new-user", "manager")
        );

        ApiAssertions.assertErrorResponse(
                response,
                400,
                400,
                "不支持的用户角色",
                "不支持的用户角色"
        );
    }

    @DataProvider(name = "blankUsernameData")
    public Object[][] blankUsernameData() {
        return new Object[][]{
                {"", "用户名为空字符串"},
                {"   ", "用户名只有空格"}
        };
    }

    @Test(
            dataProvider = "invalidTokenData",
            groups = {
                    "regression",
                    "auth"
            }
    )
    @Story("创建用户接口鉴权")
    @Severity(CRITICAL)
    @Description("验证缺少Token或Token错误时，创建用户接口拒绝访问")
    public void createUserUnauthorizedTest(
            String token,
            String scenario
    ) {
        Response response = requestWithToken(
                token,
                () -> UserApi.createUser(
                        new CreateUserRequest("new-user", "tester")
                )
        );

        ApiAssertions.assertErrorResponse(
                response,
                401,
                401,
                "未授权访问",
                scenario
        );
    }

    @Test(groups = {
            "smoke",
            "regression"
    })
    @Story("分页查询用户")
    @Severity(CRITICAL)
    @Description("携带正确Token查询第一页用户，验证分页元数据和用户列表")
    public void getUsersFirstPageTest() {
        Response response = UserApi.getUsers(1, 2);

        assertEquals(response.statusCode(), 200);
        ApiResponse<UserPageData> userListResponse = response.as(
                new TypeRef<ApiResponse<UserPageData>>() {
                }
        );

        assertEquals(userListResponse.getCode(), 0);
        assertEquals(userListResponse.getData().getPage(), 1);
        assertEquals(userListResponse.getData().getPageSize(), 2);
        assertEquals(userListResponse.getData().getTotal(), 3);
        assertEquals(userListResponse.getData().getItems().size(), 2);
        assertEquals(userListResponse.getData().getItems().getFirst().getId(), 1);
        assertEquals(
                userListResponse.getData().getItems().getFirst().getUsername(),
                "admin"
        );

        ApiAssertions.assertResponseMatchesSchema(
                response,
                "schemas/user-list-success-schema.json"
        );
    }

    @Test(groups = "regression")
    @Story("分页查询用户")
    @Severity(CRITICAL)
    @Description("携带正确Token查询每页一条数据，验证分页大小生效")
    public void getUsersWithPageSizeOneTest() {
        Response response = UserApi.getUsers(1, 1);

        assertEquals(response.statusCode(), 200);
        ApiResponse<UserPageData> userListResponse = response.as(
                new TypeRef<ApiResponse<UserPageData>>() {
                }
        );

        assertEquals(userListResponse.getCode(), 0);
        assertEquals(userListResponse.getData().getPage(), 1);
        assertEquals(userListResponse.getData().getPageSize(), 1);
        assertEquals(userListResponse.getData().getTotal(), 3);
        assertEquals(userListResponse.getData().getItems().size(), 1);
        assertEquals(userListResponse.getData().getItems().getFirst().getId(), 1);
    }

    @Test(groups = "regression")
    @Story("分页查询用户")
    @Severity(CRITICAL)
    @Description("查询第二页用户，验证最后一页返回剩余的一条数据")
    public void getUsersSecondPageTest() {
        Response response = UserApi.getUsers(2, 2);

        assertEquals(response.statusCode(), 200);
        ApiResponse<UserPageData> userListResponse = response.as(
                new TypeRef<ApiResponse<UserPageData>>() {
                }
        );

        assertEquals(userListResponse.getCode(), 0);
        assertEquals(userListResponse.getData().getPage(), 2);
        assertEquals(userListResponse.getData().getPageSize(), 2);
        assertEquals(userListResponse.getData().getTotal(), 3);
        assertEquals(userListResponse.getData().getItems().size(), 1);
        assertEquals(userListResponse.getData().getItems().getFirst().getId(), 3);
        assertEquals(
                userListResponse.getData().getItems().getFirst().getRole(),
                "developer"
        );
    }

    @Test(groups = "regression")
    @Story("分页查询用户")
    @Severity(CRITICAL)
    @Description("查询超出总页数的页面，验证接口正常返回空用户列表")
    public void getUsersOutOfRangePageTest() {
        Response response = UserApi.getUsers(3, 2);

        assertEquals(response.statusCode(), 200);
        ApiResponse<UserPageData> userListResponse = response.as(
                new TypeRef<ApiResponse<UserPageData>>() {
                }
        );

        assertEquals(userListResponse.getCode(), 0);
        assertEquals(userListResponse.getData().getPage(), 3);
        assertEquals(userListResponse.getData().getPageSize(), 2);
        assertEquals(userListResponse.getData().getTotal(), 3);
        assertTrue(userListResponse.getData().getItems().isEmpty());
    }

    @Test(groups = "regression")
    @Story("按角色筛选用户")
    @Severity(CRITICAL)
    @Description("按 tester 角色筛选用户，验证返回数据全部符合筛选条件")
    public void getUsersByRoleTest() {
        Response response = UserApi.getUsersByRole(1, 2, "tester");

        assertEquals(response.statusCode(), 200);
        ApiResponse<UserPageData> userListResponse = response.as(
                new TypeRef<ApiResponse<UserPageData>>() {
                }
        );

        assertEquals(userListResponse.getCode(), 0);
        assertEquals(userListResponse.getData().getPage(), 1);
        assertEquals(userListResponse.getData().getPageSize(), 2);
        assertEquals(userListResponse.getData().getTotal(), 2);
        assertEquals(userListResponse.getData().getItems().size(), 2);
        assertTrue(
                userListResponse.getData().getItems().stream()
                        .allMatch(user -> "tester".equals(user.getRole()))
        );
    }

    @Test(groups = "regression")
    @Story("按角色筛选用户")
    @Severity(CRITICAL)
    @Description("按 developer 角色筛选用户，验证只有符合条件的一条数据")
    public void getUsersByDeveloperRoleTest() {
        Response response = UserApi.getUsersByRole(1, 2, "developer");

        assertEquals(response.statusCode(), 200);
        ApiResponse<UserPageData> userListResponse = response.as(
                new TypeRef<ApiResponse<UserPageData>>() {
                }
        );

        assertEquals(userListResponse.getCode(), 0);
        assertEquals(userListResponse.getData().getTotal(), 1);
        assertEquals(userListResponse.getData().getItems().size(), 1);
        assertEquals(
                userListResponse.getData().getItems().getFirst().getUsername(),
                "developer"
        );
        assertEquals(
                userListResponse.getData().getItems().getFirst().getRole(),
                "developer"
        );
    }

    @Test(groups = "regression")
    @Story("按角色筛选用户")
    @Severity(CRITICAL)
    @Description("传入服务端不支持的角色，验证错误响应")
    public void getUsersWithUnsupportedRoleTest() {
        Response response = UserApi.getUsersByRole(1, 2, "manager");

        ApiAssertions.assertErrorResponse(
                response,
                400,
                400,
                "不支持的角色筛选条件",
                "不支持的角色"
        );
    }

    @Test(groups = "regression")
    @Story("按用户名排序用户")
    @Severity(CRITICAL)
    @Description("按用户名倒序查询用户，验证返回列表顺序符合排序规则")
    public void getUsersSortedByUsernameDescendingTest() {
        Response response = UserApi.getUsersSortedByUsername(1, 2, "desc");

        assertEquals(response.statusCode(), 200);
        ApiResponse<UserPageData> userListResponse = response.as(
                new TypeRef<ApiResponse<UserPageData>>() {
                }
        );

        assertEquals(userListResponse.getCode(), 0);
        assertEquals(userListResponse.getData().getPage(), 1);
        assertEquals(userListResponse.getData().getPageSize(), 2);
        assertEquals(userListResponse.getData().getTotal(), 3);
        assertEquals(
                userListResponse.getData().getItems().getFirst().getUsername(),
                "tester"
        );
        assertEquals(
                userListResponse.getData().getItems().get(1).getUsername(),
                "admin"
        );
    }

    @Test(groups = "regression")
    @Story("按用户名排序用户")
    @Severity(CRITICAL)
    @Description("按用户名升序查询用户，验证返回列表顺序符合排序规则")
    public void getUsersSortedByUsernameAscendingTest() {
        Response response = UserApi.getUsersSortedByUsername(1, 2, "asc");

        assertEquals(response.statusCode(), 200);
        ApiResponse<UserPageData> userListResponse = response.as(
                new TypeRef<ApiResponse<UserPageData>>() {
                }
        );

        assertEquals(userListResponse.getCode(), 0);
        assertEquals(userListResponse.getData().getPage(), 1);
        assertEquals(userListResponse.getData().getPageSize(), 2);
        assertEquals(userListResponse.getData().getTotal(), 3);
        assertEquals(
                userListResponse.getData().getItems().getFirst().getUsername(),
                "admin"
        );
        assertEquals(
                userListResponse.getData().getItems().get(1).getUsername(),
                "tester"
        );
    }

    @Test(groups = "regression")
    @Story("筛选并排序用户")
    @Severity(CRITICAL)
    @Description("按 tester 角色筛选并按用户名倒序，验证组合条件同时生效")
    public void getUsersByRoleSortedByUsernameTest() {
        Response response = UserApi.getUsersByRoleSortedByUsername(
                1,
                2,
                "tester",
                "desc"
        );

        assertEquals(response.statusCode(), 200);
        ApiResponse<UserPageData> userListResponse = response.as(
                new TypeRef<ApiResponse<UserPageData>>() {
                }
        );

        assertEquals(userListResponse.getCode(), 0);
        assertEquals(userListResponse.getData().getTotal(), 2);
        assertTrue(
                userListResponse.getData().getItems().stream()
                        .allMatch(user -> "tester".equals(user.getRole()))
        );
        assertEquals(
                userListResponse.getData().getItems().getFirst().getUsername(),
                "tester"
        );
        assertEquals(
                userListResponse.getData().getItems().get(1).getUsername(),
                "admin"
        );
    }

    @Test(
            dataProvider = "invalidTokenData",
            groups = {
                    "regression",
                    "auth"
            }
    )
    @Story("用户列表接口鉴权")
    @Severity(CRITICAL)
    @Description("验证缺少Token或Token错误时，用户列表接口拒绝访问")
    public void getUsersUnauthorizedTest(
            String token,
            String scenario
    ) {
        Response response = requestWithToken(
                token,
                () -> UserApi.getUsers(1, 2)
        );

        ApiAssertions.assertErrorResponse(
                response,
                401,
                401,
                "未授权访问",
                scenario
        );
    }

    @DataProvider(name = "invalidTokenData")
    public Object[][] invalidTokenData(){
        return new Object[][]{
                {null,"没有Token"},
                {"invalid-token","错误Token"}
        };
    }

    @Test(
            dataProvider = "invalidTokenData",
            groups = {
                    "regression",
                    "auth"
            }
    )
    @Story("用户接口鉴权")
    @Severity(CRITICAL)
    @Description("验证缺少Token或Token错误时，接口拒绝访问")
    public void getUserUnauthorizedTest(
            String token,
            String scenario
    ) {
        Response response = requestWithToken(token, UserApi::getUser);

        ApiAssertions.assertErrorResponse(
                response,
                401,
                401,
                "未授权访问",
                scenario
        );
    }

    @Test(groups = {"regression", "auth"})
    @Story("用户接口鉴权")
    @Severity(CRITICAL)
    @Description("验证Token过期时，接口返回专门的过期提示")
    public void getUserWithExpiredTokenTest() {
        Response response = requestWithToken("expired-token", UserApi::getUser);

        ApiAssertions.assertErrorResponse(
                response,
                401,
                401,
                "Token已过期",
                "Token过期"
        );
    }

    @Test(groups = {"regression", "auth"})
    @Story("查询用户接口权限")
    @Severity(CRITICAL)
    @Description("普通测试人员Token查询用户时，验证具备读取权限")
    public void getUserWithTesterPermissionTest() {
        Response response = requestWithToken("tester-token", UserApi::getUser);

        assertEquals(response.statusCode(), 200);
        ApiAssertions.assertResponseMatchesSchema(
                response,
                "schemas/user-success-schema.json"
        );
    }

    @Test(groups = "regression")
    @Story("用户生命周期")
    @Severity(CRITICAL)
    @Description("创建、查询、更新、删除同一用户，验证接口状态按业务流程变化")
    public void userLifecycleTest() {
        Response createResponse = UserApi.createUser(
                new CreateUserRequest("lifecycle-user", "tester")
        );
        assertEquals(createResponse.statusCode(), 201);

        Response queryAfterCreateResponse = UserApi.getUserById(100);
        assertEquals(queryAfterCreateResponse.statusCode(), 200);
        ApiResponse<UserData> queryAfterCreate = queryAfterCreateResponse.as(
                new TypeRef<ApiResponse<UserData>>() {
                }
        );
        assertEquals(
                queryAfterCreate.getData().getUsername(),
                "lifecycle-user"
        );

        Response updateResponse = UserApi.updateUser(
                100,
                new CreateUserRequest("lifecycle-user-updated", "developer")
        );
        assertEquals(updateResponse.statusCode(), 200);
        ApiResponse<UserData> updatedUser = updateResponse.as(
                new TypeRef<ApiResponse<UserData>>() {
                }
        );
        assertEquals(
                updatedUser.getData().getRole(),
                "developer"
        );

        Response queryAfterUpdateResponse = UserApi.getUserById(100);
        ApiResponse<UserData> queryAfterUpdate = queryAfterUpdateResponse.as(
                new TypeRef<ApiResponse<UserData>>() {
                }
        );
        assertEquals(queryAfterUpdateResponse.statusCode(), 200);
        assertEquals(
                queryAfterUpdate.getData().getUsername(),
                "lifecycle-user-updated"
        );
        assertEquals(queryAfterUpdate.getData().getRole(), "developer");

        Response deleteResponse = UserApi.deleteUser(100);
        assertEquals(deleteResponse.statusCode(), 204);

        Response queryAfterDeleteResponse = UserApi.getUserById(100);
        ApiAssertions.assertErrorResponse(
                queryAfterDeleteResponse,
                404,
                404,
                "用户不存在",
                "删除后查询用户"
        );
    }

    @Test(groups = "regression")
    @Story("上传用户文件")
    @Severity(CRITICAL)
    @Description("携带正确Token上传文本文件，验证服务端收到 multipart 文件内容")
    public void uploadUserFileTest() throws Exception {
        File file = getUploadSampleFile();

        Response response = UserApi.uploadUserFile(file);

        assertEquals(response.statusCode(), 201);
        ApiResponse<FileUploadData> uploadResponse = response.as(
                new TypeRef<ApiResponse<FileUploadData>>() {
                }
        );

        assertEquals(uploadResponse.getCode(), 0);
        assertEquals(uploadResponse.getMessage(), "上传成功");
        assertEquals(
                uploadResponse.getData().getFileName(),
                "upload-sample.txt"
        );
        assertEquals(
                uploadResponse.getData().getContentType(),
                "text/plain"
        );
        ApiAssertions.assertResponseMatchesSchema(
                response,
                "schemas/file-upload-success-schema.json"
        );
    }

    @Test(
            dataProvider = "invalidTokenData",
            groups = {"regression", "auth"}
    )
    @Story("上传用户文件鉴权")
    @Severity(CRITICAL)
    @Description("验证缺少Token或Token错误时，文件上传接口拒绝访问")
    public void uploadUserFileUnauthorizedTest(
            String token,
            String scenario
    ) throws Exception {
        File file = getUploadSampleFile();
        Response response = requestWithToken(
                token,
                () -> UserApi.uploadUserFile(file)
        );

        ApiAssertions.assertErrorResponse(
                response,
                401,
                401,
                "未授权访问",
                scenario
        );
    }

    @Test(groups = "regression")
    @Story("上传用户文件")
    @Severity(CRITICAL)
    @Description("上传不支持的文件类型时，验证服务端返回媒体类型错误")
    public void uploadUserFileWithUnsupportedContentTypeTest() throws Exception {
        Response response = UserApi.uploadUserFile(
                getUploadSampleFile(),
                "application/json"
        );

        ApiAssertions.assertErrorResponse(
                response,
                415,
                415,
                "不支持的文件类型",
                "上传不支持的文件类型"
        );
    }

    @Test(groups = "regression")
    @Story("下载用户文件")
    @Severity(CRITICAL)
    @Description("携带正确Token下载文件，验证文件类型、文件名和文件内容")
    public void downloadUserFileTest() throws Exception {
        Response response = UserApi.downloadUserFile("file-100");

        assertEquals(response.statusCode(), 200);
        assertEquals(response.getContentType(), "text/plain");
        assertEquals(
                response.getHeader("Content-Disposition"),
                "attachment; filename=upload-sample.txt"
        );

        Path downloadedFile = Files.createTempFile("downloaded-user-file-", ".txt");
        try {
            Files.write(downloadedFile, response.asByteArray());
            assertEquals(
                    Files.readString(downloadedFile),
                    "这是接口自动化上传测试文件。"
            );
        } finally {
            Files.deleteIfExists(downloadedFile);
        }
    }

    @Test(groups = "regression")
    @Story("下载用户文件")
    @Severity(CRITICAL)
    @Description("下载不存在的文件，验证服务端返回未找到错误")
    public void downloadNonexistentUserFileTest() {
        Response response = UserApi.downloadUserFile("missing-file");

        ApiAssertions.assertErrorResponse(
                response,
                404,
                404,
                "文件不存在",
                "下载不存在的文件"
        );
    }

    @Test(
            dataProvider = "invalidTokenData",
            groups = {"regression", "auth"}
    )
    @Story("下载用户文件鉴权")
    @Severity(CRITICAL)
    @Description("验证缺少Token或Token错误时，文件下载接口拒绝访问")
    public void downloadUserFileUnauthorizedTest(
            String token,
            String scenario
    ) {
        Response response = requestWithToken(
                token,
                () -> UserApi.downloadUserFile("file-100")
        );

        ApiAssertions.assertErrorResponse(
                response,
                401,
                401,
                "未授权访问",
                scenario
        );
    }

    private File getUploadSampleFile() throws Exception {
        return new File(
                getClass()
                        .getClassLoader()
                        .getResource("files/upload-sample.txt")
                        .toURI()
        );
    }

    private Response requestWithToken(
            String token,
            Supplier<Response> requestSender
    ) {
        String originalToken = TokenUtil.getToken();

        try {
            if (token == null) {
                TokenUtil.clear();
            } else {
                TokenUtil.setToken(token);
            }

            return requestSender.get();
        } finally {
            if (originalToken == null) {
                TokenUtil.clear();
            } else {
                TokenUtil.setToken(originalToken);
            }
        }
    }


}
