package com.example.springboot.service;

import com.example.springboot.config.PasswordEncoderUtil;
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

  @Autowired
  private PasswordEncoderUtil passwordEncoderUtil;

  // 登录：用 BCrypt 校验密码
  public int wechatLogin(String phone, String password) {
    List<WeChatUsers> users = weChatLoginMapper.findUsers();
    for (WeChatUsers i : users) {
      if (!i.getPhone().equals(phone)) {
        continue;
      }
      if (!passwordEncoderUtil.matches(password, i.getPassword())) {
        continue;
      }
      if (i.getUserStatus().equals("0")) {
        return 0;
      } else if (i.getUserStatus().equals("1")) {
        return 1;
      }
    }
    return 2;
  }

  /** 按手机号查询小程序用户（登录成功后回填用户信息、以及数据归属校验时使用）。 */
  public WeChatUsers findByPhone(String phone) {
    if (phone == null) {
      return null;
    }
    for (WeChatUsers user : weChatLoginMapper.findUsers()) {
      if (phone.equals(user.getPhone())) {
        return user;
      }
    }
    return null;
  }

  // 注册：密码以 BCrypt 哈希落库
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
    weChatLoginMapper.register(wechatName, phone, str, profile_picture,
        passwordEncoderUtil.encode(password));
    return true;
  }

  // 修改密码：校验旧密码（BCrypt），新密码哈希落库
  public boolean wechatChangePassword(String phone, String oldPassword, String newPassword) {
    List<WeChatUsers> users = weChatLoginMapper.findUsers();
    for (WeChatUsers i : users) {
      if (i.getPhone().equals(phone)
          && passwordEncoderUtil.matches(oldPassword, i.getPassword())) {
        weChatLoginMapper.wechatChangePassword(phone, passwordEncoderUtil.encode(newPassword));
        return true;
      }
    }
    return false;
  }

}
