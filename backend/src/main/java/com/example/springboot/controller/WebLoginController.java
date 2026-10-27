package com.example.springboot.controller;

import com.example.springboot.entity.WebUsers;
import com.example.springboot.security.AuthContext;
import com.example.springboot.security.AuthPrincipal;
import com.example.springboot.security.JwtService;
import com.example.springboot.security.PasswordPolicy;
import com.example.springboot.security.Roles;
import com.example.springboot.security.TokenRevocationService;
import com.example.springboot.service.LogManageService;
import com.example.springboot.service.PasswordResetService;
import com.example.springboot.service.WebLoginService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端登录 / 退出 / 找回密码。
 *
 * <p>认证语义：
 * <ul>
 *   <li>登录成功：HTTP 200，返回真实签名的 JWT（{@code token}）与用户信息（不含密码/哈希）；</li>
 *   <li>账号或密码错误：HTTP 401，返回明确 JSON 提示，不再返回 200 空响应；</li>
 *   <li>退出登录：HTTP 200，并吊销当前令牌的 jti（服务端立即失效，而非只删前端存储）；</li>
 *   <li>修改密码：仅能改自己的密码，成功后吊销该账号此前签发的所有令牌。</li>
 * </ul>
 */
@RestController
public class WebLoginController {

  private static final Logger log = LoggerFactory.getLogger(WebLoginController.class);

  private final WebLoginService webLoginService;
  private final PasswordResetService passwordResetService;
  private final LogManageService logManageService;
  private final JwtService jwtService;
  private final TokenRevocationService revocationService;

  public WebLoginController(WebLoginService webLoginService,
      PasswordResetService passwordResetService,
      LogManageService logManageService,
      JwtService jwtService,
      TokenRevocationService revocationService) {
    this.webLoginService = webLoginService;
    this.passwordResetService = passwordResetService;
    this.logManageService = logManageService;
    this.jwtService = jwtService;
    this.revocationService = revocationService;
  }

  /**
   * 网页端登录，成功后签发 Bearer 令牌。
   */
  @PostMapping("/web_login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> map) {
    String account = text(map.get("account"));
    String password = text(map.get("password"));
    String typeText = text(map.get("type"));

    if (account.isEmpty() || password.isEmpty() || typeText.isEmpty()) {
      return ResponseEntity.badRequest()
          .body(failure("账号、密码、身份类型都不能为空", "INVALID_ARGUMENT"));
    }
    char type = typeText.charAt(0);

    WebUsers webUser = webLoginService.login(account, password, type);
    if (webUser == null) {
      // 不区分“账号不存在”“身份类型不符”“密码错误”，避免账号枚举。
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(failure("账号或密码错误", "INVALID_CREDENTIALS"));
    }

    String role = Roles.fromAdminType(webUser.getAdminType());
    if (role == null) {
      log.error("[Login] 账号 {} 的 adminType 非法（{}），拒绝签发令牌", account,
          (int) webUser.getAdminType());
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(failure("账号角色配置异常，请联系超级管理员", "INVALID_ROLE"));
    }

    AuthPrincipal principal =
        jwtService.newPrincipal(webUser.getAccount(), role, AuthPrincipal.TYPE_WEB);
    String token = jwtService.issue(principal);

    logManageService.recordOperation(account, "登录系统", "web_login", "true");

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("success", true);
    body.put("code", "OK");
    body.put("message", "登录成功");
    body.put("token", token);
    body.put("tokenType", "Bearer");
    body.put("expiresIn", jwtService.getTtlSeconds());
    body.put("role", role);
    body.put("user", webUser);
    return ResponseEntity.ok(body);
  }

  /**
   * 退出登录：吊销当前令牌。前端同时清除本地存储。
   */
  @PostMapping("/web_logout")
  public Map<String, Object> logout() {
    AuthPrincipal principal = AuthContext.principal();
    revocationService.revoke(principal.jti(), principal.expiresAt());
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("success", true);
    body.put("message", "已退出登录");
    return body;
  }

  /**
   * 申请找回密码验证码（匿名）。验证码只在服务端保存，响应中绝不返回。
   */
  @PostMapping("/forget_password")
  public Map<String, Object> forgotPassword(@RequestBody Map<String, Object> map) {
    return passwordResetService.requestCode(text(map.get("account")),
        text(map.get("phoneNumber")));
  }

  /**
   * 用短信验证码重置密码（匿名，但必须持有服务端签发的验证码）。
   */
  @PostMapping("/reset_password")
  public Map<String, Object> resetPassword(@RequestBody Map<String, Object> map) {
    return passwordResetService.resetPassword(text(map.get("account")),
        text(map.get("phoneNumber")), text(map.get("code")), text(map.get("newPassword")));
  }

  /**
   * 已登录用户修改自己的密码。
   *
   * <p>账号一律取自令牌，请求体里的 account 仅用于一致性校验（不一致直接 403），
   * 因此不存在“改密码接口可改别人密码”的越权路径。
   */
  @PostMapping("/change_password")
  public Map<String, Object> changePassword(@RequestBody Map<String, Object> map) {
    String account = AuthContext.requireSelf(text(map.get("account")));
    String newPassword = text(map.get("newPassword"));

    String policyError = PasswordPolicy.validateAdminPassword(newPassword);
    if (policyError != null) {
      return Map.of("success", false, "message", policyError);
    }
    boolean changed = webLoginService.changePassword(account, newPassword);
    if (changed) {
      // 改密后立即吊销此前所有令牌，旧凭证不再可用。
      revocationService.revokeAllIssuedBefore(account);
      logManageService.recordOperation(account, "修改密码", "change_password", "true");
    }
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("success", changed);
    body.put("message", changed ? "密码修改成功，请重新登录" : "密码修改失败");
    return body;
  }

  private static Map<String, Object> failure(String message, String code) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("success", false);
    body.put("code", code);
    body.put("message", message);
    return body;
  }

  private static String text(Object value) {
    return value == null ? "" : String.valueOf(value).trim();
  }
}
