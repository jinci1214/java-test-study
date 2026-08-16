package api;

import common.RequestUtil;
import config.Config;
import io.restassured.response.Response;

public class LoginApi {



    private LoginApi() {
    }

    public static Response login(Object body) {
        return RequestUtil.post(
                Config.getBaseUrl(),
                "/login",
                body,
                false
        );
    }
}