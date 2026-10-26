package com.example.springboot.controller;

import com.example.springboot.service.LogManageService;
import com.example.springboot.service.RelationManageService;
import com.example.springboot.tools.EntityAndRelation;
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
public class RelationManageController {

  @Autowired
  private RelationManageService relationManageService;

  @Autowired
  private LogManageService logManageService;

  // 管理关系表
  @RequestMapping(value = "/add_relation", method = RequestMethod.POST)
  public EntityAndRelation addRelation(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = map.get("account").toString();
    String startLabel = map.get("startLabel").toString();
    String startName = map.get("startName").toString();
    String endLabel = map.get("endLabel").toString();
    String endName = map.get("endName").toString();
    String label = map.get("label").toString();
    String relation = map.get("relation").toString();
    EntityAndRelation entityAndRelation = relationManageService.addRelation(startLabel, startName,
        endLabel, endName, label, relation);
    if (entityAndRelation == null) {
      logManageService.recordOperation(account, "添加关系" + relation,
          "add_relation", "true");
    }  else {
      logManageService.recordOperation(account, "添加关系" + relation,
          "add_relation", "false");
    }
    return entityAndRelation;
  }

  @RequestMapping(value = "/remove_relation", method = RequestMethod.POST)
  public boolean removeRelation(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = map.get("account").toString();
    int startEntity = Integer.parseInt(map.get("startEntityId").toString());
    int endEntity = Integer.parseInt(map.get("endEntityId").toString());
    String label = map.get("label").toString();
    String relation = map.get("relation").toString();
    logManageService.recordOperation(account, "删除关系" + relation,
        "remove_relation", "true");
    return relationManageService.removeRelation(startEntity, endEntity, label, relation);
  }

  @RequestMapping(value = "/change_relation", method = RequestMethod.POST)
  public EntityAndRelation changeRelation(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    String account = map.get("account").toString();
    int startEntity = Integer.parseInt(map.get("startEntityId").toString());
    int endEntity = Integer.parseInt(map.get("endEntityId").toString());
    String oldLabel = map.get("oldLabel").toString();
    String oldName = map.get("oldName").toString();
    String newLabel = map.get("newLabel").toString();
    String newName = map.get("newName").toString();
    EntityAndRelation entityAndRelation = relationManageService.changeRelation(startEntity, endEntity, oldLabel, oldName, newLabel,
        newName);
    if (entityAndRelation == null) {
      logManageService.recordOperation(account, "修改关系" + oldName + "为" + newName,
          "change_relation", "true");
    } else {
      logManageService.recordOperation(account, "修改关系" + oldName + "为" + newName,
          "change_relation", "false");
    }
    return entityAndRelation;
  }

  @RequestMapping(value = "/search_relation", method = RequestMethod.POST)
  public List<EntityAndRelation> searchRelation(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    int field = Integer.parseInt(map.get("field").toString());
    String inquire = map.get("inquire").toString();
    return relationManageService.searchRelation(field, inquire);
  }

  @RequestMapping(value = "/count_relation", method = RequestMethod.POST)
  public int countRelation(HttpServletRequest request, HttpSession session) {
    return relationManageService.countRelation();
  }

  @RequestMapping(value = "/get_relation", method = RequestMethod.POST)
  public List<EntityAndRelation> getRelation(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    int start = Integer.parseInt(map.get("start").toString());
    int number = Integer.parseInt(map.get("number").toString());
    return relationManageService.getRelation(start, number);
  }

  @RequestMapping(value = "/get_relation_label", method = RequestMethod.POST)
  public List<String> getRelationLabel(@RequestBody Map map, HttpServletRequest request,
      HttpSession session) {
    return relationManageService.getRelationLabel();
  }

}
