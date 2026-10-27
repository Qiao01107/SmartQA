package com.example.springboot.controller;

import com.example.springboot.security.AuthContext;

import com.example.springboot.entity.WebUsers;
import com.example.springboot.service.HomePageService;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomePageController {

  @Autowired
  private HomePageService homePageService;

  @RequestMapping(value = "/get_admin_info", method = RequestMethod.POST)
  public WebUsers getAdminInfo(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = AuthContext.identity();
    return homePageService.getAdminInfo(account);
  }

  @RequestMapping(value = "/last_login", method = RequestMethod.POST)
  public String lastLogin(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = AuthContext.identity();
    return homePageService.lastLoginTime(account);
  }

  @RequestMapping(value = "/change_head", method = RequestMethod.POST)
  public boolean changeHeadPortrait(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String headNumber = map.get("head").toString();
    String account = AuthContext.identity();
    return homePageService.changeHead(headNumber, account);
  }

}
