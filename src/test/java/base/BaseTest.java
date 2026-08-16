package base;

import common.TokenUtil;
import config.Config;
import mock.LoginMockServer;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import service.AuthService;

public class BaseTest {

    @BeforeSuite
    public void beforeSuite() {

        System.out.println("当前测试环境："+ Config.getEnvironment());
        System.out.println("当前服务地址："+Config.getBaseUrl());
        LoginMockServer.start();

        AuthService.loginAndSaveToken(
                "admin",
                "123456"
        );
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        TokenUtil.clear();
        LoginMockServer.stop();
    }
}

