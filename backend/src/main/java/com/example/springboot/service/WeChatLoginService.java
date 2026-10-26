package com.example.springboot.service;

import com.example.springboot.dao.WeChatLoginMapper;
import com.example.springboot.entity.WeChatUsers;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeChatLoginService {

  @Autowired
  private WeChatLoginMapper weChatLoginMapper;

  // 登录
  public int wechatLogin(String phone, String password) {
    List<WeChatUsers> users = weChatLoginMapper.findUsers();
    for (WeChatUsers i : users) {
      if (i.getPhone().equals(phone) && i.getPassword().equals(password) && i.getUserStatus().equals("0")) {
        return 0;
      } else if(i.getPhone().equals(phone) && i.getPassword().equals(password) && i.getUserStatus().equals("1")){
        return 1;
      }
    }
    return 2;
  }

  // 注册
  public boolean register(String wechatName, String phone, String profile_picture,
      String password) {
    List<WeChatUsers> users = weChatLoginMapper.findUsers();
    for (WeChatUsers i : users) {
      if (i.getPhone().equals(phone)) {
        return false;
      }
    }
    Date date = new Date();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String str = df.format(date);
    weChatLoginMapper.register(wechatName, phone, str, profile_picture, password);
    return true;
  }

  // 修改密码
  public boolean wechatChangePassword(String phone, String oldPassword, String newPassword) {
    List<WeChatUsers> users = weChatLoginMapper.findUsers();
    for (WeChatUsers i : users) {
      if (i.getPhone().equals(phone) && i.getPassword().equals(oldPassword)) {
        weChatLoginMapper.wechatChangePassword(phone, newPassword);
        return true;
      }
    }
    return false;
  }

}
