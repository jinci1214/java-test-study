package report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.testng.ITestResult;
import org.testng.Reporter;

public final class AllureAttachmentUtil {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper();

    private AllureAttachmentUtil() {
    }

    public static void attachRequest(
            String method,
            String url,
            boolean hasAuthorization,
            Object body
    ) {

        if (!hasActiveTest()) {
            return;
        }

        String content = """
                Method: %s
                URL: %s
                Authorization: %s
                Body:
                %s
                """.formatted(
                method,
                url,
                hasAuthorization ? "***" : "未携带",
                bodyToText(body)
        );

        addTextAttachment(
                "HTTP请求：" + method,
                content
        );
    }

    public static void attachResponse(
            String method,
            Response response
    ) {

        if (!hasActiveTest()) {
            return;
        }

        String content = """
                Status: %d
                Content-Type: %s
                Body:
                %s
                """.formatted(
                response.statusCode(),
                response.contentType(),
                response.asPrettyString()
        );

        addTextAttachment(
                "HTTP响应：" + method,
                content
        );
    }

    public static void attachDatabaseOperation(
            String operation,
            String sql,
            String parameters,
            String result
    ) {

        if (!hasActiveTest()) {
            return;
        }

        String content = """
                SQL:
                %s
                Parameters:
                %s
                Result:
                %s
                """.formatted(
                sql,
                parameters == null ? "<无>" : parameters,
                result
        );

        addTextAttachment(
                "数据库操作：" + operation,
                content
        );
    }

    private static String bodyToText(Object body) {

        if (body == null) {
            return "<无请求体>";
        }

        try {
            return OBJECT_MAPPER
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(body);
        } catch (JsonProcessingException exception) {
            return String.valueOf(body);
        }
    }

    private static void addTextAttachment(
            String name,
            String content
    ) {

        String maskedContent =
                SensitiveDataMasker.mask(content);

        Allure.addAttachment(
                name,
                "text/plain",
                maskedContent
        );
    }

    private static boolean hasActiveTest() {

        ITestResult currentTestResult =
                Reporter.getCurrentTestResult();

        return currentTestResult != null
                && currentTestResult.getMethod() != null
                && currentTestResult.getMethod().isTest()
                && Allure.getLifecycle()
                        .getCurrentTestCase()
                        .isPresent();
    }
}
