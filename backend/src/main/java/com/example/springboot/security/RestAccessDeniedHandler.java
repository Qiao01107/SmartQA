package com.example.springboot.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * 已认证但无权限统一响应：HTTP 403 + JSON。
 *
 * <p>与 401 明确区分：401 表示“你没登录或凭证无效”，403 表示“你登录了，但这个角色不能做这件事”。
 * 前端据此决定是跳登录页还是提示无权访问，而不是把 403 也当成掉线。
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  public RestAccessDeniedHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException {
    // Spring Security 在"已认证但权限不足"时抛出的是英文 "Access Denied"，
    // 这里统一替换成中文；只有业务层主动抛出并带中文原因时才透传。
    String raw = accessDeniedException.getMessage();
    String message = (raw == null || raw.isBlank() || "Access Denied".equalsIgnoreCase(raw))
        ? "当前角色无权访问该接口" : raw;

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("success", false);
    body.put("code", "FORBIDDEN");
    body.put("message", message);
    body.put("path", request.getRequestURI());

    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), body);
  }
}
