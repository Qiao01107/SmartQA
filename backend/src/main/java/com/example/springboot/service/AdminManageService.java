package com.example.springboot.service;

import com.example.springboot.dao.AdminManageMapper;
import com.example.springboot.entity.WebUsers;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminManageService {

  @Autowired
  private AdminManageMapper adminManageMapper;

  // 管理员表相关接口
  public boolean addWebUser(String name, String account, String phone, String adminType,
      String password) {
    List<WebUsers> webUsers = adminManageMapper.findAllWebUser();
    for (WebUsers i : webUsers) {
      if (i.getAccount().equals(account)) {
        return false;
      }
    }
    Date date = new Date();
    SimpleDateFormat nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String registerTime = nowTime.format(date);
    adminManageMapper.addWebUser(name, account, phone, registerTime, adminType, password);
    return true;
  }

  public boolean removeWebUser(String account) {
    adminManageMapper.removeWebUser(account);
    return true;
  }

  public boolean changeWebUser(String account, String phone, String adminType) {
    adminManageMapper.changeAdmin(account, phone, adminType);
    return true;
  }

  public List<WebUsers> screenWebUser(String adminType) {
    return adminManageMapper.screenWebUser(adminType);
  }

  public int countWebUser() {
    return adminManageMapper.countWebUser();
  }

  public List<WebUsers> getWebUser(int start, int number) {
    return adminManageMapper.getWebUser(start - 1, number);
  }

  // 修改个人信息
  public boolean changeSelfInfo(String phone, String email, String password, String account) {
    adminManageMapper.changeSelfInfo(phone, email, password, account);
    return true;
  }

}
