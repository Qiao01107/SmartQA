package com.example.springboot.controller;

import com.example.springboot.entity.Question;
import com.example.springboot.service.LogManageService;
import com.example.springboot.service.QuestionManageService;
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
public class QuestionManageController {

  @Autowired
  private QuestionManageService questionManageService;

  @Autowired
  private LogManageService logManageService;

  // 问题表管理
  @RequestMapping(value = "/add_question", method = RequestMethod.POST)
  public boolean addQuestion(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = map.get("account").toString();
    String question = map.get("question").toString();
    boolean status;
    if (map.get("answer") != null) {
      String answer = map.get("answer").toString();
      status = questionManageService.addQuestion(question, answer);
    } else {
      status = questionManageService.addQuestion(question, null);
    }
    if (status) {
      logManageService.recordOperation(account, "添加问题" + question,
          "add_question", "true");
    } else {
      logManageService.recordOperation(account, "添加问题" + question,
          "add_question", "false");
    }
    return status;
  }

  @RequestMapping(value = "/remove_question", method = RequestMethod.POST)
  public boolean removeQuestion(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = map.get("account").toString();
    String question = map.get("question").toString();
    boolean status = questionManageService.removeQuestion(question);
    if (status) {
      logManageService.recordOperation(account, "删除问题" + question,
          "remove_question", "true");
    } else {
      logManageService.recordOperation(account, "删除问题" + question,
          "remove_question", "false");
    }
    return status;
  }

  @RequestMapping(value = "/insist_remove_question", method = RequestMethod.POST)
  public boolean insistRemoveQuestion(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = map.get("account").toString();
    String question = map.get("question").toString();
    logManageService.recordOperation(account, "删除问题" + question,
        "insist_remove_question", "true");
    return questionManageService.insistRemoveQuestion(question);
  }

  @RequestMapping(value = "/change_question", method = RequestMethod.POST)
  public String changeQuestion(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = map.get("account").toString();
    int questionId = Integer.parseInt(map.get("questionId").toString());
    String newQuestion = map.get("newQuestion").toString();
    String reply = questionManageService.changeQuestion(questionId, newQuestion);
    if (reply.equals("修改成功!")) {
      logManageService.recordOperation(account, "修改问题" + questionId,
          "change_question", "true");
    } else {
      logManageService.recordOperation(account, "修改问题" + questionId,
          "change_question", "false");
    }
    return reply;
  }

  @RequestMapping(value = "/insist_change_question", method = RequestMethod.POST)
  public boolean insistChangeQuestion(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = map.get("account").toString();
    int questionId = Integer.parseInt(map.get("questionId").toString());
    String newQuestion = map.get("newQuestion").toString();
    logManageService.recordOperation(account, "修改问题" + questionId,
        "insist_change_question", "true");
    return questionManageService.insistChangeQuestion(questionId, newQuestion);
  }

  @RequestMapping(value = "/change_answer", method = RequestMethod.POST)
  public boolean changeAnswer(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = map.get("account").toString();
    int questionId = Integer.parseInt(map.get("questionId").toString());
    logManageService.recordOperation(account, "修改问题" + questionId + "的答案",
        "change_answer", "true");
    if (map.get("newAnswer") != null) {
      String newAnswer = map.get("newAnswer").toString();
      return questionManageService.changeAnswer(questionId, newAnswer);
    } else {
      return questionManageService.changeAnswer(questionId, null);
    }
  }

  @RequestMapping(value = "/search_question", method = RequestMethod.POST)
  public List<Question> searchQuestion(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String inquire = map.get("inquire").toString();
    return questionManageService.searchQuestion(inquire);
  }

  @RequestMapping(value = "/screen_question", method = RequestMethod.POST)
  public List<Question> screenQuestion(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String status = map.get("status").toString();
    return questionManageService.screenQuestion(status);
  }

  @RequestMapping(value = "/count_question", method = RequestMethod.POST)
  public int countQuestion(HttpServletRequest request, HttpSession session) {
    return questionManageService.countQuestion();
  }

  @RequestMapping(value = "/get_question", method = RequestMethod.POST)
  public List<Question> getQuestion(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    int start = Integer.parseInt(map.get("start").toString());
    int number = Integer.parseInt(map.get("number").toString());
    return questionManageService.getQuestion(start, number);
  }

}
