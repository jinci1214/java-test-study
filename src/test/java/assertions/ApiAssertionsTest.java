package assertions;

import io.restassured.builder.ResponseBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ApiAssertionsTest {

    @Test
    public void shouldPassWhenErrorResponseMatches() {

        Response response = buildErrorResponse(
                401,
                1001,
                "用户名或密码错误"
        );

        ApiAssertions.assertErrorResponse(
                response,
                401,
                1001,
                "用户名或密码错误",
                "密码错误"
        );
    }

    @Test
    public void shouldContainScenarioWhenHttpStatusIsWrong() {

        Response response = buildErrorResponse(
                500,
                1001,
                "用户名或密码错误"
        );

        AssertionError error = Assert.expectThrows(
                AssertionError.class,
                () -> ApiAssertions.assertErrorResponse(
                        response,
                        401,
                        1001,
                        "用户名或密码错误",
                        "密码错误"
                )
        );

        Assert.assertTrue(
                error.getMessage()
                        .contains("密码错误：HTTP状态码错误")
        );
    }

    private Response buildErrorResponse(
            int httpStatus,
            int businessCode,
            String message
    ) {

        String body = """
                  {
                    "code": %d,
                    "message": "%s"
                  }
                  """.formatted(
                businessCode,
                message
        );

        return new ResponseBuilder()
                .setStatusCode(httpStatus)
                .setContentType(ContentType.JSON)
                .setBody(body)
                .build();
    }
}
