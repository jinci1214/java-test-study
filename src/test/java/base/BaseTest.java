package base;

import common.TokenUtil;
import config.Config;
import mock.LoginMockServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import service.AuthService;

public class BaseTest {

    private static final Logger log =
            LoggerFactory.getLogger(BaseTest.class);

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {

        log.info(
                "测试套件开始，环境：{},服务环境：{}",
                Config.getEnvironment(),
                Config.getBaseUrl()

        );
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
        log.info("测试套件结束");
    }
}

