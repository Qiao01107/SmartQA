package com.example.springboot.controller;

import com.example.springboot.entity.WeChatUsers;
import com.example.springboot.service.WeChatInformationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class WeChatInformationController {

  @Autowired
  private WeChatInformationService weChatInformationService;

  // 个人信息
  @RequestMapping(value = "/wechat_information", method = RequestMethod.POST)
  public List<WeChatUsers> webInformation(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String phone = map.get("phone").toString();
    return weChatInformationService.wechatInformation(phone);
  }

}
