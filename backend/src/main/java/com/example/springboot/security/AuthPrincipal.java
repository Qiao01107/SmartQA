package com.example.springboot.security;

import java.time.Instant;

/**
 * 已认证主体的最小信息集（由 JWT 解析得到，不包含任何密码或哈希）。
 *
 * <p>本项目的 web_user / wechat_users 两张表都以业务唯一键作为身份标识
 * （管理员用 account，小程序用户用 phone），没有独立的自增主键暴露给前端，
 * 因此这里的 {@code identity} 同时承担“身份标识”和“数据归属键”两个作用。
 *
 * @param identity  登录标识：管理员为 account，小程序用户为 phone
 * @param role      角色名，取值见 {@link Roles}
 * @param tokenType 令牌类型：{@link #TYPE_WEB} 或 {@link #TYPE_WECHAT}
 * @param jti       令牌唯一标识，用于退出登录时吊销
 * @param issuedAt  签发时间，用于“改密后吊销此前令牌”
 * @param expiresAt 过期时间
 */
public record AuthPrincipal(
    String identity,
    String role,
    String tokenType,
    String jti,
    Instant issuedAt,
    Instant expiresAt) {

  /** Web 管理端管理员令牌。 */
  public static final String TYPE_WEB = "WEB";

  /** 微信小程序用户令牌。 */
  public static final String TYPE_WECHAT = "WECHAT";

  public boolean isSuperAdmin() {
    return Roles.SUPER_ADMIN.equals(role);
  }

  public boolean isAdmin() {
    return Roles.ADMIN.equals(role) || Roles.SUPER_ADMIN.equals(role);
  }

  public boolean isWeChatUser() {
    return Roles.WECHAT_USER.equals(role);
  }
}
