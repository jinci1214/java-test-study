package assertions;

import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import model.response.ApiResponse;
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
        ApiResponse<Void> errorResponse = response.as(
                new TypeRef<ApiResponse<Void>>() {
                }
        );

        Assert.assertEquals(
                errorResponse.getCode(),
                expectedBusinessCode,
                scenario + "：业务状态码错误"
        );

        Assert.assertEquals(
                errorResponse.getMessage(),
                expectedMessage,
                scenario + "：错误消息不正确"
        );

    }

}
