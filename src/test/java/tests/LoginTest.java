package tests;

import api.LoginApi;
import assertions.ApiAssertions;
import base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import model.request.LoginRequest;
import model.response.LoginResponse;
import model.testcase.LoginFailureCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.qameta.allure.SeverityLevel.CRITICAL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.testng.Assert.*;


@Epic("接口自动化测试")
@Feature("用户认证")
public class LoginTest extends BaseTest {

    @Test(
            groups = {
                    "smoke",
                    "regression",
                    "auth"
            }
    )
    @Story("用户登录")
    @Severity(CRITICAL)
    @Description("使用正确的用户名和密码登录，验证相应结构和Token")
    public void loginSuccessTest() {
        LoginRequest request = new LoginRequest("admin", "123456");

        Response response = LoginApi.login(request);


        assertEquals(response.statusCode(), 200);

        LoginResponse loginResponse =
                response.as(LoginResponse.class);
        assertEquals(loginResponse.getCode(), 0);
        assertEquals(
                loginResponse.getMessage(),
                "success"
        );

        assertNotNull(loginResponse.getData());
        assertEquals(loginResponse.getData().getUsername(), "admin");

        String token =
                loginResponse.getData().getToken();

        assertNotNull(token);
        assertFalse(token.isBlank());

        response.then()
                .assertThat()
                .body(
                        matchesJsonSchemaInClasspath(
                                "schemas/login-success-schema.json"
                        )
                );
    }

    @DataProvider(name = "invalidLoginData")
    public Object[][] invalidLoginData() {
        return new Object[][]{
                {
                    new LoginFailureCase(
                        new LoginRequest("", "123456"),
                        400,
                        1002,
                        "用户名不能为空",
                        "用户名为空"
                    )
                },
                {
                    new LoginFailureCase(
                        new LoginRequest("   ", "123456"),
                        400,
                        1002,
                        "用户名不能为空",
                        "用户名全是空格"
                    )
                },
                {
                    new LoginFailureCase(
                        new LoginRequest("admin", ""),
                        400,
                        1003,
                        "密码不能为空",
                        "密码为空"
                    )
                },
                {
                    new LoginFailureCase(
                        new LoginRequest("admin", "wrong-password"),
                        401,
                        1001,
                        "用户名或密码错误",
                        "密码错误"
                    )
                },
                {
                    new LoginFailureCase(
                        new LoginRequest("unknown-user", "123456"),
                        401,
                        1001,
                        "用户名或密码错误",
                        "用户不存在"
                    )
                }
        };
    }


    @Test(
            dataProvider = "invalidLoginData",
            groups = {
                    "regression",
                    "auth"
            }
    )
    @Story("登录异常校验")
    @Severity(CRITICAL)
    @Description("使用无效或不完整的登录参数，验证接口错误响应")
    public void loginFailureTest(
            LoginFailureCase testCase
    ) {

        Allure.getLifecycle().updateTestCase(
                result -> result.setName(
                        "登录失败：" + testCase.scenario()
                )
        );
        Allure.parameter(
                "测试场景",
                testCase.scenario()
        );
        Allure.parameter(
                "请求对象",
                testCase.request().toString()
        );
        Allure.parameter(
                "预期HTTP状态码",
                testCase.expectedHttpStatus()
        );
        Allure.parameter(
                "预期业务码",
                testCase.expectedCode()
        );



        Response response =
                LoginApi.login(testCase.request());


        ApiAssertions.assertErrorResponse(
                response,
                testCase.expectedHttpStatus(),
                testCase.expectedCode(),
                testCase.expectedMessage(),
                testCase.scenario()
        );

        LoginResponse loginResponse =
                response.as(LoginResponse.class);

        assertNull(
                loginResponse.getData(),
                testCase.scenario() + "：登录失败不应该返回data"
        );
    }
}