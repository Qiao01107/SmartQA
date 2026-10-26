package com.example.springboot.controller;

import com.example.springboot.service.CsvService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsvController {

  @Autowired
  private CsvService csvService;

  // 数据导入数据库中
  @RequestMapping(value = "/import_data", method = RequestMethod.POST)
  public void importData(@RequestBody Map map) {
    String path = map.get("path").toString();
    csvService.handleCsv(path);
  }

}
