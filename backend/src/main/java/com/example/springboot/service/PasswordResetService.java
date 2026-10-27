package com.example.springboot.service;

import com.example.springboot.entity.WebUsers;
import com.example.springboot.security.PasswordPolicy;
import com.zhenzi.sms.ZhenziSmsClient;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * “忘记密码”验证码流程（服务端校验）。
 *
 * <p>原实现的安全问题：验证码由服务端明文返回给浏览器、再由前端比对，
 * 且重置密码接口 {@code /change_password} 无需登录即可修改任意账号的密码——
 * 等于任何人都能重置管理员密码。此处的改造要点：
 *
 * <ol>
 *   <li>验证码只在服务端生成与保存，<b>绝不出现在任何 HTTP 响应中</b>；</li>
 *   <li>验证码 5 分钟过期、最多尝试 5 次、用后立即作废；</li>
 *   <li>短信通道未配置时<b>不生成验证码</b>，直接返回失败（fail-closed），
 *       不会退化成“无短信也能重置”；</li>
 *   <li>重置成功后吊销该账号此前签发的所有令牌。</li>
 * </ol>
 *
 * <p>验证码保存在进程内存中，单实例部署可用；重启后未使用的验证码失效，属于可接受行为。
 */
@Service
public class PasswordResetService {

  private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

  private static final Duration CODE_TTL = Duration.ofMinutes(5);

  private static final int MAX_ATTEMPTS = 5;

  private static final SecureRandom RANDOM = new SecureRandom();

  private final WebLoginService webLoginService;

  private final String smsApiUrl;

  private final String smsAppId;

  private final String smsAppSecret;

  private final String smsTemplateId;

  /** account → 验证码记录。 */
  private final Map<String, ResetCode> codes = new ConcurrentHashMap<>();

  public PasswordResetService(WebLoginService webLoginService,
      @Value("${ZHENZI_SMS_URL:https://sms_developer.zhenzikj.com}") String smsApiUrl,
      @Value("${ZHENZI_SMS_APP_ID:}") String smsAppId,
      @Value("${ZHENZI_SMS_APP_SECRET:}") String smsAppSecret,
      @Value("${ZHENZI_SMS_TEMPLATE_ID:7385}") String smsTemplateId) {
    this.webLoginService = webLoginService;
    this.smsApiUrl = smsApiUrl;
    this.smsAppId = smsAppId;
    this.smsAppSecret = smsAppSecret;
    this.smsTemplateId = smsTemplateId;
  }

  /**
   * 申请重置验证码。
   *
   * @return 统一结构 {@code {success, message}}；不返回验证码本身
   */
  public Map<String, Object> requestCode(String account, String phoneNumber) {
    if (isBlank(account) || isBlank(phoneNumber)) {
      return result(false, "账号和手机号不能为空");
    }
    WebUsers user = webLoginService.findByAccount(account);
    // 账号不存在与手机号不匹配使用同一句提示，避免枚举账号。
    if (user == null || user.getPhone() == null || !user.getPhone().equals(phoneNumber)) {
      return result(false, "账号与手机号不匹配");
    }
    if (isBlank(smsAppId) || isBlank(smsAppSecret)) {
      log.warn("[Reset] 榛子云短信未配置（ZHENZI_SMS_APP_ID/SECRET 为空），"
          + "已拒绝签发验证码，密码重置流程不可用。");
      return result(false, "短信服务未配置，暂时无法通过短信重置密码，请联系超级管理员");
    }

    String code = String.format("%06d", RANDOM.nextInt(1_000_000));
    boolean sent = sendSms(phoneNumber, code);
    if (!sent) {
      return result(false, "验证码发送失败，请稍后重试");
    }
    codes.put(account, new ResetCode(code, Instant.now().plus(CODE_TTL)));
    log.info("[Reset] 已为账号 {} 签发重置验证码（有效期 {} 分钟，日志不记录验证码明文）",
        account, CODE_TTL.toMinutes());
    return result(true, "验证码已发送，5 分钟内有效");
  }

  /**
   * 使用验证码重置密码。
   *
   * @return 统一结构 {@code {success, message}}
   */
  public Map<String, Object> resetPassword(String account, String phoneNumber, String code,
      String newPassword) {
    if (isBlank(account) || isBlank(phoneNumber) || isBlank(code)) {
      return result(false, "账号、手机号、验证码均不能为空");
    }
    String policyError = PasswordPolicy.validateAdminPassword(newPassword);
    if (policyError != null) {
      return result(false, policyError);
    }

    ResetCode record = codes.get(account);
    if (record == null || record.isExpired()) {
      codes.remove(account);
      return result(false, "验证码已过期，请重新获取");
    }
    if (record.attempts.incrementAndGet() > MAX_ATTEMPTS) {
      codes.remove(account);
      return result(false, "验证码错误次数过多，请重新获取");
    }
    WebUsers user = webLoginService.findByAccount(account);
    if (user == null || user.getPhone() == null || !user.getPhone().equals(phoneNumber)) {
      return result(false, "账号与手机号不匹配");
    }
    if (!record.code.equals(code.trim())) {
      return result(false, "验证码错误");
    }

    // 一次性使用：校验通过立刻作废，避免重放。
    codes.remove(account);
    boolean changed = webLoginService.changePassword(account, newPassword);
    if (!changed) {
      return result(false, "密码重置失败，请稍后重试");
    }
    log.info("[Reset] 账号 {} 通过短信验证码重置密码成功", account);
    return result(true, "密码重置成功，请使用新密码登录");
  }

  private boolean sendSms(String phoneNumber, String code) {
    try {
      String apiUrl = isBlank(smsApiUrl) ? "https://sms_developer.zhenzikj.com" : smsApiUrl;
      ZhenziSmsClient client = new ZhenziSmsClient(apiUrl, smsAppId, smsAppSecret);
      Map<String, Object> params = new HashMap<>();
      params.put("number", phoneNumber);
      params.put("templateId", smsTemplateId);
      params.put("templateParams", new String[] {code, "5"});
      client.send(params);
      return true;
    } catch (Exception e) {
      log.error("[Reset] 短信发送异常：{}", e.getMessage());
      return false;
    }
  }

  private static Map<String, Object> result(boolean success, String message) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("success", success);
    body.put("message", message);
    return body;
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  /** 单次验证码记录。 */
  private static final class ResetCode {

    private final String code;

    private final Instant expiresAt;

    private final java.util.concurrent.atomic.AtomicInteger attempts =
        new java.util.concurrent.atomic.AtomicInteger();

    private ResetCode(String code, Instant expiresAt) {
      this.code = code;
      this.expiresAt = expiresAt;
    }

    private boolean isExpired() {
      return Instant.now().isAfter(expiresAt);
    }
  }
}
