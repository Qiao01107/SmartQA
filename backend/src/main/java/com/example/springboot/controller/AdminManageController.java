package com.example.springboot.controller;

import com.example.springboot.entity.WebUsers;
import com.example.springboot.security.AuthContext;
import com.example.springboot.security.TokenRevocationService;
import com.example.springboot.service.AdminManageService;
import com.example.springboot.service.LogManageService;
import com.example.springboot.tools.Payload;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员账号管理接口。
 *
 * <p>授权模型（接口层由 {@code SecurityConfig} 强制）：
 * <ul>
 *   <li>{@code /add_web_user}、{@code /remove_web_user}、{@code /change_web_user}、
 *       {@code /screen_web_user}、{@code /get_web_user}、{@code /count_web_user}
 *       —— 仅 ROLE_SUPER_ADMIN；普通管理员直接发 HTTP 请求也会得到 403。</li>
 *   <li>{@code /change_self_info} —— 任何已登录管理员，但只能改自己的资料：
 *       账号取自令牌，请求体里的 account 只做一致性校验。</li>
 * </ul>
 *
 * <p>操作日志里的操作人同样取自令牌，不再使用请求体中的 {@code operatorAccount}，
 * 避免“伪造操作人”污染审计日志。
 */
@RestController
public class AdminManageController {

  private final AdminManageService adminManageService;
  private final LogManageService logManageService;
  private final TokenRevocationService revocationService;

  public AdminManageController(AdminManageService adminManageService,
      LogManageService logManageService, TokenRevocationService revocationService) {
    this.adminManageService = adminManageService;
    this.logManageService = logManageService;
    this.revocationService = revocationService;
  }

  // 管理员表管理（仅超级管理员，接口层已限制）
  @PostMapping("/add_web_user")
  public boolean addWebUser(@RequestBody Map<String, Object> map) {
    String operator = AuthContext.identity();
    String name = Payload.text(map, "name");
    String account = Payload.text(map, "account");
    String phone = Payload.text(map, "phone");
    String adminType = Payload.text(map, "adminType");
    String password = Payload.text(map, "password");
    boolean status = adminManageService.addWebUser(name, account, phone, adminType, password);
    logManageService.recordOperation(operator, "添加管理员" + account,
        "add_web_user", String.valueOf(status));
    return status;
  }

  @PostMapping("/remove_web_user")
  public boolean removeWebUser(@RequestBody Map<String, Object> map) {
    String operator = AuthContext.identity();
    String account = Payload.text(map, "account");
    boolean status = adminManageService.removeWebUser(account, operator);
    logManageService.recordOperation(operator, "删除管理员" + account,
        "remove_web_user", String.valueOf(status));
    return status;
  }

  @PostMapping("/change_web_user")
  public boolean changeWebUser(@RequestBody Map<String, Object> map) {
    String operator = AuthContext.identity();
    String account = Payload.text(map, "account");
    String phone = Payload.text(map, "phone");
    String adminType = Payload.text(map, "adminType");
    boolean status = adminManageService.changeWebUser(account, phone, adminType);
    logManageService.recordOperation(operator, "修改管理员" + account,
        "change_web_user", String.valueOf(status));
    return status;
  }

  @PostMapping("/screen_web_user")
  public List<WebUsers> screenWebUser(@RequestBody Map<String, Object> map) {
    return adminManageService.screenWebUser(Payload.text(map, "adminType"));
  }

  @PostMapping("/count_web_user")
  public int countWebUser() {
    return adminManageService.countWebUser();
  }

  @PostMapping("/get_web_user")
  public List<WebUsers> getWebUser(@RequestBody Map<String, Object> map) {
    int start = Payload.intValue(map, "start", 1);
    int number = Payload.intValue(map, "number", 10);
    return adminManageService.getWebUser(start, number);
  }

  /**
   * 修改个人信息。账号来自令牌；若提交了新密码，改完即吊销该账号所有旧令牌，
   * 前端应提示重新登录。
   */
  @PostMapping("/change_self_info")
  public Map<String, Object> changeSelfInfo(@RequestBody Map<String, Object> map) {
    String account = AuthContext.requireSelf(map.get("account"));
    String phone = Payload.text(map, "phone");
    String email = Payload.text(map, "email");
    String password = Payload.text(map, "password");

    boolean status = adminManageService.changeSelfInfo(phone, email, password, account);
    boolean passwordChanged = status && !password.isEmpty();
    if (passwordChanged) {
      revocationService.revokeAllIssuedBefore(account);
    }
    logManageService.recordOperation(account, "修改个人信息", "change_self_info",
        String.valueOf(status));

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("success", status);
    body.put("passwordChanged", passwordChanged);
    body.put("message", status
        ? (passwordChanged ? "信息已保存，密码已更新，请重新登录" : "信息已保存")
        : "保存失败：密码需至少 8 位且同时包含字母和数字");
    return body;
  }
}
