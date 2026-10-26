package com.example.springboot.service;

import com.example.springboot.dao.EntityManageMapper;
import com.example.springboot.dao.RelationManageMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CsvService {

  @Autowired
  private EntityManageMapper entityManageMapper;

  @Autowired
  private RelationManageMapper relationManageMapper;

  @Autowired
  private Neo4jService neo4jService;

  public void importEntity(String line) {
    String[] entity = line.split(",");
    for (int i = 0 ; i < entity.length ; i ++) {
      entity[i] = entity[i].trim();
      // System.out.println(entity[i]);
    }
    String label = entity[2];
    String name = entity[1];
    int id = neo4jService.addEntity(label, name);
    entityManageMapper.addEntity(id, label, name);
  }

  public void importRelation(String line) {
    String[] entity = line.split("\t");
    for (int i = 0 ; i < entity.length ; i ++) {
      entity[i] = entity[i].trim();
      // System.out.println(entity[i]);
    }

  }

  public void handleCsv(String path) {
    // 1. .csv文件的路径。注意只有一个\的要改成\\
    // path = "E:\\桌面\\团队项目\\entity.csv";
    File csv = new File(path);
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(csv));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    String line = "";
    String everyLine = "";
    try {
      List<String> allString = new ArrayList<>();
      // 读取到的内容给line变量
      while ((line = br.readLine()) != null) {
        everyLine = line;
        System.out.println(everyLine);
        // importEntity(everyLine);
        importRelation(everyLine);
        allString.add(everyLine);
      }
      System.out.println("csv表格中所有行数：" + allString.size());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
