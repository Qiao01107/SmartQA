package com.example.springboot.controller;

import com.example.springboot.entity.FeedbackUsers;
import com.example.springboot.service.WeChatFeedbackService;
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
public class WeChatFeedbackController {

  @Autowired
  private WeChatFeedbackService weChatFeedbackService;

  // 问题反馈
  @RequestMapping(value = "/wechat_feedback", method = RequestMethod.POST)
  public boolean feedback(@RequestBody Map map, HttpServletRequest request, HttpSession session) {
    String phone = map.get("phone").toString();
    String question = map.get("question").toString();
    String opinion = map.get("opinion").toString();
    return weChatFeedbackService.feedback(phone, question, opinion);
  }

  // 查看反馈
  @RequestMapping(value = "/wechat_feedbackView", method = RequestMethod.POST)
  public List<FeedbackUsers> feedbackView(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String phone = map.get("phone").toString();
    return weChatFeedbackService.feedbackView(phone);
  }

}
