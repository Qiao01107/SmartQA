package com.example.springboot.dao;

import com.example.springboot.entity.WeChatUsers;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WeChatLoginMapper {

  List<WeChatUsers> findUsers();

  List<WeChatUsers> wechatChangePassword(@Param("phone") String phone,
      @Param("newPassword") String newPassword);

  void register(@Param("wechatName") String wechatName, @Param("phone") String phone,
      @Param("register_time") String register_time,
      @Param("profile_picture") String profile_picture, @Param("password") String password);

}
