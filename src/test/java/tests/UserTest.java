package tests;

import api.UserApi;
import assertions.ApiAssertions;
import base.BaseTest;

import common.TokenUtil;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.qameta.allure.SeverityLevel.CRITICAL;
import static org.testng.Assert.*;


@Epic("接口自动化测试")
@Feature("用户管理")
public class UserTest extends BaseTest {




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

        assertEquals(
                response.jsonPath().getInt("code"),
                0
        );
        assertEquals(
                response.jsonPath().getInt("data.id"),
                1
        );
        assertEquals(
                response.jsonPath().getString("data.username"),
                "admin"
        );
        assertEquals(
                response.jsonPath().getString("data.role"),
                "tester"
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
        assertEquals(response.jsonPath().getInt("code"), 0);
        assertEquals(response.jsonPath().getInt("data.page"), 1);
        assertEquals(response.jsonPath().getInt("data.pageSize"), 2);
        assertEquals(response.jsonPath().getInt("data.total"), 3);
        assertEquals(response.jsonPath().getList("data.items").size(), 2);
        assertEquals(response.jsonPath().getInt("data.items[0].id"), 1);
        assertEquals(
                response.jsonPath().getString("data.items[0].username"),
                "admin"
        );
    }

    @Test(groups = "regression")
    @Story("分页查询用户")
    @Severity(CRITICAL)
    @Description("携带正确Token查询每页一条数据，验证分页大小生效")
    public void getUsersWithPageSizeOneTest() {
        Response response = UserApi.getUsers(1, 1);

        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getInt("code"), 0);
        assertEquals(response.jsonPath().getInt("data.page"), 1);
        assertEquals(response.jsonPath().getInt("data.pageSize"), 1);
        assertEquals(response.jsonPath().getInt("data.total"), 3);
        assertEquals(response.jsonPath().getList("data.items").size(), 1);
        assertEquals(response.jsonPath().getInt("data.items[0].id"), 1);
    }

    @Test(groups = "regression")
    @Story("分页查询用户")
    @Severity(CRITICAL)
    @Description("查询第二页用户，验证最后一页返回剩余的一条数据")
    public void getUsersSecondPageTest() {
        Response response = UserApi.getUsers(2, 2);

        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getInt("code"), 0);
        assertEquals(response.jsonPath().getInt("data.page"), 2);
        assertEquals(response.jsonPath().getInt("data.pageSize"), 2);
        assertEquals(response.jsonPath().getInt("data.total"), 3);
        assertEquals(response.jsonPath().getList("data.items").size(), 1);
        assertEquals(response.jsonPath().getInt("data.items[0].id"), 3);
        assertEquals(
                response.jsonPath().getString("data.items[0].role"),
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
        assertEquals(response.jsonPath().getInt("code"), 0);
        assertEquals(response.jsonPath().getInt("data.page"), 3);
        assertEquals(response.jsonPath().getInt("data.pageSize"), 2);
        assertEquals(response.jsonPath().getInt("data.total"), 3);
        assertEquals(response.jsonPath().getList("data.items").size(), 0);
    }

    @Test(groups = "regression")
    @Story("按角色筛选用户")
    @Severity(CRITICAL)
    @Description("按 tester 角色筛选用户，验证返回数据全部符合筛选条件")
    public void getUsersByRoleTest() {
        Response response = UserApi.getUsersByRole(1, 2, "tester");

        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getInt("code"), 0);
        assertEquals(response.jsonPath().getInt("data.page"), 1);
        assertEquals(response.jsonPath().getInt("data.pageSize"), 2);
        assertEquals(response.jsonPath().getInt("data.total"), 2);
        assertEquals(response.jsonPath().getList("data.items").size(), 2);
        assertEquals(
                response.jsonPath().getList("data.items.role"),
                java.util.List.of("tester", "tester")
        );
    }

    @Test(groups = "regression")
    @Story("按角色筛选用户")
    @Severity(CRITICAL)
    @Description("按 developer 角色筛选用户，验证只有符合条件的一条数据")
    public void getUsersByDeveloperRoleTest() {
        Response response = UserApi.getUsersByRole(1, 2, "developer");

        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getInt("code"), 0);
        assertEquals(response.jsonPath().getInt("data.total"), 1);
        assertEquals(response.jsonPath().getList("data.items").size(), 1);
        assertEquals(response.jsonPath().getString("data.items[0].username"), "developer");
        assertEquals(response.jsonPath().getString("data.items[0].role"), "developer");
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
        assertEquals(response.jsonPath().getInt("code"), 0);
        assertEquals(response.jsonPath().getInt("data.page"), 1);
        assertEquals(response.jsonPath().getInt("data.pageSize"), 2);
        assertEquals(response.jsonPath().getInt("data.total"), 3);
        assertEquals(
                response.jsonPath().getList("data.items.username"),
                java.util.List.of("tester", "admin")
        );
    }

    @Test(groups = "regression")
    @Story("按用户名排序用户")
    @Severity(CRITICAL)
    @Description("按用户名升序查询用户，验证返回列表顺序符合排序规则")
    public void getUsersSortedByUsernameAscendingTest() {
        Response response = UserApi.getUsersSortedByUsername(1, 2, "asc");

        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getInt("code"), 0);
        assertEquals(response.jsonPath().getInt("data.page"), 1);
        assertEquals(response.jsonPath().getInt("data.pageSize"), 2);
        assertEquals(response.jsonPath().getInt("data.total"), 3);
        assertEquals(
                response.jsonPath().getList("data.items.username"),
                java.util.List.of("admin", "tester")
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
        assertEquals(response.jsonPath().getInt("code"), 0);
        assertEquals(response.jsonPath().getInt("data.total"), 2);
        assertEquals(
                response.jsonPath().getList("data.items.role"),
                java.util.List.of("tester", "tester")
        );
        assertEquals(
                response.jsonPath().getList("data.items.username"),
                java.util.List.of("tester", "admin")
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
        String originalToken = TokenUtil.getToken();

        try {
            if (token == null) {
                TokenUtil.clear();
            } else {
                TokenUtil.setToken(token);
            }

            Response response = UserApi.getUsers(1, 2);

            ApiAssertions.assertErrorResponse(
                    response,
                    401,
                    401,
                    "未授权访问",
                    scenario
            );
        } finally {
            TokenUtil.setToken(originalToken);
        }
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

        // 保存原来的Token，防止影响其他测试
        String originalToken = TokenUtil.getToken();

        try {

            if(token == null){
                TokenUtil.clear();
            }else{
                TokenUtil.setToken(token);
            }


            Response response = UserApi.getUser();


            ApiAssertions.assertErrorResponse(
                    response,
                    401,
                    401,
                    "未授权访问",
                    scenario
            );

        } finally {
            // 无论测试成功还是失败，都恢复Token
            TokenUtil.setToken(originalToken);
        }
    }


}
