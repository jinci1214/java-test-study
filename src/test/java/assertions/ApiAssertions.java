package assertions;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;

public final class ApiAssertions {

    private ApiAssertions() {
    }

    public static void assertErrorResponse(
            Response response,
            int expectedHttpStatus,
            int expectedBusinessCode,
            String expectedMessage,
            String scenario
    ) {
        Assert.assertEquals(
                response.statusCode(),
                expectedHttpStatus,
                scenario + "：HTTP状态码错误"
        );
        JsonPath jsonPath = response.jsonPath();

        Assert.assertEquals(
                jsonPath.getInt("code"),
                expectedBusinessCode,
                scenario + "：业务状态码错误"
        );

        Assert.assertEquals(
                jsonPath.getString("message"),
                expectedMessage,
                scenario + "：错误消息不正确"
        );

    }

}
