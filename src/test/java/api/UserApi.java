package api;


import common.RequestUtil;
import config.Config;
import io.restassured.response.Response;

public class UserApi {


    private UserApi(){}

    public static Response getUser(){
        return RequestUtil.get(
                Config.getBaseUrl(),
                "/users/1",
                true
        );
    }

}
