package com.example.springboot.config;

import com.example.springboot.dao.AdminManageMapper;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 初始超级管理员初始化器。
 *
 * <p>安全边界：
 * <ul>
 *   <li><b>仅在 web_user 表为空</b>（不存在任何管理员）时才创建初始超级管理员，
 *       重复执行不会覆盖现有账号、不会重置现有密码。</li>
 *   <li>账号与密码从环境变量读取，密码以 BCrypt 哈希落库，不保存明文。</li>
 *   <li>初始化失败（如数据库异常）会抛出异常并记录 ERROR 日志，不静默跳过。</li>
 * </ul>
 *
 * <p>环境变量（可选，均有默认值）：
 * <ul>
 *   <li>INIT_ADMIN_ACCOUNT（默认 admin）</li>
 *   <li>INIT_ADMIN_PASSWORD（默认 admin）</li>
 *   <li>INIT_ADMIN_NAME（默认 超级管理员）</li>
 *   <li>INIT_ADMIN_PHONE（默认 13800000000）</li>
 * </ul>
 */
@Component
public class AdminInitializer implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

  private final AdminManageMapper adminManageMapper;
  private final PasswordEncoderUtil passwordEncoderUtil;

  private final String account;
  private final String password;
  private final String name;
  private final String phone;

  public AdminInitializer(AdminManageMapper adminManageMapper,
      PasswordEncoderUtil passwordEncoderUtil,
      @Value("${INIT_ADMIN_ACCOUNT:admin}") String account,
      @Value("${INIT_ADMIN_PASSWORD:admin}") String password,
      @Value("${INIT_ADMIN_NAME:超级管理员}") String name,
      @Value("${INIT_ADMIN_PHONE:13800000000}") String phone) {
    this.adminManageMapper = adminManageMapper;
    this.passwordEncoderUtil = passwordEncoderUtil;
    this.account = account;
    this.password = password;
    this.name = name;
    this.phone = phone;
  }

  @Override
  public void run(ApplicationArguments args) {
    int count = adminManageMapper.countWebUser();
    if (count > 0) {
      log.info("[AdminInit] web_user 表已有 {} 个管理员，跳过初始超级管理员创建（不覆盖、不重置）。",
          count);
      return;
    }
    if (isBlank(account) || isBlank(password)) {
      log.warn("[AdminInit] INIT_ADMIN_ACCOUNT / INIT_ADMIN_PASSWORD 未配置，跳过创建初始管理员。");
      return;
    }
    // BCrypt 哈希后落库，绝不保存明文。
    String encoded = passwordEncoderUtil.encode(password);
    String registerTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    adminManageMapper.addWebUser(name, account, phone, registerTime, "1", encoded);
    log.info("[AdminInit] 已创建初始超级管理员：account={}，adminType=1（密码以 BCrypt 哈希保存，"
        + "明文不记录到日志）。", account);
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
