import api.UserApi;
import base.BaseTest;

import common.TokenUtil;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.qameta.allure.SeverityLevel.CRITICAL;
import static org.testng.Assert.*;


@Epic("接口自动化测试")
@Feature("用户管理")
public class UserTest extends BaseTest {




    @Test(groups = {
            "smoke",
            "regression"
    }

    )
    @Story("查询用户信息")
    @Severity(CRITICAL)
    @Description("携带正确Token查询用户信息")
    public void getUserTest(){

        assertTrue(TokenUtil.hasToken());

        Response response =
                UserApi.getUser();



        assertEquals(
                response.statusCode(),
                200
        );

        assertEquals(
                response.jsonPath().getInt("code"),
                0
        );
        assertEquals(
                response.jsonPath().getInt("data.id"),
                1
        );
        assertEquals(
                response.jsonPath().getString("data.username"),
                "admin"
        );
        assertEquals(
                response.jsonPath().getString("data.role"),
                "tester"
        );


    }

    @DataProvider(name = "invalidTokenData")
    public Object[][] invalidTokenData(){
        return new Object[][]{
                {null,"没有Token"},
                {"invalid-token","错误Token"}
        };
    }

    @Test(
            dataProvider = "invalidTokenData",
            groups = {
                    "regression",
                    "auth"
            }
    )
    @Story("用户接口鉴权")
    @Severity(CRITICAL)
    @Description("验证缺少Token或Token错误时，接口拒绝访问")
    public void getUserUnauthorizedTest(
            String token,
            String scenario
    ) {

        // 保存原来的Token，防止影响其他测试
        String originalToken = TokenUtil.getToken();

        try {

            if(token == null){
                TokenUtil.clear();
            }else{
                TokenUtil.setToken(token);
            }


            Response response = UserApi.getUser();


            assertEquals(
                    response.statusCode(),
                    401,
                    scenario+"时，HTTP状态码应该是401"
            );
            assertEquals(
                    response.jsonPath().getInt("code"),
                    401,
                    scenario+"时，业务状态码应该是401"
            );
            assertEquals(
                    response.jsonPath().getString("message"),
                    "未授权访问",
                    scenario+"时，错误信息不正确"
            );

        } finally {
            // 无论测试成功还是失败，都恢复Token
            TokenUtil.setToken(originalToken);
        }
    }


}