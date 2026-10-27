package com.example.springboot.controller;

import com.example.springboot.entity.FeedbackUsers;
import com.example.springboot.security.AuthContext;
import com.example.springboot.service.WeChatFeedbackService;
import com.example.springboot.tools.Payload;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序问题反馈（私有数据）。身份取自令牌，禁止查看或提交他人反馈。
 */
@RestController
public class WeChatFeedbackController {

  private final WeChatFeedbackService weChatFeedbackService;

  public WeChatFeedbackController(WeChatFeedbackService weChatFeedbackService) {
    this.weChatFeedbackService = weChatFeedbackService;
  }

  // 提交反馈（仅本人）
  @PostMapping("/wechat_feedback")
  public boolean feedback(@RequestBody Map<String, Object> map) {
    String phone = AuthContext.requireSelf(map.get("phone"));
    String question = Payload.text(map, "question");
    String opinion = Payload.text(map, "opinion");
    return weChatFeedbackService.feedback(phone, question, opinion);
  }

  // 查看反馈（仅本人）
  @PostMapping("/wechat_feedbackView")
  public List<FeedbackUsers> feedbackView(@RequestBody Map<String, Object> map) {
    String phone = AuthContext.requireSelf(map.get("phone"));
    return weChatFeedbackService.feedbackView(phone);
  }
}
