package com.example.springboot.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 令牌吊销语义测试。
 *
 * <p>重点覆盖一个真实踩到的边界：JWT 的 {@code iat} 精度只有秒，
 * “改密后立刻重新登录”拿到的新令牌不能被 {@code revokeAllIssuedBefore} 误杀。
 */
class TokenRevocationServiceTest {

  private static AuthPrincipal principal(String identity, String jti, Instant issuedAt) {
    return new AuthPrincipal(identity, Roles.WECHAT_USER, AuthPrincipal.TYPE_WECHAT, jti,
        issuedAt, issuedAt.plusSeconds(1800));
  }

  @Test
  @DisplayName("退出登录：按 jti 精确吊销该令牌")
  void revokesByJti() {
    TokenRevocationService svc = new TokenRevocationService();
    Instant now = Instant.now();
    AuthPrincipal p = principal("u1", "jti-1", now);
    assertFalse(svc.isRevoked(p));
    svc.revoke("jti-1", p.expiresAt());
    assertTrue(svc.isRevoked(p));
    // 同身份的其它令牌不受影响
    assertFalse(svc.isRevoked(principal("u1", "jti-2", now)));
  }

  @Test
  @DisplayName("改密后：此前签发的令牌全部失效")
  void revokesTokensIssuedBeforeCut() {
    TokenRevocationService svc = new TokenRevocationService();
    Instant old = Instant.now().minusSeconds(30);
    AuthPrincipal oldToken = principal("u2", "old", old);
    svc.revokeAllIssuedBefore("u2");
    assertTrue(svc.isRevoked(oldToken));
  }

  @Test
  @DisplayName("改密后同一秒内重新登录：新令牌不被误判为已吊销")
  void keepsTokenIssuedInSameSecondAsCut() {
    TokenRevocationService svc = new TokenRevocationService();
    svc.revokeAllIssuedBefore("u3");
    // 模拟同一秒内签发的令牌：iat 与切点同属一秒（iat 精度只到秒）
    Instant sameSecond = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    assertFalse(svc.isRevoked(principal("u3", "fresh", sameSecond)),
        "同一秒内新签发的令牌必须可用，否则用户改密后立刻登录会被 401 挡住");
  }

  @Test
  @DisplayName("吊销只作用于该身份")
  void doesNotAffectOtherIdentity() {
    TokenRevocationService svc = new TokenRevocationService();
    Instant old = Instant.now().minusSeconds(60);
    svc.revokeAllIssuedBefore("u4");
    assertFalse(svc.isRevoked(principal("u5", "other", old)));
  }

  @Test
  @DisplayName("空主体视为已吊销；空身份不抛异常")
  void handlesNullInputs() {
    TokenRevocationService svc = new TokenRevocationService();
    assertTrue(svc.isRevoked(null));
    svc.revokeAllIssuedBefore(null);
    svc.revokeAllIssuedBefore("   ");
    svc.revoke(null, null);
    assertFalse(svc.isRevoked(principal("u6", "j", Instant.now())));
  }
}
