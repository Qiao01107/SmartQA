package com.example.springboot.service;

import com.example.springboot.dao.AdminManageMapper;
import com.example.springboot.dao.LogManageMapper;
import com.example.springboot.entity.Log;
import com.example.springboot.entity.WebUsers;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogManageService {

  @Autowired
  private LogManageMapper logManageMapper;

  @Autowired
  private AdminManageMapper adminManageMapper;

  // 操作日志相关
  public void recordOperation(String account, String operation, String usedInterface,
      String operationStatus) {
    WebUsers webUser = adminManageMapper.searchWebUser(account);
    String name = webUser.getName();
    Date date = new Date();
    SimpleDateFormat nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String operationTime = nowTime.format(date);
    logManageMapper.recordOperation(account, name, operation, operationTime, usedInterface,
        operationStatus);
  }

  public List<Log> searchLog(String inquire) {
    return logManageMapper.searchLog(inquire);
  }

  public int countLog() {
    return logManageMapper.countLog();
  }

  public List<Log> getLog(int start, int number) {
    return logManageMapper.getLog(start - 1, number);
  }

}
