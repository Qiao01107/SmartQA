package com.example.springboot.dao;

import com.example.springboot.entity.EntityRelation;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RelationManageMapper {

  // 关系表相关接口
  void addRelation(@Param("startEntityId") int startEntityId, @Param("endEntityId") int endEntityId,
      @Param("label") String label, @Param("relation") String relation);

  void removeRelation(@Param("startEntityId") int startEntityId,
      @Param("endEntityId") int endEntityId,
      @Param("label") String label, @Param("relation") String relation);

  void deleteInRelation(@Param("endEntityId") int endEntityId);

  void deleteOutRelation(@Param("startEntityId") int startEntityId);

  void changeRelation(@Param("startEntityId") int startEntityId,
      @Param("endEntityId") int endEntityId,
      @Param("oldLabel") String oldLabel, @Param("oldRelation") String oldRelation,
      @Param("newLabel") String newLabel, @Param("newRelation") String newRelation);

  List<EntityRelation> searchRelationByLabel(@Param("inquire") String inquire);

  List<EntityRelation> searchRelationByName(@Param("inquire") String inquire);

  int countRelation();

  List<EntityRelation> getRelation(@Param("start") int start, @Param("number") int number);

  List<String> getRelationLabel();

  List<EntityRelation> findAllRelation();

}
