package mock;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public final class LoginMockServer {

    private static final int PORT = 8089;

    private static WireMockServer server;

    private static final Logger log =
            LoggerFactory.getLogger(LoginMockServer.class);

    private LoginMockServer() {
    }

    public static void start() {

        if(server != null && server.isRunning()){
            log.warn("WireMock已经启动，跳过重复启动");
            return;
        }

        server = new WireMockServer(PORT);
        try{
            server.start();
        }catch (RuntimeException exception){
            server = null;

            log.error(
                    "WireMock启动失败，端口：{}",
                    PORT,
                    exception
            );
            throw exception;
        }



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
                post(urlEqualTo("/users"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withRequestBody(
                                matchingJsonPath("$.username", equalTo("new-user"))
                        )
                        .withRequestBody(
                                matchingJsonPath("$.role", equalTo("tester"))
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(201)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody("""
                                                {
                                                  "code": 0,
                                                  "message": "创建成功",
                                                  "data": {
                                                    "id": 4,
                                                    "username": "new-user",
                                                    "role": "tester"
                                                  }
                                                }
                                                """)
                        )
        );

        server.stubFor(
                post(urlEqualTo("/users"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withRequestBody(
                                matchingJsonPath("$.username", matching("^\\s*$"))
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
                                                  "code": 400,
                                                  "message": "用户名不能为空"
                                                }
                                                """)
                        )
        );

        server.stubFor(
                post(urlEqualTo("/users"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withRequestBody(
                                matchingJsonPath("$.username", equalTo("admin"))
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(409)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody("""
                                                {
                                                  "code": 409,
                                                  "message": "用户名已存在"
                                                }
                                                """)
                        )
        );

        server.stubFor(
                post(urlEqualTo("/users"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withRequestBody(
                                matchingJsonPath(
                                        "$.role",
                                        matching("^(?!(?:tester|developer)$).+$")
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
                                                  "code": 400,
                                                  "message": "不支持的用户角色"
                                                }
                                                """)
                        )
        );

        server.stubFor(
                post(urlEqualTo("/users"))
                        .atPriority(20)
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

        server.stubFor(
                get(urlPathEqualTo("/users"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withQueryParam("page", equalTo("1"))
                        .withQueryParam("pageSize", equalTo("2"))
                        .withQueryParam(
                                "role",
                                matching("^(?!(?:tester|developer)$).+$")
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
                                                  "code": 400,
                                                  "message": "不支持的角色筛选条件"
                                                }
                                                """)
                        )
        );

        server.stubFor(
                get(urlPathEqualTo("/users"))
                        .atPriority(2)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withQueryParam("page", equalTo("1"))
                        .withQueryParam("pageSize", equalTo("2"))
                        .withQueryParam("role", equalTo("developer"))
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
                                                    "page": 1,
                                                    "pageSize": 2,
                                                    "total": 1,
                                                    "items": [
                                                      {
                                                        "id": 3,
                                                        "username": "developer",
                                                        "role": "developer"
                                                      }
                                                    ]
                                                  }
                                                }
                                                """)
                        )
        );

        server.stubFor(
                get(urlPathEqualTo("/users"))
                        .atPriority(2)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withQueryParam("page", equalTo("1"))
                        .withQueryParam("pageSize", equalTo("2"))
                        .withQueryParam("sortBy", equalTo("username"))
                        .withQueryParam("sortOrder", equalTo("asc"))
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
                                                    "page": 1,
                                                    "pageSize": 2,
                                                    "total": 3,
                                                    "items": [
                                                      {
                                                        "id": 1,
                                                        "username": "admin",
                                                        "role": "tester"
                                                      },
                                                      {
                                                        "id": 2,
                                                        "username": "tester",
                                                        "role": "tester"
                                                      }
                                                    ]
                                                  }
                                                }
                                                """)
                        )
        );

        server.stubFor(
                get(urlPathEqualTo("/users"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withQueryParam("page", equalTo("1"))
                        .withQueryParam("pageSize", equalTo("2"))
                        .withQueryParam("role", equalTo("tester"))
                        .withQueryParam("sortBy", equalTo("username"))
                        .withQueryParam("sortOrder", equalTo("desc"))
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
                                                    "page": 1,
                                                    "pageSize": 2,
                                                    "total": 2,
                                                    "items": [
                                                      {
                                                        "id": 2,
                                                        "username": "tester",
                                                        "role": "tester"
                                                      },
                                                      {
                                                        "id": 1,
                                                        "username": "admin",
                                                        "role": "tester"
                                                      }
                                                    ]
                                                  }
                                                }
                                                """)
                        )
        );

        server.stubFor(
                get(urlPathEqualTo("/users"))
                        .atPriority(2)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withQueryParam("page", equalTo("1"))
                        .withQueryParam("pageSize", equalTo("2"))
                        .withQueryParam("sortBy", equalTo("username"))
                        .withQueryParam("sortOrder", equalTo("desc"))
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
                                                    "page": 1,
                                                    "pageSize": 2,
                                                    "total": 3,
                                                    "items": [
                                                      {
                                                        "id": 2,
                                                        "username": "tester",
                                                        "role": "tester"
                                                      },
                                                      {
                                                        "id": 1,
                                                        "username": "admin",
                                                        "role": "tester"
                                                      }
                                                    ]
                                                  }
                                                }
                                                """)
                        )
        );

        server.stubFor(
                get(urlPathEqualTo("/users"))
                        .atPriority(2)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withQueryParam("page", equalTo("1"))
                        .withQueryParam("pageSize", equalTo("2"))
                        .withQueryParam("role", equalTo("tester"))
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
                                                    "page": 1,
                                                    "pageSize": 2,
                                                    "total": 2,
                                                    "items": [
                                                      {
                                                        "id": 1,
                                                        "username": "admin",
                                                        "role": "tester"
                                                      },
                                                      {
                                                        "id": 2,
                                                        "username": "tester",
                                                        "role": "tester"
                                                      }
                                                    ]
                                                  }
                                                }
                                                """)
                        )
        );

        server.stubFor(
                get(urlPathEqualTo("/users"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withQueryParam("page", equalTo("3"))
                        .withQueryParam("pageSize", equalTo("2"))
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
                                                    "page": 3,
                                                    "pageSize": 2,
                                                    "total": 3,
                                                    "items": []
                                                  }
                                                }
                                                """)
                        )
        );

        server.stubFor(
                get(urlPathEqualTo("/users"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withQueryParam("page", equalTo("2"))
                        .withQueryParam("pageSize", equalTo("2"))
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
                                                    "page": 2,
                                                    "pageSize": 2,
                                                    "total": 3,
                                                    "items": [
                                                      {
                                                        "id": 3,
                                                        "username": "developer",
                                                        "role": "developer"
                                                      }
                                                    ]
                                                  }
                                                }
                                                """)
                        )
        );

        server.stubFor(
                get(urlPathEqualTo("/users"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withQueryParam("page", equalTo("1"))
                        .withQueryParam("pageSize", equalTo("1"))
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
                                                    "page": 1,
                                                    "pageSize": 1,
                                                    "total": 3,
                                                    "items": [
                                                      {
                                                        "id": 1,
                                                        "username": "admin",
                                                        "role": "tester"
                                                      }
                                                    ]
                                                  }
                                                }
                                                """)
                        )
        );

        server.stubFor(
                get(urlPathEqualTo("/users"))
                        .atPriority(10)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withQueryParam("page", equalTo("1"))
                        .withQueryParam("pageSize", equalTo("2"))
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
                                                    "page": 1,
                                                    "pageSize": 2,
                                                    "total": 3,
                                                    "items": [
                                                      {
                                                        "id": 1,
                                                        "username": "admin",
                                                        "role": "tester"
                                                      },
                                                      {
                                                        "id": 2,
                                                        "username": "tester",
                                                        "role": "tester"
                                                      }
                                                    ]
                                                  }
                                                }
                                                """)
                        )
        );


        server.stubFor(
                get(urlPathEqualTo("/users"))
                        .atPriority(20)
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

        server.stubFor(
                delete(urlEqualTo("/users/1"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .willReturn(aResponse().withStatus(204))
        );

        server.stubFor(
                delete(urlEqualTo("/users/999"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(404)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody("""
                                                {
                                                  "code": 404,
                                                  "message": "用户不存在"
                                                }
                                                """)
                        )
        );

        server.stubFor(
                delete(urlPathMatching("/users/\\d+"))
                        .atPriority(20)
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

        server.stubFor(
                put(urlEqualTo("/users/1"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withRequestBody(
                                matchingJsonPath(
                                        "$.username",
                                        equalTo("updated-admin")
                                )
                        )
                        .withRequestBody(
                                matchingJsonPath("$.role", equalTo("tester"))
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
                                                  "message": "更新成功",
                                                  "data": {
                                                    "id": 1,
                                                    "username": "updated-admin",
                                                    "role": "tester"
                                                  }
                                                }
                                                """)
                        )
        );

        server.stubFor(
                put(urlEqualTo("/users/999"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(404)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody("""
                                                {
                                                  "code": 404,
                                                  "message": "用户不存在"
                                                }
                                                """)
                        )
        );

        server.stubFor(
                put(urlPathMatching("/users/\\d+"))
                        .atPriority(20)
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

        server.stubFor(
                get(urlEqualTo("/users/999"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(404)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody("""
                                                {
                                                  "code": 404,
                                                  "message": "用户不存在"
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


        log.info(
                "WireMock启动成功，端口：{}",
                PORT
        );
    }

    public static void stop() {
        if (server == null) {
            log.debug("WireMock未创建，无需停止");
            return;
        }
        if(!server.isRunning()){
            log.debug("WireMock未运行，无需停止");
            server = null;
            return;
        }
        try{
            server.stop();
            log.info("WireMock已停止");
        }finally {
            server = null;
        }
    }
}
