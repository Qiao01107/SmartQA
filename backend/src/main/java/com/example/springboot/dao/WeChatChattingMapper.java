package com.example.springboot.dao;

import com.example.springboot.entity.Chatting;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WeChatChattingMapper {

  List<Chatting> wechatChatting(@Param("phone") String phone);

  void addChatting(@Param("phone") String phone, @Param("speaker") String speaker,
      @Param("content") String content, @Param("time") String time);

  void clearHistory(@Param("phone") String phone);

}
