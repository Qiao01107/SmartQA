package com.example.springboot.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * 令牌吊销表（进程内）。
 *
 * <p>JWT 是自包含的无状态凭证，服务端不会保存会话，因此“前端删掉 localStorage 里
 * 的字符串”并不能让令牌立刻失效——令牌在有效期内依然可用。为了让“退出登录”和
 * “修改密码”具备真实的服务端语义，这里维护两张极小的表：
 *
 * <ul>
 *   <li>{@code revokedJti}：jti → 该令牌自身的过期时间。用于退出登录，精确吊销单枚令牌。</li>
 *   <li>{@code revokeBeforeByIdentity}：身份 → 时间点。签发时间早于该时间点的令牌一律拒绝，
 *       用于“修改密码后立刻踢掉该账号此前签发的所有令牌”。</li>
 * </ul>
 *
 * <p><b>已知限制（必须如实告知）</b>：吊销表保存在单个 JVM 内存中。
 * 单实例部署时退出/改密立即生效；多实例或重启后，吊销记录丢失，
 * 未过期令牌会重新变为可用。彻底方案是引入 Redis 等共享存储或改用不透明令牌，
 * 本项目为课程交付范围，采用“短有效期（默认 30 分钟）+ 进程内吊销”的组合，
 * 并在报告中明确该边界，不宣称“前端删除令牌即等于服务端失效”。
 */
@Service
public class TokenRevocationService {

  /** 已吊销的 jti → 令牌过期时间（过期后可安全清理）。 */
  private final Map<String, Instant> revokedJti = new ConcurrentHashMap<>();

  /** 身份 → 吊销时间点，早于该时间签发的令牌全部失效。 */
  private final Map<String, Instant> revokeBeforeByIdentity = new ConcurrentHashMap<>();

  /** 吊销单枚令牌（退出登录）。 */
  public void revoke(String jti, Instant expiresAt) {
    if (jti == null) {
      return;
    }
    revokedJti.put(jti, expiresAt == null ? Instant.now() : expiresAt);
    purgeExpired();
  }

  /**
   * 吊销某个身份在 {@code now} 之前签发的所有令牌（修改密码后调用）。
   *
   * <p>时间点会被<b>截断到秒</b>：JWT 的 {@code iat} 精度只有秒，若这里保留纳秒，
   * 那么“改密后同一秒内重新登录”拿到的新令牌，其 {@code iat}（xx.000）会小于切点
   * （xx.4xx）而被误判为已吊销。截断后语义变为“早于切点所在秒签发的令牌失效”，
   * 代价是同一秒内签发的旧令牌最多多存活 1 秒（短有效期内可接受）。
   */
  public void revokeAllIssuedBefore(String identity) {
    if (identity == null || identity.isBlank()) {
      return;
    }
    revokeBeforeByIdentity.put(identity, Instant.now().truncatedTo(ChronoUnit.SECONDS));
  }

  /**
   * 判断令牌是否已被吊销。
   *
   * @param principal 已通过签名/有效期校验的主体
   */
  public boolean isRevoked(AuthPrincipal principal) {
    if (principal == null) {
      return true;
    }
    if (revokedJti.containsKey(principal.jti())) {
      return true;
    }
    Instant cut = revokeBeforeByIdentity.get(principal.identity());
    if (cut == null || principal.issuedAt() == null) {
      return false;
    }
    // 严格早于切点所在秒才算吊销：同一秒内新签发的令牌必须可用。
    return principal.issuedAt().truncatedTo(ChronoUnit.SECONDS).isBefore(cut);
  }

  /** 清理已自然过期的吊销记录，避免内存无界增长。 */
  private void purgeExpired() {
    Instant now = Instant.now();
    revokedJti.entrySet().removeIf(e -> e.getValue() != null && e.getValue().isBefore(now));
    revokeBeforeByIdentity.entrySet().removeIf(e -> {
      Instant cut = e.getValue();
      // 超过默认有效期上限（2 小时）后，此前签发的令牌必然已过期，记录可丢弃。
      return cut != null && cut.plusSeconds(7200).isBefore(now);
    });
  }
}
