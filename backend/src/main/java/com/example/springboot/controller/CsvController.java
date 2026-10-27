package com.example.springboot.controller;

import com.example.springboot.security.ImportPathGuard;
import com.example.springboot.service.CsvService;
import com.example.springboot.tools.Payload;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据导入。
 *
 * <p>安全要点：请求体里的 {@code path} 不再直接用于 {@code new File(path)}，而是先经
 * {@link ImportPathGuard} 收敛到固定目录内的 {@code .csv} 文件，杜绝任意文件读取。
 * 校验失败返回 400 + 明确原因，不再因为 {@code FileNotFoundException} 变成 500/NPE。
 *
 * <p>接口只对超级管理员开放（见 {@code SecurityConfig.SUPER_ADMIN_ENDPOINTS}）。
 */
@RestController
public class CsvController {

  private final CsvService csvService;
  private final ImportPathGuard importPathGuard;

  public CsvController(
      CsvService csvService,
      @Value("${smartqa.data-import.base-dir:import-data}") String baseDir,
      @Value("${smartqa.data-import.max-size-bytes:0}") long maxSizeBytes) {
    this.csvService = csvService;
    this.importPathGuard = new ImportPathGuard(baseDir, maxSizeBytes);
  }

  // 数据导入数据库中
  @PostMapping("/import_data")
  public ResponseEntity<Map<String, Object>> importData(@RequestBody Map<String, Object> map) {
    Map<String, Object> body = new LinkedHashMap<>();
    try {
      Path file = importPathGuard.resolve(Payload.text(map, "path"));
      int lines = csvService.handleCsv(file);
      body.put("success", true);
      body.put("lines", lines);
      body.put("file", file.getFileName().toString());
      body.put("message", "已读取 " + lines + " 行");
      return ResponseEntity.ok(body);
    } catch (IllegalArgumentException e) {
      body.put("success", false);
      body.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(body);
    }
  }
}
