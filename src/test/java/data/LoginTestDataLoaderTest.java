package data;

import model.testcase.LoginFailureCase;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;


public class LoginTestDataLoaderTest {

    @Test
    public void shouldLoadLoginFailureCases() {

        List<LoginFailureCase> cases =
                LoginTestDataLoader.loadFailureCases();

        assertFalse(
                cases.isEmpty(),
                "登录失败测试数据不能为空"
        );

        LoginFailureCase firstCase = cases.get(0);

        assertEquals(
                firstCase.scenario(),
                "用户名为空"
        );

        assertEquals(
                firstCase.request().getUsername(),
                ""
        );

        assertEquals(
                firstCase.request().getPassword(),
                "123456"
        );

        assertEquals(
                firstCase.expectedHttpStatus(),
                400
        );

        assertEquals(
                firstCase.expectedCode(),
                1002
        );

        assertEquals(
                firstCase.expectedMessage(),
                "用户名不能为空"
        );
    }

    @Test
    public void shouldPreserveWhitespaceUsername() {

        List<LoginFailureCase> cases =
                LoginTestDataLoader.loadFailureCases();

        String username =
                cases.get(1)
                        .request()
                        .getUsername();

        assertEquals(
                username,
                "   ",
                "纯空格用户名必须被完整保留"
        );

        assertEquals(
                username.length(),
                3,
                "用户名应该包含3个空格"
        );
    }
}
