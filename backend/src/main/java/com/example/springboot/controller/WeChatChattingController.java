package com.example.springboot.controller;

import com.example.springboot.entity.Chatting;
import com.example.springboot.service.WeChatChattingService;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeChatChattingController {

  @Autowired
  private WeChatChattingService weChatChattingService;

  // 聊天记录
  @RequestMapping(value = "/wechat_chatting", method = RequestMethod.POST)
  public List<Chatting> wechatChatting(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String phone = map.get("phone").toString();
    return weChatChattingService.wechatChatting(phone);
  }

  // 添加记录
  @RequestMapping(value = "/add_chatting", method = RequestMethod.POST)
  public boolean addChatting(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String phone = map.get("phone").toString();
    String speaker = map.get("speaker").toString();
    String content = map.get("content").toString();
    return weChatChattingService.addChatting(phone, speaker, content);
  }

  // 清除历史记录
  @RequestMapping(value = "/clearHistory", method = RequestMethod.POST)
  public boolean clearHistory(@RequestBody Map map, HttpServletRequest request,
      HttpServletResponse response) {
    String phone = map.get("phone").toString();
    return weChatChattingService.clearHistory(phone);
  }

}
