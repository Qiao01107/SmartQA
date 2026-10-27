package com.example.springboot.controller;

import com.example.springboot.entity.Entity;
import com.example.springboot.security.AuthContext;
import com.example.springboot.service.EntityManageService;
import com.example.springboot.service.LogManageService;
import com.example.springboot.tools.Payload;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识图谱实体管理（ROLE_ADMIN / ROLE_SUPER_ADMIN）。
 *
 * <p>操作人一律取自令牌（{@link AuthContext#identity()}），
 * 请求体里的 account 不再参与身份判断，也不参与“该管理员是否有权强制删除”的判断——
 * 后者改用令牌中的角色声明 {@code isSuperAdmin()}，客户端无法伪造。
 */
@RestController
public class EntityManageController {

  private final EntityManageService entityManageService;
  private final LogManageService logManageService;

  public EntityManageController(EntityManageService entityManageService,
      LogManageService logManageService) {
    this.entityManageService = entityManageService;
    this.logManageService = logManageService;
  }

  // 管理实体表
  @PostMapping("/add_entity")
  public int addEntity(@RequestBody Map<String, Object> map) {
    String account = AuthContext.identity();
    String label = Payload.text(map, "label");
    String name = Payload.text(map, "name");
    int entityId = entityManageService.addEntity(label, name);
    logManageService.recordOperation(account, "添加实体", "add_entity",
        String.valueOf(entityId == -1));
    return entityId;
  }

  @PostMapping("/remove_entity")
  public int removeEntity(@RequestBody Map<String, Object> map) {
    String account = AuthContext.identity();
    int id = Payload.intValue(map, "id", -1);
    if (id <= 0) {
      return 0;
    }
    int relationNumber = entityManageService.removeEntity(id);
    logManageService.recordOperation(account, "删除实体" + id, "remove_entity",
        String.valueOf(relationNumber == 0));
    return relationNumber;
  }

  @PostMapping("/insist_remove_entity")
  public String insistRemoveEntity(@RequestBody Map<String, Object> map) {
    String account = AuthContext.identity();
    int id = Payload.intValue(map, "id", -1);
    if (id <= 0) {
      return "实体不存在";
    }
    // 角色来自签名令牌，普通管理员无法通过修改请求体冒充超级管理员。
    String reply = entityManageService.insistRemoveEntity(AuthContext.isSuperAdmin(), id);
    logManageService.recordOperation(account, "删除实体" + id, "insist_remove_entity",
        String.valueOf(!reply.equals(EntityManageService.DENIED_BY_ROLE)));
    return reply;
  }

  @PostMapping("/change_entity")
  public int changeEntity(@RequestBody Map<String, Object> map) {
    String account = AuthContext.identity();
    int id = Payload.intValue(map, "id", -1);
    String label = Payload.text(map, "label");
    String name = Payload.text(map, "name");
    if (id <= 0) {
      return 0;
    }
    int entityId = entityManageService.changeEntity(id, label, name);
    logManageService.recordOperation(account, "修改实体" + id, "change_entity",
        String.valueOf(entityId == -1));
    return entityId;
  }

  @PostMapping("/search_entity")
  public List<Entity> searchEntity(@RequestBody Map<String, Object> map) {
    int field = Payload.intValue(map, "field", 0);
    String inquire = Payload.text(map, "inquire");
    return entityManageService.searchEntity(field, inquire);
  }

  @PostMapping("/count_entity")
  public int countEntity() {
    return entityManageService.countEntity();
  }

  @PostMapping("/get_entity")
  public List<Entity> getEntity(@RequestBody Map<String, Object> map) {
    int start = Payload.intValue(map, "start", 1);
    int number = Payload.intValue(map, "number", 10);
    return entityManageService.getEntity(start, number);
  }

  @PostMapping("/get_entity_label")
  public List<String> getEntityLabel() {
    return entityManageService.getEntityLabel();
  }
}
