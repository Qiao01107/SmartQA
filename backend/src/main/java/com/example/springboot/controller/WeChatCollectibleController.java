package com.example.springboot.controller;

import com.example.springboot.entity.UserCollectible;
import com.example.springboot.service.WeChatCollectibleService;
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
public class WeChatCollectibleController {

  @Autowired
  private WeChatCollectibleService weChatCollectibleService;

  // 查看收藏
  @RequestMapping(value = "/collectible", method = RequestMethod.POST)
  public List<UserCollectible> collectible(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String phone = map.get("phone").toString();
    return weChatCollectibleService.collectible(phone);
  }

  // 判断收藏
  @RequestMapping(value = "/get_collectible", method = RequestMethod.POST)
  public boolean getCollectible(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String phone = map.get("phone").toString();
    String question = map.get("question").toString();
    String reply = map.get("reply").toString();
    return weChatCollectibleService.getCollectible(phone, question, reply);
  }

  // 添加收藏
  @RequestMapping(value = "/add_collectible", method = RequestMethod.POST)
  public boolean addCollectible(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String phone = map.get("phone").toString();
    String question = map.get("question").toString();
    String reply = map.get("reply").toString();
    return weChatCollectibleService.addCollectible(phone, question, reply);
  }

  // 取消收藏
  @RequestMapping(value = "/remove_collectible", method = RequestMethod.POST)
  public boolean removeCollectible(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String phone = map.get("phone").toString();
    String question = map.get("question").toString();
    String reply = map.get("reply").toString();
    return weChatCollectibleService.removeCollectible(phone, question, reply);
  }

}
