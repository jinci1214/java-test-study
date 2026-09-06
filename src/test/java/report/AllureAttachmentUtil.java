package report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.io.ByteArrayInputStream;
import java.io.File;

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

    public static void attachMultipartRequest(
            String method,
            String url,
            boolean hasAuthorization,
            String fieldName,
            File file,
            String contentType
    ) {

        if (!hasActiveTest()) {
            return;
        }

        String content = """
                Method: %s
                URL: %s
                Authorization: %s
                Multipart field: %s
                File name: %s
                File size: %d bytes
                Content-Type: %s
                """.formatted(
                method,
                url,
                hasAuthorization ? "***" : "未携带",
                fieldName,
                file.getName(),
                file.length(),
                contentType
        );

        addTextAttachment(
                "HTTP请求：" + method + "（multipart）",
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

    public static void attachFile(
            String name,
            String contentType,
            String extension,
            byte[] content
    ) {

        if (!hasActiveTest()) {
            return;
        }

        Allure.addAttachment(
                name,
                contentType,
                new ByteArrayInputStream(content),
                extension
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
