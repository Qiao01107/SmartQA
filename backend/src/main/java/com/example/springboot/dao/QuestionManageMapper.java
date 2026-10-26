package com.example.springboot.dao;

import com.example.springboot.entity.Question;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QuestionManageMapper {

  // 问题表相关接口
  void addQuestion(@Param("question") String question, @Param("answer") String answer,
      @Param("status") String status);

  void removeQuestion(@Param("question") String question);

  void changeQuestion(@Param("id") int id, @Param("question") String question);

  void changeAnswer(@Param("id") int id, @Param("answer") String answer,
      @Param("status") String status);

  List<Question> screenQuestion(@Param("status") String status);

  int countQuestion();

  List<Question> getQuestion(@Param("start") int start, @Param("number") int number);

  List<Question> findAllQuestion();

}
