package common;

import config.Config;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        log.info(
                "发送HTTP请求：method=GET,url={}{}",
                baseUrl,
                path
        );
        Response response =
                createRequest(baseUrl,needAuth)
                        .when()
                        .get(path);
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

        log.info(
                "收到HTTP响应，method=POST,path={},status={}",
                path,
                response.statusCode()
        );
        return response;
    }

}
