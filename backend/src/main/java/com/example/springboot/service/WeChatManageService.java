package com.example.springboot.service;

import com.example.springboot.dao.WeChatManageMapper;
import com.example.springboot.entity.WeChatUsers;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeChatManageService {

  @Autowired
  private WeChatManageMapper wechatManageMapper;

  // 微信用户表相关接口
  public boolean disableUser(String cause, String phone) {
    wechatManageMapper.disableUser(cause, phone);
    return true;
  }

  public boolean ableUser(String phone) {
    wechatManageMapper.ableUser(phone);
    return true;
  }

  public List<WeChatUsers> searchUser(String phone) {
    phone = phone + "%";
    return wechatManageMapper.searchWeChatUser(phone);
  }

  public List<WeChatUsers> screenUser(String status) {
    return wechatManageMapper.screenWeChatUser(status);
  }

  public int countWeChatUser() {
    return wechatManageMapper.countWeChatUser();
  }

  public List<WeChatUsers> getWeChatUser(int start, int number) {
    return wechatManageMapper.getWeChatUser(start - 1, number);
  }

}
