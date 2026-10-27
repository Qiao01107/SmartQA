package com.example.springboot.controller;

import com.example.springboot.entity.Chatting;
import com.example.springboot.security.AuthContext;
import com.example.springboot.service.WeChatChattingService;
import com.example.springboot.tools.Payload;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序聊天记录（私有数据）。
 *
 * <p>手机号一律取自令牌，请求体中的 phone 仅用于一致性校验，
 * 传入他人手机号会直接 403，无法读取或清除他人记录。
 */
@RestController
public class WeChatChattingController {

  private final WeChatChattingService weChatChattingService;

  public WeChatChattingController(WeChatChattingService weChatChattingService) {
    this.weChatChattingService = weChatChattingService;
  }

  // 聊天记录（仅本人）
  @PostMapping("/wechat_chatting")
  public List<Chatting> wechatChatting(@RequestBody Map<String, Object> map) {
    String phone = AuthContext.requireSelf(map.get("phone"));
    return weChatChattingService.wechatChatting(phone);
  }

  // 添加记录（仅本人）
  @PostMapping("/add_chatting")
  public boolean addChatting(@RequestBody Map<String, Object> map) {
    String phone = AuthContext.requireSelf(map.get("phone"));
    String speaker = Payload.text(map, "speaker");
    String content = Payload.text(map, "content");
    return weChatChattingService.addChatting(phone, speaker, content);
  }

  // 清除历史记录（仅本人）
  @PostMapping("/clearHistory")
  public boolean clearHistory(@RequestBody Map<String, Object> map) {
    String phone = AuthContext.requireSelf(map.get("phone"));
    return weChatChattingService.clearHistory(phone);
  }
}
