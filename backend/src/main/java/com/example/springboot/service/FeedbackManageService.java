package com.example.springboot.service;

import com.example.springboot.dao.FeedbackManageMapper;
import com.example.springboot.entity.FeedbackUsers;
import com.example.springboot.entity.WeChatUsers;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeedbackManageService {

  @Autowired
  private FeedbackManageMapper feedbackManageMapper;

  // 用户反馈表相关接口
  public WeChatUsers getUserInfo(String phone) {
    return feedbackManageMapper.getUserInfo(phone);
  }

  public boolean changeFeedback(String phone, String opinion, String question,
      String status, String feedbackTime, String evaluation) {
    if (status.equals("1") || status.equals("2")) {
      feedbackManageMapper.changeFeedback(phone, opinion, question, status, feedbackTime, evaluation);
      return true;
    }
    return false;
  }

  public List<FeedbackUsers> screenFeedback(String status) {
    return feedbackManageMapper.screenFeedback(status);
  }

  public List<FeedbackUsers> searchFeedback(String inquire) {
    List<FeedbackUsers> feedbackUsers = new ArrayList<>();
    List<FeedbackUsers> allFeedbackUsers = feedbackManageMapper.findAllFeedback();
    for (FeedbackUsers i:allFeedbackUsers) {
      if (i.getQuestion().contains(inquire)) {
        feedbackUsers.add(i);
      }
    }
    return feedbackUsers;
  }

  public int countFeedback() {
    return feedbackManageMapper.countFeedback();
  }

  public List<FeedbackUsers> getFeedback(int start, int number) {
    return feedbackManageMapper.getFeedback(start - 1, number);
  }

}
