package com.example.springboot.dao;

import com.example.springboot.entity.Log;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LogManageMapper {

  void recordOperation(@Param("account") String account, @Param("name") String name,
      @Param("operation") String operation,
      @Param("operationTime") String operationTime,
      @Param("usedInterface") String usedInterface,
      @Param("operationStatus") String operationStatus);

  List<Log> searchLog(@Param("inquire") String inquire);

  String searchLogByAccount(@Param("account") String account);

  int countLog();

  List<Log> getLog(@Param("start") int start, @Param("number") int number);

}
