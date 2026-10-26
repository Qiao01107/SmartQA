package com.example.springboot.controller;

import com.example.springboot.entity.Log;
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
public class LogManageController {

  @Autowired
  private LogManageService logManageService;

  // 操作日志相关内容
  @RequestMapping(value = "/search_log", method = RequestMethod.POST)
  public List<Log> searchLog(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String inquire = map.get("inquire").toString();
    return logManageService.searchLog(inquire);
  }

  @RequestMapping(value = "/count_log", method = RequestMethod.POST)
  public int countLog(HttpServletRequest request, HttpSession session) {
    return logManageService.countLog();
  }

  @RequestMapping(value = "/get_log", method = RequestMethod.POST)
  public List<Log> getLog(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    int start = Integer.parseInt(map.get("start").toString());
    int number = Integer.parseInt(map.get("number").toString());
    return logManageService.getLog(start, number);
  }

}
