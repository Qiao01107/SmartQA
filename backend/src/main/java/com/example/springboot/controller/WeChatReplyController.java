package com.example.springboot.controller;

import com.example.springboot.service.WeChatReplyService;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeChatReplyController {

  @Autowired
  private WeChatReplyService weChatReplyService;

  // 热搜
  @RequestMapping(value = "/hotSearch", method = RequestMethod.POST)
  public List hotSearch(@RequestBody Map map, HttpServletRequest request, HttpServletResponse response) {
    return weChatReplyService.hotSearch();
  }

  // 问答
  @RequestMapping(value = "/apart", method = RequestMethod.POST)
  public String apart(@RequestBody Map map, HttpServletRequest request,
      HttpServletResponse response) {
    String text = map.get("text").toString();
    String answer = weChatReplyService.apart(text);
    weChatReplyService.addSearch(text,answer);
    return answer;
  }

}
