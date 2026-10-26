package com.example.springboot.service;

import com.example.springboot.dao.WeChatInformationMapper;
import com.example.springboot.entity.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeChatInformationService {

  @Autowired
  private WeChatInformationMapper weChatInformationMapper;

  // 查看个人信息
  public List<WeChatUsers> wechatInformation(String phone) {
    return weChatInformationMapper.wechatInformation(phone);
  }

}
