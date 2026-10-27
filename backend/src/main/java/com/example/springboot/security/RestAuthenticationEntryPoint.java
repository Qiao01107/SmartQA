package com.example.springboot.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 未认证统一响应：HTTP 401 + JSON。
 *
 * <p>替代 Spring Security 默认的 HTML 登录页跳转，保证 Vue / 小程序拿到的是
 * 可解析的 JSON，而不是 302 或一段 HTML。
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException {
    Object error = request.getAttribute("smartqa.auth.error");
    String code = "UNAUTHORIZED";
    String message = "未认证或登录凭证已失效，请重新登录";
    if ("TOKEN_EXPIRED".equals(error)) {
      code = "TOKEN_EXPIRED";
      message = "登录凭证已过期，请重新登录";
    } else if ("TOKEN_REVOKED".equals(error)) {
      code = "TOKEN_REVOKED";
      message = "登录凭证已注销，请重新登录";
    } else if ("TOKEN_INVALID".equals(error)) {
      code = "TOKEN_INVALID";
      message = "登录凭证无效，请重新登录";
    }

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("success", false);
    body.put("code", code);
    body.put("message", message);
    body.put("path", request.getRequestURI());

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), body);
  }
}
