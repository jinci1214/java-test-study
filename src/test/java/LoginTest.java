import api.LoginApi;
import base.BaseTest;
import io.restassured.response.Response;
import model.request.LoginRequest;
import model.response.LoginResponse;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class LoginTest extends BaseTest {

    @Test
    public void loginSuccessTest() {
        LoginRequest request = new LoginRequest("admin", "123456");

        Response response = LoginApi.login(request);

        System.out.println(response.asPrettyString());

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
    }

    @DataProvider(name = "invalidLoginData")
    public Object[][] invalidLoginData() {
        return new Object[][]{
                {
                        "",
                        "123456",
                        400,
                        1002,
                        "用户名不能为空",
                        "用户名为空"
                },
                {
                        "   ",
                        "123456",
                        400,
                        1002,
                        "用户名不能为空",
                        "用户名全是空格"
                },
                {
                        "admin",
                        "",
                        400,
                        1003,
                        "密码不能为空",
                        "密码为空"
                },
                {
                        "admin",
                        "wrong-password",
                        401,
                        1001,
                        "用户名或密码错误",
                        "密码错误"
                },
                {
                        "unknown-user",
                        "123456",
                        401,
                        1001,
                        "用户名或密码错误",
                        "用户不存在"
                }
        };
    }


    @Test(dataProvider = "invalidLoginData")
    public void loginFailureTest(
            String username,
            String password,
            int expectedHttpStatus,
            int expectedCode,
            String expectedMessage,
            String scenario
    ) {
        LoginRequest request =
                new LoginRequest(username, password);

        Response response =
                LoginApi.login(request);

        System.out.println("当前登录场景：" + scenario);
        System.out.println(response.asPrettyString());

        assertEquals(
                response.statusCode(),
                expectedHttpStatus,
                scenario + "：HTTP状态码错误"
        );

        LoginResponse loginResponse =
                response.as(LoginResponse.class);

        assertEquals(
                loginResponse.getCode(),
                expectedCode,
                scenario + "：业务状态码错误"
        );

        assertEquals(
                loginResponse.getMessage(),
                expectedMessage,
                scenario + "：错误消息不正确"
        );

        assertNull(
                loginResponse.getData(),
                scenario + "：登录失败不应该返回data"
        );
    }
}