package com.example.springboot.controller;

import com.example.springboot.entity.WebUsers;
import com.example.springboot.service.AdminManageService;
import com.example.springboot.service.LogManageService;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminManageController {

  @Autowired
  private AdminManageService adminManageService;

  @Autowired
  private LogManageService logManageService;

  // 管理员表管理
  @RequestMapping(value = "/add_web_user", method = RequestMethod.POST)
  public boolean addWebUser(@RequestBody Map map, HttpServletRequest request, HttpSession session) {
    String operatorAccount = map.get("operatorAccount").toString();
    String name = map.get("name").toString();
    String account = map.get("account").toString();
    String phone = map.get("phone").toString();
    String adminType = map.get("adminType").toString();
    String password = map.get("password").toString();
    boolean status = adminManageService.addWebUser(name, account, phone, adminType, password);
    if (status) {
      logManageService.recordOperation(operatorAccount, "添加管理员" + account,
          "add_web_user", "true");
    } else {
      logManageService.recordOperation(operatorAccount, "添加管理员" + account,
          "add_web_user", "false");
    }
    return status;
  }

  @RequestMapping(value = "/remove_web_user", method = RequestMethod.POST)
  public boolean removeWebUser(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String operatorAccount = map.get("operatorAccount").toString();
    String account = map.get("account").toString();
    logManageService.recordOperation(operatorAccount, "删除管理员" + account,
        "remove_web_user", "true");
    return adminManageService.removeWebUser(account);
  }

  @RequestMapping(value = "/change_web_user", method = RequestMethod.POST)
  public boolean changeWebUser(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String operatorAccount = map.get("operatorAccount").toString();
    String account = map.get("account").toString();
    String phone = map.get("phone").toString();
    String adminType = map.get("adminType").toString();
    logManageService.recordOperation(operatorAccount, "修改管理员" + account,
        "change_web_user", "true");
    return adminManageService.changeWebUser(account, phone, adminType);
  }

  @RequestMapping(value = "/screen_web_user", method = RequestMethod.POST)
  public List<WebUsers> screenWebUser(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String adminType = map.get("adminType").toString();
    return adminManageService.screenWebUser(adminType);
  }

  @RequestMapping(value = "/count_web_user", method = RequestMethod.POST)
  public int countWebUser(HttpServletRequest request, HttpSession session) {
    return adminManageService.countWebUser();
  }

  @RequestMapping(value = "/get_web_user", method = RequestMethod.POST)
  public List<WebUsers> getWebUser(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    int start = Integer.parseInt(map.get("start").toString());
    int number = Integer.parseInt(map.get("number").toString());
    return adminManageService.getWebUser(start, number);
  }

  // 修改个人信息
  @RequestMapping(value = "/change_self_info", method = RequestMethod.POST)
  public boolean changeSelfInfo(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String phone = map.get("phone").toString();
    String email = map.get("email").toString();
    String password = map.get("password").toString();
    String account = map.get("account").toString();
    logManageService.recordOperation(account, "修改个人信息", "change_self_info",
        "true");
    return adminManageService.changeSelfInfo(phone, email, password, account);
  }

}
