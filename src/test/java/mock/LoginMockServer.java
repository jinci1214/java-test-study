package mock;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public final class LoginMockServer {

    private static final int PORT = 8089;

    private static WireMockServer server;

    private LoginMockServer() {
    }

    public static void start() {
        server = new WireMockServer(PORT);
        server.start();

        server.stubFor(
                post(urlEqualTo("/login"))
                        .atPriority(1)
                        .withRequestBody(
                                matchingJsonPath(
                                        "$.username",
                                        matching("^\\s*$")
                                )
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(400)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody("""
                                          {
                                            "code": 1002,
                                            "message": "用户名不能为空",
                                            "data": null
                                          }
                                          """)
                        )
        );
        server.stubFor(
                post(urlEqualTo("/login"))
                        .atPriority(1)
                        .withRequestBody(
                                matchingJsonPath(
                                        "$.password",
                                        matching("^\\s*$")
                                )
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(400)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody("""
                                          {
                                            "code": 1003,
                                            "message": "密码不能为空",
                                            "data": null
                                          }
                                          """)
                        )
        );
        server.stubFor(
                post(urlEqualTo("/login"))
                        .atPriority(2)
                        .withRequestBody(
                                matchingJsonPath(
                                        "$.username",
                                        equalTo("admin")
                                )
                        )
                        .withRequestBody(
                                matchingJsonPath(
                                        "$.password",
                                        equalTo("123456")
                                )
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody("""
                                          {
                                            "code": 0,
                                            "message": "success",
                                            "data": {
                                              "username": "admin",
                                              "token": "test-token-123456"
                                            }
                                          }
                                          """)
                        )
        );
        server.stubFor(
                post(urlEqualTo("/login"))
                        .atPriority(10)
                        .willReturn(
                                aResponse()
                                        .withStatus(401)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody("""
                                          {
                                            "code": 1001,
                                            "message": "用户名或密码错误",
                                            "data": null
                                          }
                                          """)
                        )
        );







        server.stubFor(
                get(urlEqualTo("/users/1"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody("""
                                                {
                                                "code": 0,
                                                "message": "success",
                                                "data": {
                                                "id": 1,
                                                "username": "admin",
                                                "role": "tester"
                                                }
                                                }
                                                """)
                        )
        );
        server.stubFor(
                get(urlEqualTo("/users/1"))
                        .atPriority(10)
                        .willReturn(
                                aResponse()
                                        .withStatus(401)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody("""
                                                {
                                                "code": 401,
                                                "message": "未授权访问"
                                                }
                                                """)
                        )
        );


        System.out.println("Mock登录服务启动成功");
    }

    public static void stop() {
        if (server != null) {
            server.stop();
            System.out.println("Mock登录服务已停止");
        }
    }
}
