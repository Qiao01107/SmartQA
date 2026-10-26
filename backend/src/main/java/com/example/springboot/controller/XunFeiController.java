package com.example.springboot.controller;

import com.example.springboot.service.UrlService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.Map;

@Controller
public class XunFeiController {

  @Autowired
  UrlService urlService;

  @ResponseBody
  @GetMapping("/url")
  @CrossOrigin
  public String getUrl() {
    return urlService.getUrl();
  }

  /**
   * 未配置讯飞密钥时返回明确的 503 错误，而不是让空 URL 被前端当成可用地址。
   */
  @ExceptionHandler(IllegalStateException.class)
  @ResponseBody
  public ResponseEntity<Map<String, String>> handleConfigError(IllegalStateException e) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Collections.singletonMap("error", e.getMessage()));
  }

}
