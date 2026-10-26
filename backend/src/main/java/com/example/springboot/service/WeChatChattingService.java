package com.example.springboot.service;

import com.example.springboot.dao.WeChatChattingMapper;
import com.example.springboot.entity.Chatting;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeChatChattingService {

  @Autowired
  private WeChatChattingMapper weChatChattingMapper;

  // 查看聊天记录
  public List<Chatting> wechatChatting(String phone) {
    return weChatChattingMapper.wechatChatting(phone);
  }

  // 添加聊天记录
  public boolean addChatting(String phone, String speaker, String content) {
    Date date = new Date();//获取当前的日期
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
    String str = df.format(date);
    weChatChattingMapper.addChatting(phone, speaker, content, str);
    return true;
  }

  // 清除历史记录
  public boolean clearHistory(String phone) {
    weChatChattingMapper.clearHistory(phone);
    return true;
  }

}
