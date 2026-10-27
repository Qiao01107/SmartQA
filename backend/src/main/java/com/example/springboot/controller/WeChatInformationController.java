package com.example.springboot.controller;

import com.example.springboot.entity.WeChatUsers;
import com.example.springboot.security.AuthContext;
import com.example.springboot.service.WeChatInformationService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序个人信息（私有数据）。身份取自令牌，只能查询本人资料。
 */
@RestController
public class WeChatInformationController {

  private final WeChatInformationService weChatInformationService;

  public WeChatInformationController(WeChatInformationService weChatInformationService) {
    this.weChatInformationService = weChatInformationService;
  }

  // 个人信息（仅本人）
  @PostMapping("/wechat_information")
  public List<WeChatUsers> webInformation(@RequestBody Map<String, Object> map) {
    String phone = AuthContext.requireSelf(map.get("phone"));
    return weChatInformationService.wechatInformation(phone);
  }
}
