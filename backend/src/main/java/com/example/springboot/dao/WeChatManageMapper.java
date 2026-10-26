package com.example.springboot.dao;

import com.example.springboot.entity.WeChatUsers;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WeChatManageMapper {

  // 微信用户表相关接口
  void disableUser(@Param("cause") String cause, @Param("phone") String phone);

  void ableUser(@Param("phone") String phone);

  List<WeChatUsers> searchWeChatUser(@Param("phone") String phone);

  List<WeChatUsers> screenWeChatUser(@Param("status") String status);

  int countWeChatUser();

  List<WeChatUsers> getWeChatUser(@Param("start") int start, @Param("number") int number);

}
