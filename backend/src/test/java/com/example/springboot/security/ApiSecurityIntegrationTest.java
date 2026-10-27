package com.example.springboot.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * 后端鉴权集成测试（Phase Security-1）。
 *
 * <p>这是<b>真实 HTTP 端到端测试</b>：直接请求已启动的后端（默认 127.0.0.1:8081），
 * 断言 401 / 403 / 200 的真实状态码，而不是只做 Spring 上下文加载。
 *
 * <p>运行前置条件（缺任一项时相关用例会 skip，并在输出中说明原因，不会伪造通过）：
 * <ol>
 *   <li>后端已启动，且 MySQL 可用；</li>
 *   <li>环境变量提供测试账号：</li>
 * </ol>
 * <pre>
 * SMARTQA_TEST_SUPER_ACCOUNT / SMARTQA_TEST_SUPER_PASSWORD   （超级管理员）
 * SMARTQA_TEST_ADMIN_ACCOUNT / SMARTQA_TEST_ADMIN_PASSWORD   （普通管理员，缺省则跳过越权用例）
 * SMARTQA_TEST_WECHAT_PHONE  / SMARTQA_TEST_WECHAT_PASSWORD   （小程序用户，缺省则跳过跨用户用例）
 * </pre>
 *
 * <p>凭证全部来自环境变量，仓库内不出现任何真实密码。
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiSecurityIntegrationTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final HttpClient CLIENT = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(5))
      .build();

  private static String baseUrl;

  private static boolean serverUp;

  private static String superToken;

  @BeforeAll
  static void detectServer() {
    baseUrl = env("SMARTQA_BASE_URL", "http://127.0.0.1:8081");
    serverUp = probe();
  }

  private static boolean probe() {
    try {
      HttpResponse<String> response = send("POST", "/web_login", "{}", null);
      return response.statusCode() > 0;
    } catch (Exception e) {
      return false;
    }
  }

  @Test
  @Order(1)
  @DisplayName("未登录访问管理员列表 → 401")
  void unauthenticatedAdminListReturns401() throws Exception {
    requireServer();
    HttpResponse<String> response =
        send("POST", "/get_web_user", "{\"start\":1,\"number\":10}", null);
    assertEquals(401, response.statusCode());
    assertEquals("UNAUTHORIZED", json(response).path("code").asText());
  }

  @Test
  @Order(2)
  @DisplayName("未登录尝试创建管理员 → 401")
  void unauthenticatedCreateAdminReturns401() throws Exception {
    requireServer();
    HttpResponse<String> response = send("POST", "/add_web_user",
        "{\"name\":\"x\",\"account\":\"x\",\"phone\":\"1\",\"adminType\":\"1\","
            + "\"password\":\"Abcd1234\"}", null);
    assertEquals(401, response.statusCode());
  }

  @Test
  @Order(3)
  @DisplayName("错误密码登录 → 401（不是 200 空响应）")
  void wrongPasswordReturns401() throws Exception {
    requireServer();
    String account = env("SMARTQA_TEST_SUPER_ACCOUNT", "");
    assumeTrue(!account.isEmpty(), "未提供 SMARTQA_TEST_SUPER_ACCOUNT，跳过");
    HttpResponse<String> response = send("POST", "/web_login",
        "{\"account\":\"" + account + "\",\"password\":\"definitely-wrong-pwd\",\"type\":\"1\"}",
        null);
    assertEquals(401, response.statusCode());
    assertEquals("INVALID_CREDENTIALS", json(response).path("code").asText());
  }

  @Test
  @Order(4)
  @DisplayName("BCrypt 登录成功 → 签发真实 JWT，响应不含密码")
  void bcryptLoginIssuesRealToken() throws Exception {
    requireServer();
    String account = env("SMARTQA_TEST_SUPER_ACCOUNT", "");
    String password = env("SMARTQA_TEST_SUPER_PASSWORD", "");
    assumeTrue(!account.isEmpty() && !password.isEmpty(),
        "未提供 SMARTQA_TEST_SUPER_ACCOUNT/PASSWORD，跳过");

    HttpResponse<String> response = send("POST", "/web_login",
        "{\"account\":\"" + account + "\",\"password\":\"" + password + "\",\"type\":\"1\"}",
        null);
    assertEquals(200, response.statusCode());
    JsonNode body = json(response);
    superToken = body.path("token").asText();
    assertEquals(3, superToken.split("\\.").length, "应为三段式 JWT");
    assertTrue(body.path("success").asBoolean());
    assertTrue(body.path("user").path("password").isMissingNode(), "登录响应不得包含密码字段");
  }

  @Test
  @Order(5)
  @DisplayName("超级管理员访问管理员列表 → 200")
  void superAdminCanListAdmins() throws Exception {
    requireServer();
    assumeTrue(superToken != null && !superToken.isEmpty(), "缺少超级管理员令牌，跳过");
    HttpResponse<String> response =
        send("POST", "/get_web_user", "{\"start\":1,\"number\":10}", superToken);
    assertEquals(200, response.statusCode());
    assertTrue(json(response).isArray());
  }

  @Test
  @Order(6)
  @DisplayName("普通管理员调用超级管理员接口 → 403")
  void normalAdminCannotUseSuperAdminApi() throws Exception {
    requireServer();
    String account = env("SMARTQA_TEST_ADMIN_ACCOUNT", "");
    String password = env("SMARTQA_TEST_ADMIN_PASSWORD", "");
    assumeTrue(!account.isEmpty() && !password.isEmpty(),
        "未提供 SMARTQA_TEST_ADMIN_ACCOUNT/PASSWORD，跳过");

    HttpResponse<String> login = send("POST", "/web_login",
        "{\"account\":\"" + account + "\",\"password\":\"" + password + "\",\"type\":\"0\"}",
        null);
    assertEquals(200, login.statusCode());
    String token = json(login).path("token").asText();
    assertNotNull(token);

    assertEquals(403, send("POST", "/get_web_user", "{\"start\":1,\"number\":10}", token)
        .statusCode());
    assertEquals(403, send("POST", "/add_web_user",
        "{\"name\":\"x\",\"account\":\"hack_x\",\"phone\":\"1\",\"adminType\":\"1\","
            + "\"password\":\"Abcd1234\"}", token).statusCode());
    // 授权范围内的接口仍然可用
    assertEquals(200, send("POST", "/count_wechat_user", "{}", token).statusCode());
  }

  @Test
  @Order(7)
  @DisplayName("小程序用户访问管理员接口 → 403")
  void wechatUserCannotUseAdminApi() throws Exception {
    String token = wechatToken();
    assumeTrue(token != null, "未提供 SMARTQA_TEST_WECHAT_PHONE/PASSWORD，跳过");
    assertEquals(403, send("POST", "/get_web_user", "{\"start\":1,\"number\":10}", token)
        .statusCode());
    assertEquals(403, send("POST", "/count_web_user", "{}", token).statusCode());
  }

  @Test
  @Order(8)
  @DisplayName("小程序用户读取他人私有数据 → 403")
  void wechatUserCannotReadOtherUserData() throws Exception {
    String token = wechatToken();
    assumeTrue(token != null, "未提供 SMARTQA_TEST_WECHAT_PHONE/PASSWORD，跳过");
    String otherPhone = "13000000000";
    assertEquals(403, send("POST", "/wechat_chatting", "{\"phone\":\"" + otherPhone + "\"}", token)
        .statusCode());
    assertEquals(403, send("POST", "/collectible", "{\"phone\":\"" + otherPhone + "\"}", token)
        .statusCode());
    assertEquals(403, send("POST", "/wechat_feedbackView", "{\"phone\":\"" + otherPhone + "\"}", token)
        .statusCode());
    assertEquals(403, send("POST", "/wechat_information", "{\"phone\":\"" + otherPhone + "\"}", token)
        .statusCode());
  }

  @Test
  @Order(9)
  @DisplayName("篡改 / 伪造的令牌 → 401")
  void tamperedTokenReturns401() throws Exception {
    requireServer();
    assertEquals(401, send("POST", "/count_web_user", "{}", "not-a-jwt").statusCode());
    assumeTrue(superToken != null && superToken.length() > 8, "缺少超级管理员令牌，跳过部分用例");
    String tampered = superToken.substring(0, superToken.length() - 4) + "AAAA";
    assertEquals(401, send("POST", "/count_web_user", "{}", tampered).statusCode());
  }

  private static String wechatToken() throws Exception {
    String phone = env("SMARTQA_TEST_WECHAT_PHONE", "");
    String password = env("SMARTQA_TEST_WECHAT_PASSWORD", "");
    if (phone.isEmpty() || password.isEmpty() || !serverUp) {
      return null;
    }
    HttpResponse<String> login = send("POST", "/wechat_login",
        "{\"phone\":\"" + phone + "\",\"password\":\"" + password + "\"}", null);
    if (login.statusCode() != 200) {
      return null;
    }
    String token = json(login).path("token").asText();
    return token.isEmpty() ? null : token;
  }

  private static void requireServer() {
    assumeTrue(serverUp, "后端未运行（" + baseUrl + "），跳过端到端鉴权测试");
  }

  private static HttpResponse<String> send(String method, String path, String body, String token)
      throws Exception {
    HttpRequest.Builder builder = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + path))
        .timeout(Duration.ofSeconds(10))
        .header("Content-Type", "application/json")
        .header("Accept", "application/json");
    if (token != null) {
      builder.header("Authorization", "Bearer " + token);
    }
    builder.method(method, body == null
        ? HttpRequest.BodyPublishers.noBody()
        : HttpRequest.BodyPublishers.ofString(body));
    return CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
  }

  private static JsonNode json(HttpResponse<String> response) throws Exception {
    String body = response.body();
    if (body == null || body.isBlank()) {
      return MAPPER.createObjectNode();
    }
    return MAPPER.readTree(body);
  }

  private static String env(String key, String defaultValue) {
    String value = System.getenv(key);
    if (value == null || value.isBlank()) {
      value = System.getProperty(key);
    }
    return (value == null || value.isBlank()) ? defaultValue : value.trim();
  }
}
