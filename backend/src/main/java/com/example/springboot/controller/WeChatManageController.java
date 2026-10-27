package com.example.springboot.controller;

import com.example.springboot.security.AuthContext;

import com.example.springboot.entity.WeChatUsers;
import com.example.springboot.service.LogManageService;
import com.example.springboot.service.WeChatManageService;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeChatManageController {

  @Autowired
  private WeChatManageService wechatManageService;

  @Autowired
  private LogManageService logManageService;

  // 管理微信用户表
  @RequestMapping(value = "/disable_user", method = RequestMethod.POST)
  public boolean disableUser(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = AuthContext.identity();
    String cause = "";
    if (map.get("cause") != null) {
      cause = map.get("cause").toString();
    }
    String phone = map.get("phone").toString();
    logManageService.recordOperation(account, "禁用微信用户" + phone,
        "disable_user", "true");
    return wechatManageService.disableUser(cause, phone);
  }

  @RequestMapping(value = "/able_user", method = RequestMethod.POST)
  public boolean ableUser(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = AuthContext.identity();
    String phone = map.get("phone").toString();
    logManageService.recordOperation(account, "取消禁用微信用户" + phone,
        "able_user", "true");
    return wechatManageService.ableUser(phone);
  }

  @RequestMapping(value = "/search_user", method = RequestMethod.POST)
  public List<WeChatUsers> searchUser(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String phone = map.get("phone").toString();
    return wechatManageService.searchUser(phone);
  }

  @RequestMapping(value = "/screen_user", method = RequestMethod.POST)
  public List<WeChatUsers> screenUser(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String status = map.get("status").toString();
    return wechatManageService.screenUser(status);
  }

  @RequestMapping(value = "/count_wechat_user", method = RequestMethod.POST)
  public int countWeChatUser(HttpServletRequest request, HttpSession session) {
    return wechatManageService.countWeChatUser();
  }

  @RequestMapping(value = "/get_wechat_user", method = RequestMethod.POST)
  public List<WeChatUsers> getWeChatUser(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    int start = Integer.parseInt(map.get("start").toString());
    int number = Integer.parseInt(map.get("number").toString());
    return wechatManageService.getWeChatUser(start, number);
  }

}
