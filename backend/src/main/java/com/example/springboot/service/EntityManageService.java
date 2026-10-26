package com.example.springboot.service;

import com.example.springboot.dao.AdminManageMapper;
import com.example.springboot.dao.EntityManageMapper;
import com.example.springboot.dao.RelationManageMapper;
import com.example.springboot.entity.Entity;
import com.example.springboot.entity.WebUsers;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntityManageService {

  @Autowired
  private EntityManageMapper entityManageMapper;

  @Autowired
  private RelationManageMapper relationManageMapper;

  @Autowired
  private AdminManageMapper adminManageMapper;

  @Autowired
  private Neo4jService neo4jService;

  // 实体表相关接口
  public int addEntity(String label, String name) {
    int exist = neo4jService.matchEntity(label, name);
    // System.out.println(exist);
    if (exist == -1) {
      int entityId = neo4jService.addEntity(label, name);
      entityManageMapper.addEntity(entityId, label, name);
      return -1;
    }
    return exist;
  }

  public int removeEntity(int id) {
    int relationNumber = neo4jService.countRelation(id);
    if (relationNumber == 0) {
      neo4jService.deleteEntity(id);
      entityManageMapper.removeEntity(id);
      return 0;
    } else {
      return relationNumber;
    }
  }

  public String insistRemoveEntity(String account, int id) {
    WebUsers webUser = adminManageMapper.searchWebUser(account);
    int relationNumber = neo4jService.countRelation(id);
    if (webUser.getAdminType() == '0' && relationNumber > 5) {
      return "该管理员无权删除该实体！";
    } else {
      // 先删除所有关系
      neo4jService.deleteAllRelation(id);
      relationManageMapper.deleteInRelation(id);
      relationManageMapper.deleteOutRelation(id);
      // 再删除实体
      neo4jService.deleteEntity(id);
      entityManageMapper.removeEntity(id);
      return "成功删除该实体与该实体上的关系。";
    }
  }

  public int changeEntity(int id, String label, String name) {
    // 确保修改后仍满足查重
    int entityId = neo4jService.matchEntity(label, name);
    if (entityId == -1) {
      Entity entity = entityManageMapper.searchEntityById(id);
      String oldLabel = entity.getLabel();
      neo4jService.updateEntity(id, oldLabel, label, name);
      entityManageMapper.changeEntity(id, label, name);
      return -1;
    } else {
      return entityId;
    }
  }

  public List<Entity> searchEntity(int field, String inquire) {
    if (field == 1) {
      return entityManageMapper.searchEntityByLabel(inquire);
    } else {
      return entityManageMapper.searchEntityByName(inquire);
    }
  }

  public int countEntity() {
    return entityManageMapper.countEntity();
  }

  public List<Entity> getEntity(int start, int number) {
    return entityManageMapper.getEntity(start - 1, number);
  }

  public List<String> getEntityLabel() {
    return entityManageMapper.getEntityLabel();
  }

}
