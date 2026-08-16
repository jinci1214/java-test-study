package common;

import config.Config;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.*;


public class RequestUtil {

    private RequestUtil(){}

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
        return createRequest(baseUrl,needAuth)
                .when()
                .get(path);
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
        return createRequest(baseUrl,needAuth)
                .body(body)
                .when()
                .post(path);
    }

}
