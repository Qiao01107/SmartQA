package com.example.springboot.controller;

import com.example.springboot.entity.UserCollectible;
import com.example.springboot.security.AuthContext;
import com.example.springboot.service.WeChatCollectibleService;
import com.example.springboot.tools.Payload;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序收藏（私有数据）。身份取自令牌，禁止通过改 phone 操作他人收藏。
 */
@RestController
public class WeChatCollectibleController {

  private final WeChatCollectibleService weChatCollectibleService;

  public WeChatCollectibleController(WeChatCollectibleService weChatCollectibleService) {
    this.weChatCollectibleService = weChatCollectibleService;
  }

  // 查看收藏（仅本人）
  @PostMapping("/collectible")
  public List<UserCollectible> collectible(@RequestBody Map<String, Object> map) {
    String phone = AuthContext.requireSelf(map.get("phone"));
    return weChatCollectibleService.collectible(phone);
  }

  // 判断收藏（仅本人）
  @PostMapping("/get_collectible")
  public boolean getCollectible(@RequestBody Map<String, Object> map) {
    String phone = AuthContext.requireSelf(map.get("phone"));
    String question = Payload.text(map, "question");
    String reply = Payload.text(map, "reply");
    return weChatCollectibleService.getCollectible(phone, question, reply);
  }

  // 添加收藏（仅本人）
  @PostMapping("/add_collectible")
  public boolean addCollectible(@RequestBody Map<String, Object> map) {
    String phone = AuthContext.requireSelf(map.get("phone"));
    String question = Payload.text(map, "question");
    String reply = Payload.text(map, "reply");
    return weChatCollectibleService.addCollectible(phone, question, reply);
  }

  // 取消收藏（仅本人）
  @PostMapping("/remove_collectible")
  public boolean removeCollectible(@RequestBody Map<String, Object> map) {
    String phone = AuthContext.requireSelf(map.get("phone"));
    String question = Payload.text(map, "question");
    String reply = Payload.text(map, "reply");
    return weChatCollectibleService.removeCollectible(phone, question, reply);
  }
}
