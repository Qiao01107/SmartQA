package com.example.springboot.dao;

import com.example.springboot.entity.WebUsers;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminManageMapper {

  // 管理员表相关接口
  void addWebUser(@Param("name") String name, @Param("account") String account,
      @Param("phone") String phone,
      @Param("registerTime") String registerTime,
      @Param("adminType") String adminType,
      @Param("password") String password);

  void removeWebUser(@Param("account") String account);

  void changePassword(@Param("account") String account, @Param("newPassword") String newPassword);

  void changeAdmin(@Param("account") String account, @Param("phone") String phone,
      @Param("adminType") String adminType);

  void changeHead(@Param("head") String head, @Param("account") String account);

  WebUsers searchWebUser(@Param("account") String account);

  List<WebUsers> screenWebUser(@Param("adminType") String adminType);

  int countWebUser();

  List<WebUsers> getWebUser(@Param("start") int start, @Param("number") int number);

  List<WebUsers> findAllWebUser();

  // 修改个人信息
  void changeSelfInfo(@Param("phone") String phone, @Param("email") String email,
      @Param("password") String password,
      @Param("account") String account);

}
