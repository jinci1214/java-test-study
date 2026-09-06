package mock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public final class LoginMockServer {

    private static final int PORT = 8089;

    private static final String USER_LIFECYCLE_SCENARIO = "用户生命周期";
    private static final String USER_CREATED = "用户已创建";
    private static final String USER_UPDATED = "用户已更新";
    private static final String USER_DELETED = "用户已删除";
    private static final String FILE_LIFECYCLE_SCENARIO = "文件生命周期";
    private static final String FILE_DELETED = "文件已删除";

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
                                equalTo("Bearer tester-token")
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(403)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody("""
                                                {
                                                  "code": 403,
                                                  "message": "权限不足"
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
                get(urlEqualTo("/users/888"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .willReturn(
                                aResponse()
                                        .withFixedDelay(1000)
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
                                                    "id": 888,
                                                    "username": "slow-user",
                                                    "role": "tester"
                                                  }
                                                }
                                                """)
                        )
        );

        server.stubFor(
                post(urlEqualTo("/files"))
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
                post(urlEqualTo("/files"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withMultipartRequestBody(
                                aMultipart()
                                        .withName("file")
                                        .withHeader(
                                                "Content-Type",
                                                containing("application/json")
                                        )
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(415)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody("""
                                                {
                                                  "code": 415,
                                                  "message": "不支持的文件类型"
                                                }
                                                """)
                        )
        );

        server.stubFor(
                post(urlEqualTo("/files"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withMultipartRequestBody(
                                aMultipart()
                                        .withName("file")
                                        .withBody(equalTo(""))
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
                                                  "message": "文件不能为空"
                                                }
                                                """)
                        )
        );

        server.stubFor(
                post(urlEqualTo("/files"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withMultipartRequestBody(
                                aMultipart()
                                        .withName("file")
                                        .withBody(matching("a{1025,}"))
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(413)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody("""
                                                {
                                                  "code": 413,
                                                  "message": "文件大小超过限制"
                                                }
                                                """)
                        )
        );

        server.stubFor(
                post(urlEqualTo("/files"))
                        .atPriority(2)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withMultipartRequestBody(
                                aMultipart()
                                        .withName("file")
                                        .withBody(matching("a{1024}"))
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
                                                  "message": "上传成功",
                                                  "data": {
                                                    "fileId": "file-101",
                                                    "fileName": "max-size-sample.txt",
                                                    "contentType": "text/plain"
                                                  }
                                                }
                                                """)
                        )
        );

        server.stubFor(
                get(urlEqualTo("/files/file-100/download"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .inScenario(FILE_LIFECYCLE_SCENARIO)
                        .whenScenarioStateIs(Scenario.STARTED)
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                "Content-Type",
                                                "text/plain"
                                        )
                                        .withHeader(
                                                "Content-Disposition",
                                                "attachment; filename=upload-sample.txt"
                                        )
                                        .withBody("这是接口自动化上传测试文件。")
                        )
        );

        server.stubFor(
                delete(urlEqualTo("/files/file-100"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .inScenario(FILE_LIFECYCLE_SCENARIO)
                        .whenScenarioStateIs(Scenario.STARTED)
                        .willSetStateTo(FILE_DELETED)
                        .willReturn(aResponse().withStatus(204))
        );

        server.stubFor(
                delete(urlEqualTo("/files/missing-file"))
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
                                                  "message": "文件不存在"
                                                }
                                                """)
                        )
        );

        server.stubFor(
                delete(urlEqualTo("/files/file-100"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .inScenario(FILE_LIFECYCLE_SCENARIO)
                        .whenScenarioStateIs(FILE_DELETED)
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
                                                  "message": "文件不存在"
                                                }
                                                """)
                        )
        );

        server.stubFor(
                get(urlEqualTo("/files/file-100/download"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .inScenario(FILE_LIFECYCLE_SCENARIO)
                        .whenScenarioStateIs(FILE_DELETED)
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
                                                  "message": "文件不存在"
                                                }
                                                """)
                        )
        );

        server.stubFor(
                delete(urlPathMatching("/files/[^/]+"))
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
                get(urlEqualTo("/files/file-200/download"))
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
                                                "application/octet-stream"
                                        )
                                        .withHeader(
                                                "Content-Disposition",
                                                "attachment; filename=binary-sample.bin"
                                        )
                                        .withBody(new byte[]{0, 1, 2, 127, -128, -1})
                        )
        );

        server.stubFor(
                get(urlEqualTo("/files/missing-file/download"))
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
                                                  "message": "文件不存在"
                                                }
                                                """)
                        )
        );

        server.stubFor(
                get(urlEqualTo("/files/file-500/download"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(500)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody("""
                                                {
                                                  "code": 500,
                                                  "message": "文件下载失败"
                                                }
                                                """)
                        )
        );

        server.stubFor(
                get(urlPathMatching("/files/[^/]+/download"))
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
                get(urlEqualTo("/users/1"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer tester-token")
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
                post(urlEqualTo("/files"))
                        .atPriority(2)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withMultipartRequestBody(
                                aMultipart()
                                        .withName("file")
                                        .withBody(
                                                containing("接口自动化上传测试文件")
                                        )
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
                                                  "message": "上传成功",
                                                  "data": {
                                                    "fileId": "file-100",
                                                    "fileName": "upload-sample.txt",
                                                    "contentType": "text/plain"
                                                  }
                                                }
                                                """)
                        )
        );

        server.stubFor(
                get(urlEqualTo("/users/1"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer expired-token")
                        )
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
                                                  "message": "Token已过期"
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

        server.stubFor(
                post(urlEqualTo("/users"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withRequestBody(
                                matchingJsonPath(
                                        "$.username",
                                        equalTo("lifecycle-user")
                                )
                        )
                        .withRequestBody(
                                matchingJsonPath("$.role", equalTo("tester"))
                        )
                        .inScenario(USER_LIFECYCLE_SCENARIO)
                        .whenScenarioStateIs(Scenario.STARTED)
                        .willSetStateTo(USER_CREATED)
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
                                                    "id": 100,
                                                    "username": "lifecycle-user",
                                                    "role": "tester"
                                                  }
                                                }
                                                """)
                        )
        );

        server.stubFor(
                get(urlEqualTo("/users/100"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .inScenario(USER_LIFECYCLE_SCENARIO)
                        .whenScenarioStateIs(USER_CREATED)
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
                                                    "id": 100,
                                                    "username": "lifecycle-user",
                                                    "role": "tester"
                                                  }
                                                }
                                                """)
                        )
        );

        server.stubFor(
                put(urlEqualTo("/users/100"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .withRequestBody(
                                matchingJsonPath(
                                        "$.username",
                                        equalTo("lifecycle-user-updated")
                                )
                        )
                        .withRequestBody(
                                matchingJsonPath("$.role", equalTo("developer"))
                        )
                        .inScenario(USER_LIFECYCLE_SCENARIO)
                        .whenScenarioStateIs(USER_CREATED)
                        .willSetStateTo(USER_UPDATED)
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
                                                    "id": 100,
                                                    "username": "lifecycle-user-updated",
                                                    "role": "developer"
                                                  }
                                                }
                                                """)
                        )
        );

        server.stubFor(
                get(urlEqualTo("/users/100"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .inScenario(USER_LIFECYCLE_SCENARIO)
                        .whenScenarioStateIs(USER_UPDATED)
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
                                                    "id": 100,
                                                    "username": "lifecycle-user-updated",
                                                    "role": "developer"
                                                  }
                                                }
                                                """)
                        )
        );

        server.stubFor(
                delete(urlEqualTo("/users/100"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .inScenario(USER_LIFECYCLE_SCENARIO)
                        .whenScenarioStateIs(USER_UPDATED)
                        .willSetStateTo(USER_DELETED)
                        .willReturn(aResponse().withStatus(204))
        );

        server.stubFor(
                get(urlEqualTo("/users/100"))
                        .atPriority(1)
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer test-token-123456")
                        )
                        .inScenario(USER_LIFECYCLE_SCENARIO)
                        .whenScenarioStateIs(USER_DELETED)
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

    public static void resetScenarios() {
        if (server == null || !server.isRunning()) {
            throw new IllegalStateException("WireMock未启动，无法重置场景");
        }

        server.resetScenarios();
        log.info("WireMock场景状态已重置");
    }
}
