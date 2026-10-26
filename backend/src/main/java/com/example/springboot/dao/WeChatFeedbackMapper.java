package com.example.springboot.dao;

import com.example.springboot.entity.FeedbackUsers;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WeChatFeedbackMapper {

  List<FeedbackUsers> findFeedback();

  List<FeedbackUsers> feedbackView(@Param("phone") String phone);

  void feedback(@Param("phone") String phone, @Param("opinion") String opinion,
      @Param("status") String status, @Param("question") String question,
      @Param("feedbackTime") String feedbackTime);

}
