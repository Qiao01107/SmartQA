package com.example.springboot.dao;

import com.example.springboot.entity.Question;
import com.example.springboot.entity.QuestionTemplate;
import com.example.springboot.entity.SearchTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WeChatReplyMapper {

  void addSearch(@Param("question") String question,@Param("searchTime") String searchTime ,@Param("reply")String reply);

  List<Question> findQue();

  List<QuestionTemplate> findQueTemp();

  List<SearchTime> findSearchTime();

}
