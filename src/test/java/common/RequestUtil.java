package common;

import config.Config;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import report.AllureAttachmentUtil;
import validation.TimeoutValidator;

import java.io.File;

import static io.restassured.RestAssured.*;


public class RequestUtil {

    private RequestUtil(){}

    private static final Logger log =
            LoggerFactory.getLogger(RequestUtil.class);

    private static RequestSpecification createRequest(
            String baseUrl,
            boolean needAuth
    ){
        RequestSpecification request = given()
                .baseUri(baseUrl)
                .contentType("application/json");

        if(needAuth && TokenUtil.hasToken()){
            request.header(
                    "Authorization",
                    "Bearer "+TokenUtil.getToken()
            );
        }
        return request;
    }
    public static Response get(String path,boolean needAuth){

        return get(
                Config.getBaseUrl(),
                path,
                needAuth
        );
    }

    public static Response getWithTimeout(
            String baseUrl,
            String path,
            boolean needAuth,
            int timeoutMillis
    ) {
        TimeoutValidator.validate(timeoutMillis);

        boolean hasAuthorization =
                needAuth && TokenUtil.hasToken();

        AllureAttachmentUtil.attachRequest(
                "GET",
                baseUrl + path,
                hasAuthorization,
                "超时时间：" + timeoutMillis + " 毫秒"
        );

        log.info(
                "发送HTTP请求：method=GET,url={},timeout={}ms",
                baseUrl + path,
                timeoutMillis
        );
        Response response = createRequest(baseUrl, needAuth)
                .config(
                        RestAssuredConfig.config().httpClient(
                                HttpClientConfig.httpClientConfig()
                                        .setParam(
                                                "http.connection.timeout",
                                                timeoutMillis
                                        )
                                        .setParam(
                                                "http.socket.timeout",
                                                timeoutMillis
                                        )
                        )
                )
                .when()
                .get(path);

        AllureAttachmentUtil.attachResponse("GET", response);

        log.info(
                "收到HTTP响应，method=GET,path={},status={}",
                path,
                response.statusCode()
        );
        return response;
    }

    public static Response get(
            String baseUrl,
            String path,
            boolean needAuth
    ){
        boolean hasAuthorization =
                needAuth && TokenUtil.hasToken();

        AllureAttachmentUtil.attachRequest(
                "GET",
                baseUrl + path,
                hasAuthorization,
                null
        );

        log.info(
                "发送HTTP请求：method=GET,url={}{}",
                baseUrl,
                path
        );
        Response response =
                createRequest(baseUrl,needAuth)
                        .when()
                        .get(path);

        AllureAttachmentUtil.attachResponse(
                "GET",
                response
        );

        log.info(
                "收到HTTP响应：method=GET,path={},status={}",
                path,
                response.statusCode()
        );

       return response;
    }

    public static Response post(String path,Object body,boolean needAuth){

        return post(
                Config.getBaseUrl(),
                path,
                body,
                needAuth
        );
    }

    public static Response post(
            String baseUrl,
            String path,
            Object body,
            boolean needAuth
    ){
        boolean hasAuthorization =
                needAuth && TokenUtil.hasToken();

        AllureAttachmentUtil.attachRequest(
                "POST",
                baseUrl + path,
                hasAuthorization,
                body
        );

        log.info(
                "发送HTTP请求：method=POST,url={}{}",
                baseUrl,
                path
        );
        Response response =
                createRequest(baseUrl,needAuth)
                        .body(body)
                        .when()
                        .post(path);

        AllureAttachmentUtil.attachResponse(
                "POST",
                response
        );

        log.info(
                "收到HTTP响应，method=POST,path={},status={}",
                path,
                response.statusCode()
        );
        return response;
    }

    public static Response postMultipart(
            String baseUrl,
            String path,
            String fieldName,
            File file,
            String contentType,
            boolean needAuth
    ) {
        boolean hasAuthorization =
                needAuth && TokenUtil.hasToken();

        AllureAttachmentUtil.attachMultipartRequest(
                "POST",
                baseUrl + path,
                hasAuthorization,
                fieldName,
                file,
                contentType
        );

        log.info(
                "发送HTTP请求：method=POST,url={},file={}",
                baseUrl + path,
                file.getName()
        );
        Response response = createRequest(baseUrl, needAuth)
                .contentType("multipart/form-data")
                .multiPart(fieldName, file, contentType)
                .when()
                .post(path);

        AllureAttachmentUtil.attachResponse("POST", response);

        log.info(
                "收到HTTP响应，method=POST,path={},status={}",
                path,
                response.statusCode()
        );
        return response;
    }

    public static Response put(
            String baseUrl,
            String path,
            Object body,
            boolean needAuth
    ) {
        boolean hasAuthorization =
                needAuth && TokenUtil.hasToken();

        AllureAttachmentUtil.attachRequest(
                "PUT",
                baseUrl + path,
                hasAuthorization,
                body
        );

        log.info(
                "发送HTTP请求：method=PUT,url={}{}",
                baseUrl,
                path
        );
        Response response =
                createRequest(baseUrl, needAuth)
                        .body(body)
                        .when()
                        .put(path);

        AllureAttachmentUtil.attachResponse("PUT", response);

        log.info(
                "收到HTTP响应，method=PUT,path={},status={}",
                path,
                response.statusCode()
        );
        return response;
    }

    public static Response delete(
            String baseUrl,
            String path,
            boolean needAuth
    ) {
        boolean hasAuthorization =
                needAuth && TokenUtil.hasToken();

        AllureAttachmentUtil.attachRequest(
                "DELETE",
                baseUrl + path,
                hasAuthorization,
                null
        );

        log.info(
                "发送HTTP请求：method=DELETE,url={}{}",
                baseUrl,
                path
        );
        Response response =
                createRequest(baseUrl, needAuth)
                        .when()
                        .delete(path);

        AllureAttachmentUtil.attachResponse("DELETE", response);

        log.info(
                "收到HTTP响应，method=DELETE,path={},status={}",
                path,
                response.statusCode()
        );
        return response;
    }

}
