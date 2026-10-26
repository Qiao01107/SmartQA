package com.example.springboot.dao;

import com.example.springboot.entity.*;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WeChatInformationMapper {

  List<WeChatUsers> wechatInformation(@Param("phone") String phone);

}
