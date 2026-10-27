package com.example.springboot.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * JWT 签发与校验。
 *
 * <p>安全约定：
 * <ul>
 *   <li>签名算法固定为 HS256，密钥只从环境变量 {@code JWT_SECRET} 读取，<b>绝不硬编码</b>。</li>
 *   <li>密钥长度不足 32 字节时启动失败（fail-closed），避免使用弱密钥。</li>
 *   <li>未配置 {@code JWT_SECRET} 时生成一次性随机密钥并打印 WARN：
 *       进程重启后旧令牌全部失效，仅可用于本地临时启动，交付/生产环境必须显式配置。</li>
 *   <li>令牌包含 {@code iss / sub / iat / exp / jti / role / typ}，默认有效期 30 分钟（短有效期）。</li>
 * </ul>
 */
@Service
public class JwtService {

  private static final Logger log = LoggerFactory.getLogger(JwtService.class);

  /** HS256 要求密钥至少 256 位。 */
  private static final int MIN_SECRET_BYTES = 32;

  private static final long CLOCK_SKEW_SECONDS = 30L;

  private final SecretKey key;
  private final String issuer;
  private final Duration ttl;

  public JwtService(
      @Value("${JWT_SECRET:}") String secret,
      @Value("${JWT_ISSUER:smartqa}") String issuer,
      @Value("${JWT_TTL_MINUTES:30}") long ttlMinutes) {
    this.issuer = (issuer == null || issuer.isBlank()) ? "smartqa" : issuer.trim();
    long minutes = ttlMinutes <= 0 ? 30 : Math.min(ttlMinutes, 120);
    this.ttl = Duration.ofMinutes(minutes);
    this.key = Keys.hmacShaKeyFor(resolveSecret(secret));
  }

  private static byte[] resolveSecret(String rawSecret) {
    if (rawSecret == null || rawSecret.isBlank()) {
      byte[] random = new byte[MIN_SECRET_BYTES];
      new SecureRandom().nextBytes(random);
      log.warn("[JWT] 未配置环境变量 JWT_SECRET，已生成本次进程专用随机密钥。"
          + "进程重启后所有令牌立即失效；交付环境必须在 .env 中配置至少 32 字节的 JWT_SECRET。");
      return random;
    }
    String trimmed = rawSecret.trim();
    // 同时支持“原始字符串”和“Base64 编码字符串”两种写法：先按原文取字节，
    // 原文不足 32 字节时再尝试按 Base64 解码，两者都不足则拒绝启动。
    byte[] direct = trimmed.getBytes(StandardCharsets.UTF_8);
    if (direct.length >= MIN_SECRET_BYTES) {
      return direct;
    }
    try {
      byte[] decoded = Base64.getDecoder().decode(trimmed);
      if (decoded.length >= MIN_SECRET_BYTES) {
        return decoded;
      }
    } catch (IllegalArgumentException ignored) {
      // 不是合法 Base64，走下面的失败分支。
    }
    throw new IllegalStateException(
        "[JWT] JWT_SECRET 长度不足：HS256 要求密钥至少 " + MIN_SECRET_BYTES
            + " 字节（或等长的 Base64 字符串）。请在 .env 中配置更强的随机密钥，"
            + "例如执行 `openssl rand -base64 48` 生成。");
  }

  /** 令牌有效期（秒），用于登录响应中的 expiresIn 字段。 */
  public long getTtlSeconds() {
    return ttl.getSeconds();
  }

  /**
   * 为已通过密码校验的主体签发令牌。
   *
   * @return 紧凑序列化的 JWT 字符串
   */
  public String issue(AuthPrincipal principal) {
    Instant now = Instant.now();
    return Jwts.builder()
        .issuer(issuer)
        .subject(principal.identity())
        .id(principal.jti())
        .issuedAt(Date.from(principal.issuedAt() == null ? now : principal.issuedAt()))
        .expiration(Date.from(principal.expiresAt() == null
            ? now.plus(ttl) : principal.expiresAt()))
        .claim("role", principal.role())
        .claim("typ", principal.tokenType())
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  /**
   * 构造一个新的主体（含 jti 与有效期），供登录成功后调用。
   */
  public AuthPrincipal newPrincipal(String identity, String role, String tokenType) {
    Instant now = Instant.now();
    return new AuthPrincipal(identity, role, tokenType, UUID.randomUUID().toString(),
        now, now.plus(ttl));
  }

  /**
   * 校验签名、签发者与有效期，并解析出主体。
   *
   * @throws JwtException 签名无效、已过期、issuer 不匹配、格式损坏等
   */
  public AuthPrincipal parse(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(key)
        .requireIssuer(issuer)
        .clockSkewSeconds(CLOCK_SKEW_SECONDS)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    String identity = claims.getSubject();
    String role = claims.get("role", String.class);
    String typ = claims.get("typ", String.class);
    String jti = claims.getId();
    if (identity == null || identity.isBlank() || role == null || typ == null || jti == null) {
      throw new JwtException("令牌缺少必要声明");
    }
    return new AuthPrincipal(identity, role, typ, jti,
        claims.getIssuedAt().toInstant(), claims.getExpiration().toInstant());
  }
}
