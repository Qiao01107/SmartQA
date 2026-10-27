package com.example.springboot.controller;

import com.example.springboot.entity.WeChatUsers;
import com.example.springboot.security.AuthContext;
import com.example.springboot.security.AuthPrincipal;
import com.example.springboot.security.JwtService;
import com.example.springboot.security.Roles;
import com.example.springboot.security.TokenRevocationService;
import com.example.springboot.service.WeChatLoginService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信小程序登录 / 注册 / 改密 / 退出。
 *
 * <p>响应结构已从“裸 int / 裸 boolean”改为带 {@code success/code/message} 的 JSON，
 * 登录成功后额外返回 JWT。小程序的 {@code utils/request.js} 会把它存进本地存储，
 * 并在后续每个请求中带上 {@code Authorization: Bearer <token>}。
 *
 * <ul>
 *   <li>手机号或密码错误 → HTTP 401，{@code code=INVALID_CREDENTIALS}；</li>
 *   <li>账号被禁用 → HTTP 403，{@code code=USER_DISABLED}；</li>
 *   <li>登录成功 → HTTP 200，返回 token 与用户信息（不含密码哈希）。</li>
 * </ul>
 */
@RestController
public class WeChatLoginController {

  private final WeChatLoginService weChatLoginService;
  private final JwtService jwtService;
  private final TokenRevocationService revocationService;

  public WeChatLoginController(WeChatLoginService weChatLoginService,
      JwtService jwtService, TokenRevocationService revocationService) {
    this.weChatLoginService = weChatLoginService;
    this.jwtService = jwtService;
    this.revocationService = revocationService;
  }

  /** 登录：校验手机号 + BCrypt 密码，成功后签发小程序用户令牌。 */
  @PostMapping("/wechat_login")
  public ResponseEntity<Map<String, Object>> wechatLogin(@RequestBody Map<String, Object> map) {
    String phone = text(map.get("phone"));
    String password = text(map.get("password"));
    if (phone.isEmpty() || password.isEmpty()) {
      return ResponseEntity.badRequest()
          .body(failure("手机号和密码不能为空", "INVALID_ARGUMENT"));
    }

    int status = weChatLoginService.wechatLogin(phone, password);
    if (status == 1) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(failure("用户已被禁用，请联系管理员", "USER_DISABLED"));
    }
    if (status != 0) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(failure("手机号或密码错误", "INVALID_CREDENTIALS"));
    }

    AuthPrincipal principal =
        jwtService.newPrincipal(phone, Roles.WECHAT_USER, AuthPrincipal.TYPE_WECHAT);
    String token = jwtService.issue(principal);

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("success", true);
    body.put("code", "OK");
    body.put("message", "登录成功");
    body.put("token", token);
    body.put("tokenType", "Bearer");
    body.put("expiresIn", jwtService.getTtlSeconds());
    body.put("role", Roles.WECHAT_USER);
    body.put("user", weChatLoginService.findByPhone(phone));
    return ResponseEntity.ok(body);
  }

  /** 注册（匿名）。 */
  @PostMapping("/wechat_register")
  public Map<String, Object> register(@RequestBody Map<String, Object> map) {
    String wechatName = text(map.get("wechatName"));
    String profilePicture = text(map.get("profile_picture"));
    String password = text(map.get("password"));
    String phone = text(map.get("phone"));

    if (phone.isEmpty() || password.isEmpty() || wechatName.isEmpty()) {
      return failure("用户名、手机号和密码都不能为空", "INVALID_ARGUMENT");
    }
    if (password.length() < 6) {
      return failure("密码长度至少 6 位", "WEAK_PASSWORD");
    }
    boolean ok = weChatLoginService.register(wechatName, phone, profilePicture, password);
    return ok ? success("注册成功") : failure("手机号已存在", "PHONE_EXISTS");
  }

  /**
   * 修改自己的密码：手机号取自令牌，请求体里的 phone 只做一致性校验。
   */
  @PostMapping("/wechat_change_password")
  public Map<String, Object> wechatChangePassword(@RequestBody Map<String, Object> map) {
    String phone = AuthContext.requireSelf(text(map.get("phone")));
    String oldPassword = text(map.get("oldPassword"));
    String newPassword = text(map.get("newPassword"));

    if (newPassword.length() < 6) {
      return failure("新密码长度至少 6 位", "WEAK_PASSWORD");
    }
    boolean changed = weChatLoginService.wechatChangePassword(phone, oldPassword, newPassword);
    if (changed) {
      revocationService.revokeAllIssuedBefore(phone);
    }
    return changed ? success("修改密码成功，请重新登录") : failure("旧密码错误", "INVALID_CREDENTIALS");
  }

  /** 退出登录：吊销当前令牌。 */
  @PostMapping("/wechat_logout")
  public Map<String, Object> wechatLogout() {
    AuthPrincipal principal = AuthContext.principal();
    revocationService.revoke(principal.jti(), principal.expiresAt());
    return success("已退出登录");
  }

  private static Map<String, Object> success(String message) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("success", true);
    body.put("message", message);
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
