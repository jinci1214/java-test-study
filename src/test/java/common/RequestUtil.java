package common;

import config.Config;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import report.AllureAttachmentUtil;

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
