package com.example.springboot.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码哈希工具（BCrypt）。
 *
 * <p>统一封装 BCrypt 的哈希与校验，供管理员/用户注册、登录、改密码等环节复用，
 * 避免各处直接 new BCryptPasswordEncoder 导致配置不一致。
 */
@Component
public class PasswordEncoderUtil {

  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  /** 对明文密码做 BCrypt 哈希（每次生成含随机盐，结果不同但校验一致）。 */
  public String encode(String rawPassword) {
    return encoder.encode(rawPassword);
  }

  /** 校验明文密码是否与已存哈希匹配。 */
  public boolean matches(String rawPassword, String encodedPassword) {
    if (rawPassword == null || encodedPassword == null) {
      return false;
    }
    return encoder.matches(rawPassword, encodedPassword);
  }

  /**
   * 判断一个字符串是否已经是 BCrypt 哈希（用于兼容存量明文数据时的迁移判断）。
   * BCrypt 哈希以 "$2a$" / "$2b$" / "$2y$" 开头，长度为 60。
   */
  public boolean isEncoded(String value) {
    if (value == null || value.length() != 60) {
      return false;
    }
    return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
  }
}
