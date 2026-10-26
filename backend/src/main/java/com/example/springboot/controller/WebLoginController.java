package com.example.springboot.controller;

import com.example.springboot.entity.WebUsers;
import com.example.springboot.service.LogManageService;
import com.example.springboot.service.WebLoginService;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebLoginController {

  @Autowired
  private WebLoginService webLoginService;

  @Autowired
  private LogManageService logManageService;

  /**
   * 网页端登录。
   *
   * <p>始终返回 HTTP 200，统一 JSON 结构：
   * 成功 {@code {"success": true, "user": {...}}}；
   * 失败 {@code {"success": false, "message": "账号或密码错误"}}。
   * 不再出现“登录失败却返回 HTTP 200 空 body”的歧义。
   */
  @RequestMapping(value = "/web_login", method = RequestMethod.POST)
  public Map<String, Object> login(@RequestBody Map<String, Object> map,
      HttpServletRequest request, HttpSession session) {
    Map<String, Object> body = new HashMap<>();
    String account = String.valueOf(map.get("account"));
    String password = String.valueOf(map.get("password"));
    char type = String.valueOf(map.get("type")).charAt(0);
    WebUsers webUser = webLoginService.login(account, password, type);
    if (webUser != null) {
      logManageService.recordOperation(account, "登录系统", "web_login", "true");
      body.put("success", true);
      body.put("user", webUser);
      return body;
    }
    body.put("success", false);
    body.put("message", "账号或密码错误");
    return body;
  }

  // 忘记密码
  @RequestMapping(value = "/forget_password", method = RequestMethod.POST)
  public String forgotPassword(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = map.get("account").toString();
    String phone = map.get("phoneNumber").toString();
    return webLoginService.forgotPassword(account, phone);
  }

  @RequestMapping(value = "/change_password", method = RequestMethod.POST)
  public boolean changePassword(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = map.get("account").toString();
    String newPassword = map.get("newPassword").toString();
    logManageService.recordOperation(account, "修改密码", "change_password",
        "true");
    return webLoginService.changePassword(account, newPassword);
  }

}
