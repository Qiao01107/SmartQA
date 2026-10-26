package com.example.springboot.dao;

import com.example.springboot.entity.Entity;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EntityManageMapper {

  // 实体表相关接口
  void addEntity(@Param("id") int id, @Param("label") String label, @Param("name") String name);

  void removeEntity(@Param("id") int id);

  void changeEntity(@Param("id") int id, @Param("label") String label,
      @Param("name") String name);

  Entity searchEntityById(@Param("id") int id);

  List<Entity> searchEntityByLabel(@Param("inquire") String inquire);

  List<Entity> searchEntityByName(@Param("inquire") String inquire);

  int countEntity();

  List<Entity> getEntity(@Param("start") int start, @Param("number") int number);

  List<String> getEntityLabel();

  int getEntityId(@Param("label") String label, @Param("name") String name);

  List<Entity> findAllEntity();

}
