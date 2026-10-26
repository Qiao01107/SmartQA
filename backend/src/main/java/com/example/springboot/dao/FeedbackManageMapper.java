package com.example.springboot.dao;

import com.example.springboot.entity.FeedbackUsers;
import com.example.springboot.entity.Log;
import com.example.springboot.entity.WeChatUsers;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FeedbackManageMapper {

  // 用户反馈表相关接口
  WeChatUsers getUserInfo(@Param("phone") String phone);

  void changeFeedback(@Param("phone") String phone, @Param("opinion") String opinion,
      @Param("question") String question,
      @Param("status") String status,
      @Param("feedbackTime") String feedbackTime,
      @Param("evaluation") String evaluation);

  List<FeedbackUsers> screenFeedback(@Param("status") String status);

  int countFeedback();

  List<FeedbackUsers> getFeedback(@Param("start") int start, @Param("number") int number);

  List<FeedbackUsers> findAllFeedback();

  void recordOperation(@Param("account") String account, @Param("name") String name,
      @Param("operation") String operation,
      @Param("operationTime") String operationTime,
      @Param("usedInterface") String usedInterface,
      @Param("operationStatus") String operationStatus);

  List<Log> searchLog(@Param("inquire") String inquire);

  int countLog();

  List<Log> getLog(@Param("start") int start, @Param("number") int number);

}
