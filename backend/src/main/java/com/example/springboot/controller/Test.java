package com.example.springboot.controller;

import com.example.springboot.service.Neo4jService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class Test {

  @Autowired
  private Neo4jService neo4jService;

  // 开发阶段用于测试
  @RequestMapping(value = "/test", method = RequestMethod.POST)
  public void test(@RequestBody Map map) {
    // Neo4jService 由 Spring 注入，不再手动 new。
    // neo4jService.allEntity();
     neo4jService.allRelation();
//    int id = Integer.parseInt(map.get("id").toString());
//    String oldLabel = map.get("oldLabel").toString();
//    String newLabel = map.get("newLabel").toString();
//    String newName = map.get("newName").toString();
//    neo4jService.updateEntity(id,oldLabel,newLabel,newName);
  }

}
