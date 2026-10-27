package com.example.springboot.service;

import com.example.springboot.dao.EntityManageMapper;
import com.example.springboot.dao.RelationManageMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * CSV 导入。
 *
 * <p>安全相关的改动只在“如何取到文件”这一步：路径必须由
 * {@link com.example.springboot.security.ImportPathGuard} 先解析校验，本类只接受已经
 * 确认落在允许目录内的 {@link Path}，不再接收任意字符串路径。
 *
 * <p>另外去掉了原来逐行 {@code System.out.println(everyLine)} 的调试打印 ——
 * 那会把被导入文件的<b>全文写进服务端日志</b>，是实际的敏感信息泄露面。
 */
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
    }
    // 说明：关系导入的落库逻辑在原项目中即为空实现，本轮不扩展功能，
    // 因此这里只做行解析，不写入数据库（详见 FINAL_DELIVERY_REPORT 的已知限制）。
  }

  /**
   * 读取并逐行解析 CSV。
   *
   * @param csvPath 已经过安全校验的文件路径
   * @return 读取到的行数
   */
  public int handleCsv(Path csvPath) {
    List<String> allString = new ArrayList<>();
    try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
      String line;
      while ((line = br.readLine()) != null) {
        allString.add(line);
        importRelation(line);
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("导入文件读取失败：" + e.getClass().getSimpleName());
    }
    return allString.size();
  }

}
