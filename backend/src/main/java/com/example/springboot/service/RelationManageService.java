package com.example.springboot.service;

import com.example.springboot.dao.EntityManageMapper;
import com.example.springboot.dao.RelationManageMapper;
import com.example.springboot.entity.Entity;
import com.example.springboot.entity.EntityRelation;
import com.example.springboot.tools.EntityAndRelation;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RelationManageService {

  @Autowired
  private RelationManageMapper relationManageMapper;

  @Autowired
  private EntityManageMapper entityManageMapper;

  @Autowired
  private Neo4jService neo4jService;

  // 关系表相关接口
  private List<EntityAndRelation> getEntityAndRelations(List<EntityAndRelation> entityAndRelations,
      List<EntityRelation> relations) {
    for (EntityRelation i : relations) {
      Entity startEntity = entityManageMapper.searchEntityById(i.getStartEntity());
      Entity endEntity = entityManageMapper.searchEntityById(i.getEndEntity());
      EntityAndRelation e = new EntityAndRelation(startEntity.getId(), startEntity.getLabel(),
          startEntity.getName(),
          endEntity.getId(), endEntity.getLabel(), endEntity.getName(),
          i.getLabel(), i.getRelation());
      entityAndRelations.add(e);
    }
    return entityAndRelations;
  }

  public EntityAndRelation addRelation(String startLabel, String startName,
      String endLabel, String endName, String label, String relation) {
    // 若不存在指定实体，则添加
    int startId = neo4jService.matchEntity(startLabel, startName);
    if (startId == -1) {
      startId = neo4jService.addEntity(startLabel, startName);
      entityManageMapper.addEntity(startId, startLabel, startName);
    }
    int endId = neo4jService.matchEntity(endLabel, endName);
    if (endId == -1) {
      endId = neo4jService.addEntity(endLabel, endName);
      entityManageMapper.addEntity(endId, endLabel, endName);
    }
    int relationId = neo4jService.matchRelation(startId, endId, label, relation);
    if (relationId == -1) {
      neo4jService.addRelation(startId, endId, label, relation);
      relationManageMapper.addRelation(startId, endId, label, relation);
      return null;
    } else {
      return new EntityAndRelation(startId, startLabel, startName,endId, endLabel, endName,
          label, relation);
    }
  }

  public boolean removeRelation(int startEntityId, int endEntityId, String label, String relation) {
    neo4jService.deleteRelation(startEntityId, endEntityId, label, relation);
    relationManageMapper.removeRelation(startEntityId, endEntityId, label, relation);
    return true;
  }

  public EntityAndRelation changeRelation(int startEntityId, int endEntityId,
      String oldLabel, String oldName,String newLabel, String newName) {
    int relationId = neo4jService.matchRelation(startEntityId, endEntityId, newLabel, newName);
    if (relationId == -1) {
      neo4jService.deleteRelation(startEntityId, endEntityId, oldLabel, oldName);
      neo4jService.addRelation(startEntityId, endEntityId, newLabel, newName);
      relationManageMapper.changeRelation(startEntityId, endEntityId, oldLabel, oldName, newLabel, newName);
      return null;
    } else {
      Entity startEntity = entityManageMapper.searchEntityById(startEntityId);
      String startLabel = startEntity.getLabel();
      String startName = startEntity.getName();
      Entity endEntity = entityManageMapper.searchEntityById(endEntityId);
      String endLabel = endEntity.getLabel();
      String endName = endEntity.getName();
      return new EntityAndRelation(startEntityId, startLabel, startName,
          endEntityId, endLabel, endName, oldLabel, oldName);
    }
  }

  public List<EntityAndRelation> searchRelation(int field, String inquire) {
    List<EntityRelation> relations;
    List<EntityAndRelation> entityAndRelations = new ArrayList<>();
    if (field == 1) {
      relations = relationManageMapper.searchRelationByLabel(inquire);
    } else {
      relations = relationManageMapper.searchRelationByName(inquire);
    }
    return getEntityAndRelations(entityAndRelations, relations);
  }

  public int countRelation() {
    return relationManageMapper.countRelation();
  }

  public List<EntityAndRelation> getRelation(int start, int number) {
    List<EntityAndRelation> entityAndRelations = new ArrayList<>();
    List<EntityRelation> relations = relationManageMapper.getRelation(start - 1, number);
    return getEntityAndRelations(entityAndRelations, relations);
  }

  public List<String> getRelationLabel() {
    return relationManageMapper.getRelationLabel();
  }

}
