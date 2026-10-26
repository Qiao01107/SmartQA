package com.example.springboot.controller;

import com.example.springboot.entity.FeedbackUsers;
import com.example.springboot.entity.WeChatUsers;
import com.example.springboot.service.FeedbackManageService;
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
public class FeedbackManageController {

  @Autowired
  private FeedbackManageService feedbackManageService;

  @Autowired
  private LogManageService logManageService;

  // 管理用户反馈表
  @RequestMapping(value = "/get_info", method = RequestMethod.POST)
  public WeChatUsers getUserInfo(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String phone = map.get("phone").toString();
    return feedbackManageService.getUserInfo(phone);
  }

  @RequestMapping(value = "/change_feedback", method = RequestMethod.POST)
  public boolean changeFeedback(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = map.get("account").toString();
    String phone = map.get("phone").toString();
    String opinion = map.get("opinion").toString();
    String question = map.get("question").toString();
    String status = map.get("status").toString();
    String feedbackTime = map.get("feedbackTime").toString();
    String evaluation = map.get("evaluation").toString();
    boolean reply = feedbackManageService.changeFeedback(phone, opinion, question, status, feedbackTime,
        evaluation);
    if (reply) {
      logManageService.recordOperation(account, "审阅用户反馈", "change_feedback",
          "true");
    } else {
      logManageService.recordOperation(account, "审阅用户反馈", "change_feedback",
          "false");
    }
    return reply;
  }

  @RequestMapping(value = "/screen_feedback", method = RequestMethod.POST)
  public List<FeedbackUsers> screenFeedback(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String status = map.get("status").toString();
    return feedbackManageService.screenFeedback(status);
  }

  @RequestMapping(value = "/search_feedback", method = RequestMethod.POST)
  public List<FeedbackUsers> searchFeedback(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String inquire = map.get("inquire").toString();
    return feedbackManageService.searchFeedback(inquire);
  }

  @RequestMapping(value = "/count_feedback", method = RequestMethod.POST)
  public int countFeedback(HttpServletRequest request, HttpSession session) {
    return feedbackManageService.countFeedback();
  }

  @RequestMapping(value = "get_feedback", method = RequestMethod.POST)
  public List<FeedbackUsers> getFeedback(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    int start = Integer.parseInt(map.get("start").toString());
    int number = Integer.parseInt(map.get("number").toString());
    return feedbackManageService.getFeedback(start, number);
  }

}
