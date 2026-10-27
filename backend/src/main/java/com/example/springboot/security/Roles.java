package com.example.springboot.security;

/**
 * 后端角色常量。
 *
 * <p>与数据库 adminType 的映射关系（唯一映射入口，避免各处硬编码）：
 * <ul>
 *   <li>adminType = '1'（超级管理员）→ ROLE_SUPER_ADMIN + ROLE_ADMIN</li>
 *   <li>adminType = '0'（普通管理员）→ ROLE_ADMIN</li>
 *   <li>微信小程序用户 → ROLE_WECHAT_USER</li>
 * </ul>
 *
 * <p>超级管理员额外持有 ROLE_ADMIN，是为了让“普通管理员可用”的接口对超管同样放行，
 * 而 {@code hasRole("SUPER_ADMIN")} 只匹配超管，不会反过来放宽普通管理员。
 */
public final class Roles {

  /** 普通管理员（adminType='0'）。 */
  public static final String ADMIN = "ROLE_ADMIN";

  /** 超级管理员（adminType='1'）。 */
  public static final String SUPER_ADMIN = "ROLE_SUPER_ADMIN";

  /** 微信小程序用户。 */
  public static final String WECHAT_USER = "ROLE_WECHAT_USER";

  private Roles() {
  }

  /**
   * 把数据库中的 adminType 映射为角色名。
   *
   * @param adminType 数据库字段值，仅接受 '0' / '1'
   * @return 角色名；非法值返回 null（调用方必须拒绝签发令牌）
   */
  public static String fromAdminType(Character adminType) {
    if (adminType == null) {
      return null;
    }
    if (adminType == '1') {
      return SUPER_ADMIN;
    }
    if (adminType == '0') {
      return ADMIN;
    }
    return null;
  }
}
