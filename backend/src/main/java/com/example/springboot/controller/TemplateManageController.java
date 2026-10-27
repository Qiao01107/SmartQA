package com.example.springboot.controller;

import com.example.springboot.security.AuthContext;

import com.example.springboot.entity.QuestionTemplate;
import com.example.springboot.service.LogManageService;
import com.example.springboot.service.TemplateManageService;
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
public class TemplateManageController {

  @Autowired
  private TemplateManageService templateManageService;

  @Autowired
  private LogManageService logManageService;

  // 问题模板表管理
  @RequestMapping(value = "/add_template", method = RequestMethod.POST)
  public boolean addTemplate(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = AuthContext.identity();
    String template = map.get("questionTemplate").toString();
    String question = map.get("preciseQuestion").toString();
    boolean status = templateManageService.addTemplate(template, question);
    if (status) {
      logManageService.recordOperation(account, "添加问题模板",
          "add_template", "true");
    } else {
      logManageService.recordOperation(account, "添加问题模板",
          "add_template", "false");
    }
    return status;
  }

  @RequestMapping(value = "/remove_template", method = RequestMethod.POST)
  public boolean removeTemplate(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = AuthContext.identity();
    String template = map.get("questionTemplate").toString();
    logManageService.recordOperation(account, "删除问题模板",
        "remove_template", "true");
    return templateManageService.removeTemplate(template);
  }

  @RequestMapping(value = "/change_template", method = RequestMethod.POST)
  public boolean changeTemplate(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = AuthContext.identity();
    int questionId = Integer.parseInt(map.get("questionId").toString());
    String oldTemplate = map.get("oldTemplate").toString();
    String newTemplate = map.get("newTemplate").toString();
    boolean status = templateManageService.changeTemplate(questionId, oldTemplate, newTemplate);
    if (status) {
      logManageService.recordOperation(account, "修改问题模板",
          "change_template", "true");
    } else {
      logManageService.recordOperation(account, "修改问题模板",
          "change_template", "false");
    }
    return status;
  }

  @RequestMapping(value = "/search_template", method = RequestMethod.POST)
  public List<QuestionTemplate> searchTemplate(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    int field = Integer.parseInt(map.get("field").toString());
    String inquire = map.get("inquire").toString();
    return templateManageService.searchTemplate(field, inquire);
  }

  @RequestMapping(value = "/count_template", method = RequestMethod.POST)
  public int countTemplate(HttpServletRequest request, HttpSession session) {
    return templateManageService.countTemplate();
  }

  @RequestMapping(value = "/get_template", method = RequestMethod.POST)
  public List<QuestionTemplate> getTemplate(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    int start = Integer.parseInt(map.get("start").toString());
    int number = Integer.parseInt(map.get("number").toString());
    return templateManageService.getTemplate(start, number);
  }

}
