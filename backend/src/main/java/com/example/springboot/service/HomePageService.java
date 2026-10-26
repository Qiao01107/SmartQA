package com.example.springboot.service;

import com.example.springboot.dao.AdminManageMapper;
import com.example.springboot.dao.LogManageMapper;
import com.example.springboot.entity.WebUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class HomePageService {

  @Autowired
  private AdminManageMapper adminManageMapper;

  @Autowired
  private LogManageMapper logManageMapper;

  public WebUsers getAdminInfo(String account) {
    return adminManageMapper.searchWebUser(account);
  }

  public String lastLoginTime(String account) {
    String lastLoginTime = logManageMapper.searchLogByAccount(account);
    return Objects.requireNonNullElse(lastLoginTime, "该管理员暂无上次登录记录。");
  }

  public boolean changeHead(String head, String account) {
    adminManageMapper.changeHead(head, account);
    return true;
  }

}
