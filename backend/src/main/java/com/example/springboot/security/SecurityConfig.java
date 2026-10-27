package com.example.springboot.security;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 统一安全配置（Spring Security 6）。
 *
 * <p>总体策略：<b>默认拒绝，逐条放行</b>。
 * <ul>
 *   <li>仅登录/注册/找回密码等少数入口允许匿名；</li>
 *   <li>管理员账号管理（增删改查）只允许超级管理员；</li>
 *   <li>知识图谱、问题、模板、反馈、日志、小程序用户管理等后台业务允许两种管理员；</li>
 *   <li>小程序私有数据（聊天、收藏、反馈、个人信息、改密）只允许小程序用户角色，
 *       并在控制器里再校验“数据归属 = 令牌身份”；</li>
 *   <li>未列出的任何路径一律 {@code denyAll()}，新增接口若忘记登记会默认 403，
 *       而不是默认可访问。</li>
 * </ul>
 *
 * <p>CSRF 说明：认证凭证只通过 {@code Authorization: Bearer} 头传递，
 * <b>不使用 Cookie、不使用 Session</b>（{@link SessionCreationPolicy#STATELESS}），
 * 浏览器不会自动附带该头，因此不存在“跨站请求自动携带凭证”的 CSRF 攻击面，
 * 关闭 CSRF 是与之匹配的配置，而非为了通过测试而关闭安全保护。
 * 若将来改为 Cookie 承载令牌，必须同时开启 CSRF 并配套 SameSite 策略。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  /** 允许匿名访问的入口（登录、注册、找回密码，以及错误页）。 */
  private static final String[] PUBLIC_ENDPOINTS = {
      "/web_login",
      "/wechat_login",
      "/wechat_register",
      "/forget_password",
      "/reset_password",
      "/error"
  };

  /** 超级管理员专属：管理员账号的增删改查 + 全量数据导入。 */
  private static final String[] SUPER_ADMIN_ENDPOINTS = {
      "/add_web_user",
      "/remove_web_user",
      "/change_web_user",
      "/screen_web_user",
      "/get_web_user",
      "/count_web_user",
      "/import_data"
  };

  /** 管理员可用（含超管）：知识图谱/问题/模板/反馈/日志/小程序用户管理/个人中心。 */
  private static final String[] ADMIN_ENDPOINTS = {
      "/add_entity", "/remove_entity", "/insist_remove_entity", "/change_entity",
      "/search_entity", "/count_entity", "/get_entity", "/get_entity_label",
      "/add_relation", "/remove_relation", "/change_relation",
      "/search_relation", "/count_relation", "/get_relation", "/get_relation_label",
      "/add_question", "/remove_question", "/insist_remove_question",
      "/change_question", "/insist_change_question", "/change_answer",
      "/search_question", "/screen_question", "/count_question", "/get_question",
      "/add_template", "/remove_template", "/change_template",
      "/search_template", "/count_template", "/get_template",
      "/get_feedback", "/screen_feedback", "/search_feedback",
      "/count_feedback", "/change_feedback", "/get_info",
      "/search_log", "/count_log", "/get_log",
      "/disable_user", "/able_user", "/search_user", "/screen_user",
      "/count_wechat_user", "/get_wechat_user",
      "/get_admin_info", "/last_login", "/change_head", "/change_self_info",
      "/change_password", "/web_logout"
  };

  /** 小程序用户专属：只能操作“自己的”私有数据。 */
  private static final String[] WECHAT_USER_ENDPOINTS = {
      "/wechat_chatting", "/add_chatting", "/clearHistory",
      "/collectible", "/get_collectible", "/add_collectible", "/remove_collectible",
      "/wechat_feedback", "/wechat_feedbackView",
      "/wechat_information",
      "/wechat_change_password",
      "/apart",
      "/wechat_logout"
  };

  /** 两种身份都可用的公共只读接口。 */
  private static final String[] SHARED_AUTHENTICATED_ENDPOINTS = {
      "/hotSearch",
      "/url"
  };

  @Bean
  public PasswordEncoder passwordEncoder() {
    // 统一使用 BCrypt（强度 10，与既有库中的 $2a$ 哈希兼容）。
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      RestAuthenticationEntryPoint authenticationEntryPoint,
      RestAccessDeniedHandler accessDeniedHandler) throws Exception {
    http
        // 纯 Bearer Token + 无 Cookie/无 Session，CSRF 不适用（详见类注释）。
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .httpBasic(basic -> basic.disable())
        .formLogin(form -> form.disable())
        .logout(logout -> logout.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(handling -> handling
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler))
        .authorizeHttpRequests(auth -> auth
            // 预检请求不需要认证（CORS 规范要求）。
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
            .requestMatchers(SUPER_ADMIN_ENDPOINTS).hasRole("SUPER_ADMIN")
            .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
            .requestMatchers(WECHAT_USER_ENDPOINTS).hasRole("WECHAT_USER")
            .requestMatchers(SHARED_AUTHENTICATED_ENDPOINTS)
            .hasAnyRole("ADMIN", "SUPER_ADMIN", "WECHAT_USER")
            // 未登记的路径默认拒绝，避免“新接口忘了加权限=默认可访问”。
            .anyRequest().denyAll())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * CORS：只允许本地开发前端源，且 {@code allowCredentials=false}。
   *
   * <p>认证凭证走 Authorization 头，与 Cookie 无关，因此不需要放开凭据共享；
   * 生产环境应通过 {@code CORS_ALLOWED_ORIGINS} 环境变量显式指定前端域名。
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${CORS_ALLOWED_ORIGINS:http://127.0.0.1:8080,http://localhost:8080}")
      String allowedOrigins) {
    List<String> origins = Arrays.stream(allowedOrigins.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();

    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(origins);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
    configuration.setAllowCredentials(false);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
