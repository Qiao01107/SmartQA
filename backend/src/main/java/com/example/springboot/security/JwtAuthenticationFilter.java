package com.example.springboot.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Bearer 令牌认证过滤器。
 *
 * <p>只做一件事：读取 {@code Authorization: Bearer <jwt>}，校验签名/有效期/issuer/吊销状态，
 * 校验通过则把 {@link AuthPrincipal} 放入 SecurityContext。
 *
 * <p>任何校验失败都<b>不抛出异常、不写入 SecurityContext</b>，请求以匿名身份继续，
 * 由 Spring Security 的授权规则和 {@link RestAuthenticationEntryPoint} 统一返回 401。
 * 这样可以避免“解析失败反而返回 500”的信息泄露。
 *
 * <p>本过滤器只认 Authorization 头，不读 Cookie、不读查询参数、不读请求体，
 * 因此不存在“令牌从 URL 泄露到日志”或“Cookie 自动携带导致 CSRF”的问题。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  private static final String BEARER_PREFIX = "Bearer ";

  private static final String ATTR_ERROR = "smartqa.auth.error";

  private final JwtService jwtService;

  private final TokenRevocationService revocationService;

  public JwtAuthenticationFilter(JwtService jwtService,
      TokenRevocationService revocationService) {
    this.jwtService = jwtService;
    this.revocationService = revocationService;
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring(BEARER_PREFIX.length()).trim();
    if (token.isEmpty()) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      AuthPrincipal principal = jwtService.parse(token);
      if (revocationService.isRevoked(principal)) {
        request.setAttribute(ATTR_ERROR, "TOKEN_REVOKED");
        filterChain.doFilter(request, response);
        return;
      }
      // 角色直接来自签名后的 claim，客户端无法通过请求参数篡改。
      // 超级管理员同时持有 ROLE_SUPER_ADMIN 与 ROLE_ADMIN：
      // 普通管理员可用的接口对超管同样开放，而 hasRole("SUPER_ADMIN") 只匹配超管。
      List<SimpleGrantedAuthority> authorities =
          Roles.SUPER_ADMIN.equals(principal.role())
              ? List.of(new SimpleGrantedAuthority(Roles.SUPER_ADMIN),
                  new SimpleGrantedAuthority(Roles.ADMIN))
              : List.of(new SimpleGrantedAuthority(principal.role()));
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(principal, null, authorities);
      authentication.setDetails(
          new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (ExpiredJwtException e) {
      log.debug("[JWT] 令牌已过期：{}", e.getMessage());
      request.setAttribute(ATTR_ERROR, "TOKEN_EXPIRED");
    } catch (JwtException | IllegalArgumentException e) {
      // 签名错误、被篡改、格式损坏：一律视为未认证。
      log.debug("[JWT] 令牌校验失败（{}），按未认证处理：{}", e.getClass().getSimpleName(),
          e.getMessage());
      request.setAttribute(ATTR_ERROR, "TOKEN_INVALID");
    }

    filterChain.doFilter(request, response);
  }
}
