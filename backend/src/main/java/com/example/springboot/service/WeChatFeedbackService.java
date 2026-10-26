package com.example.springboot.service;

import com.example.springboot.dao.WeChatFeedbackMapper;
import com.example.springboot.entity.FeedbackUsers;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeChatFeedbackService {

  @Autowired
  private WeChatFeedbackMapper weChatFeedbackMapper;

  // 问题反馈
  public boolean feedback(String phone, String question, String opinion) {
    List<FeedbackUsers> users = weChatFeedbackMapper.findFeedback();
    for (FeedbackUsers i : users) {
      if (i.getPhone().equals(phone) && i.getQuestion().equals(question)) {
        return false;
      }
    }
    Date date = new Date();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String str = df.format(date);
    String status = "0";
    weChatFeedbackMapper.feedback(phone, opinion, status, question, str);
    return true;
  }

  // 查看反馈
  public List<FeedbackUsers> feedbackView(String phone) {
    return weChatFeedbackMapper.feedbackView(phone);
  }

}
