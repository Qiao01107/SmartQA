package com.example.springboot.controller;

import com.example.springboot.service.WeChatLoginService;
import com.example.springboot.service.WebLoginService;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeChatLoginController {

  @Autowired
  private WeChatLoginService weChatLoginService;

  // 登录
  @RequestMapping(value = "/wechat_login", method = RequestMethod.POST)
  public int wechatLogin(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String phone = map.get("phone").toString();
    String password = map.get("password").toString();
    return weChatLoginService.wechatLogin(phone, password);
  }

  // 注册
  @RequestMapping(value = "/wechat_register", method = RequestMethod.POST)
  public boolean register(@RequestBody Map map, HttpServletRequest request, HttpSession session) {
    String wechatName = map.get("wechatName").toString();
    String profilePicture = map.get("profile_picture").toString();
    String password = map.get("password").toString();
    String phone = map.get("phone").toString();
    return weChatLoginService.register(wechatName, phone, profilePicture, password);
  }

  // 修改密码
  @RequestMapping(value = "/wechat_change_password", method = RequestMethod.POST)
  public boolean wechatChangePassword(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String phone = map.get("phone").toString();
    String oldPassword = map.get("oldPassword").toString();
    String newPassword = map.get("newPassword").toString();
    return weChatLoginService.wechatChangePassword(phone, oldPassword, newPassword);
  }

}
