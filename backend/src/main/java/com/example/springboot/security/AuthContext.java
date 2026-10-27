package com.example.springboot.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 控制器内获取“当前登录者”的唯一入口。
 *
 * <p>设计目的：彻底切断“从请求体里读 account / phone 当作身份”的越权路径。
 * 身份只来源于经签名校验的令牌，请求体里的同名字段最多只能作为“待校验的声明值”，
 * 与令牌不一致时直接 403，绝不用于查询或写入。
 */
public final class AuthContext {

  private AuthContext() {
  }

  /**
   * 取当前已认证主体。
   *
   * @throws AuthenticationCredentialsNotFoundException 未携带有效令牌（→ 401）
   */
  public static AuthPrincipal principal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()
        || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
      throw new AuthenticationCredentialsNotFoundException("未认证或凭证已失效");
    }
    return principal;
  }

  /** 当前登录者标识：管理员为 account，小程序用户为 phone。 */
  public static String identity() {
    return principal().identity();
  }

  public static String role() {
    return principal().role();
  }

  public static boolean isSuperAdmin() {
    return principal().isSuperAdmin();
  }

  /** 只有超级管理员可以执行，否则 403。 */
  public static void requireSuperAdmin() {
    if (!isSuperAdmin()) {
      throw new AccessDeniedException("该操作仅限超级管理员");
    }
  }

  /**
   * 校验请求体中的自声明标识是否与令牌一致，返回可信的身份值。
   *
   * <p>客户端不传、传空串时直接使用令牌身份（正常前端行为）；
   * 传了但与令牌不一致时抛出 403，防止“改个 phone 就读别人的收藏/聊天/反馈”。
   *
   * @param claimed 请求体中的 phone / account 等自声明值，可为 null
   * @return 令牌中的真实身份，业务层必须使用该返回值
   */
  public static String requireSelf(Object claimedValue) {
    AuthPrincipal principal = principal();
    String claimed = claimedValue == null ? null : String.valueOf(claimedValue).trim();
    if (claimed != null && !claimed.isEmpty() && !claimed.equals(principal.identity())) {
      throw new AccessDeniedException("禁止访问或修改其他用户的数据");
    }
    return principal.identity();
  }
}
