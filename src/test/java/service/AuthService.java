package service;

import api.LoginApi;
import common.TokenUtil;
import io.restassured.response.Response;
import model.request.LoginRequest;
import model.response.LoginResponse;

import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private AuthService(){}

    public static String loginAndSaveToken(
            String username,
            String password
    ){
        LoginRequest request =
                new LoginRequest(username,password);

        Response response = LoginApi.login(request);

        if(response.statusCode() !=200){
            throw new IllegalStateException(
                    "登录失败，HTTP状态码："+response.statusCode()
                    +",响应："+response.asString()
            );
        }

        LoginResponse loginResponse =
                response.as(LoginResponse.class);

        if(loginResponse.getCode() != 0){
            throw new IllegalStateException(
                    "登录业务失败："+loginResponse.getMessage()
            );
        }
        if(loginResponse.getData() == null){
            throw new IllegalStateException(
                    "登录响应中缺少data"
            );
        }

        String token = loginResponse.getData().getToken();

        TokenUtil.setToken(token);
        return token;

    }
}
