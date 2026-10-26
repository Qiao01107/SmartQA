package com.example.springboot.controller;

import com.example.springboot.entity.Entity;
import com.example.springboot.service.EntityManageService;
import com.example.springboot.service.LogManageService;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EntityManageController {

  @Autowired
  private EntityManageService entityManageService;

  @Autowired
  private LogManageService logManageService;

  // 管理实体表
  @RequestMapping(value = "/add_entity", method = RequestMethod.POST)
  public int addEntity(@RequestBody Map map, HttpServletRequest request, HttpSession session) {
    String account = map.get("account").toString();
    String label = map.get("label").toString();
    String name = map.get("name").toString();
    int entityId = entityManageService.addEntity(label, name);
    if (entityId == -1) {
      logManageService.recordOperation(account, "添加实体", "add_entity",
          "true");
    } else {
      logManageService.recordOperation(account, "添加实体", "add_entity",
          "false");
    }
    return entityId;
  }

  @RequestMapping(value = "/remove_entity", method = RequestMethod.POST)
  public int removeEntity(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = map.get("account").toString();
    int id = Integer.parseInt(map.get("id").toString());
    int relationNumber = entityManageService.removeEntity(id);
    if (relationNumber == 0) {
      logManageService.recordOperation(account, "删除实体" + id, "remove_entity",
          "true");
    } else {
      logManageService.recordOperation(account, "删除实体" + id, "remove_entity",
          "false");
    }
    return relationNumber;
  }

  @RequestMapping(value = "/insist_remove_entity", method = RequestMethod.POST)
  public String insistRemoveEntity(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = map.get("account").toString();
    int id = Integer.parseInt(map.get("id").toString());
    String reply = entityManageService.insistRemoveEntity(account, id);
    if (reply.equals("该管理员无权删除该实体！")) {
      logManageService.recordOperation(account, "删除实体" + id,
          "insist_remove_entity", "false");
    } else {
      logManageService.recordOperation(account, "删除实体" + id,
          "insist_remove_entity", "true");
    }
    return reply;
  }

  @RequestMapping(value = "/change_entity", method = RequestMethod.POST)
  public int changeEntity(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = map.get("account").toString();
    int id = Integer.parseInt(map.get("id").toString());
    String label = map.get("label").toString();
    String name = map.get("name").toString();
    int entityId = entityManageService.changeEntity(id, label, name);
    if (entityId == -1) {
      logManageService.recordOperation(account, "修改实体" + id, "change_entity",
          "true");
    } else {
      logManageService.recordOperation(account, "修改实体" + id, "change_entity",
          "false");
    }
    return entityId;
  }

  @RequestMapping(value = "/search_entity", method = RequestMethod.POST)
  public List<Entity> searchEntity(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    int field = Integer.parseInt(map.get("field").toString());
    String inquire = map.get("inquire").toString();
    return entityManageService.searchEntity(field, inquire);
  }

  @RequestMapping(value = "/count_entity", method = RequestMethod.POST)
  public int countEntity(HttpServletRequest request, HttpSession session) {
    return entityManageService.countEntity();
  }

  @RequestMapping(value = "/get_entity", method = RequestMethod.POST)
  public List<Entity> getEntity(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    int start = Integer.parseInt(map.get("start").toString());
    int number = Integer.parseInt(map.get("number").toString());
    return entityManageService.getEntity(start, number);
  }

  @RequestMapping(value = "/get_entity_label", method = RequestMethod.POST)
  public List<String> getEntityLabel(HttpServletRequest request, HttpSession session) {
    return entityManageService.getEntityLabel();
  }

}
