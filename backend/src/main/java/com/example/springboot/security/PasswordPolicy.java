package com.example.springboot.security;

import java.util.Set;

/**
 * 管理员初始/新建密码强度策略。
 *
 * <p>背景：原项目初始管理员使用 {@code admin/admin} 这类弱凭据，属于已知安全问题。
 * 现在“创建管理员”和“初始超级管理员初始化”两个入口都要求满足以下规则：
 * <ul>
 *   <li>长度 8–64；</li>
 *   <li>至少包含一个字母和一个数字；</li>
 *   <li>不在常见弱口令黑名单中。</li>
 * </ul>
 *
 * <p>该策略<b>只作用于管理员</b>。小程序用户沿用原有“至少 6 位”的既有规则，
 * 避免在本次“鉴权专项修复”中擅自改变面向普通用户的注册体验。
 */
public final class PasswordPolicy {

  private static final int MIN_LENGTH = 8;

  private static final int MAX_LENGTH = 64;

  /** 常见弱口令（小写比较）。 */
  private static final Set<String> WEAK_PASSWORDS = Set.of(
      "admin", "admin123", "administrator", "12345678", "123456789", "1234567890",
      "password", "password1", "passw0rd", "qwerty123", "abc12345", "a1234567",
      "11111111", "00000000", "iloveyou", "root1234", "test1234", "admin@123");

  private PasswordPolicy() {
  }

  /**
   * 校验管理员密码。
   *
   * @param password 明文密码
   * @return null 表示通过；否则返回可直接展示给用户的中文原因
   */
  public static String validateAdminPassword(String password) {
    if (password == null || password.isEmpty()) {
      return "密码不能为空";
    }
    if (password.length() < MIN_LENGTH) {
      return "管理员密码长度至少 " + MIN_LENGTH + " 位";
    }
    if (password.length() > MAX_LENGTH) {
      return "密码长度不能超过 " + MAX_LENGTH + " 位";
    }
    boolean hasLetter = password.chars().anyMatch(Character::isLetter);
    boolean hasDigit = password.chars().anyMatch(Character::isDigit);
    if (!hasLetter || !hasDigit) {
      return "管理员密码必须同时包含字母和数字";
    }
    if (WEAK_PASSWORDS.contains(password.toLowerCase())) {
      return "该密码属于常见弱口令，请更换";
    }
    return null;
  }

  public static boolean isValidAdminPassword(String password) {
    return validateAdminPassword(password) == null;
  }
}
