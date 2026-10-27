package com.example.springboot.service;

import com.example.springboot.config.PasswordEncoderUtil;
import com.example.springboot.dao.AdminManageMapper;
import com.example.springboot.entity.WebUsers;
import com.example.springboot.security.PasswordPolicy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * 管理员账号管理。
 *
 * <p>权限边界（真正的授权判断在 {@code SecurityConfig} + {@code AuthContext}，
 * 这里只做业务规则兜底）：
 * <ul>
 *   <li>调用方一律是超级管理员（接口层已限制），本层不再信任任何来自请求体的角色字段；</li>
 *   <li>新建管理员必须满足 {@link PasswordPolicy}；</li>
 *   <li>禁止删除/降级最后一个超级管理员，避免把自己锁在系统外。</li>
 * </ul>
 */
@Service
public class AdminManageService {

  private static final Logger log = LoggerFactory.getLogger(AdminManageService.class);

  private final AdminManageMapper adminManageMapper;

  private final PasswordEncoderUtil passwordEncoderUtil;

  public AdminManageService(AdminManageMapper adminManageMapper,
      PasswordEncoderUtil passwordEncoderUtil) {
    this.adminManageMapper = adminManageMapper;
    this.passwordEncoderUtil = passwordEncoderUtil;
  }

  // 管理员表相关接口
  public boolean addWebUser(String name, String account, String phone, String adminType,
      String password) {
    if (isBlank(name) || isBlank(account) || isBlank(password)) {
      log.warn("[Admin] 新增管理员失败：姓名/账号/密码不能为空");
      return false;
    }
    if (!"0".equals(adminType) && !"1".equals(adminType)) {
      log.warn("[Admin] 新增管理员失败：adminType 非法（{}）", adminType);
      return false;
    }
    String policyError = PasswordPolicy.validateAdminPassword(password);
    if (policyError != null) {
      log.warn("[Admin] 新增管理员 {} 失败：{}", account, policyError);
      return false;
    }
    for (WebUsers i : adminManageMapper.findAllWebUser()) {
      if (i.getAccount().equals(account)) {
        log.warn("[Admin] 新增管理员失败：账号 {} 已存在", account);
        return false;
      }
    }
    String registerTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    // 密码以 BCrypt 哈希落库，不保存明文。
    adminManageMapper.addWebUser(name, account, phone, registerTime, adminType,
        passwordEncoderUtil.encode(password));
    return true;
  }

  /**
   * 删除管理员。
   *
   * @param account 待删除账号
   * @param operator 当前登录的超管账号（用于禁止自删）
   */
  public boolean removeWebUser(String account, String operator) {
    if (account == null || account.isBlank()) {
      return false;
    }
    if (account.equals(operator)) {
      log.warn("[Admin] 拒绝删除当前登录账号自身：{}", account);
      return false;
    }
    WebUsers target = adminManageMapper.searchWebUser(account);
    if (target == null) {
      return false;
    }
    if (target.getAdminType() == '1' && adminManageMapper.countSuperAdmin() <= 1) {
      log.warn("[Admin] 拒绝删除最后一个超级管理员：{}", account);
      return false;
    }
    try {
      adminManageMapper.removeWebUser(account);
      return true;
    } catch (DataIntegrityViolationException e) {
      // log 表通过外键 FK_LOG_REFERENCE_WEB_USER（ON DELETE RESTRICT）引用 web_user，
      // 已产生操作日志的管理员无法物理删除。这里保留审计数据、明确失败，不做级联删除。
      log.warn("[Admin] 删除管理员 {} 失败：存在关联操作日志（外键保护），"
          + "如需彻底清理请先归档日志。", account);
      return false;
    }
  }

  /**
   * 修改管理员手机号与角色。
   *
   * <p>若把最后一个超级管理员降级为普通管理员，会导致无人能管理系统，直接拒绝。
   */
  public boolean changeWebUser(String account, String phone, String adminType) {
    if (account == null || account.isBlank()) {
      return false;
    }
    if (!"0".equals(adminType) && !"1".equals(adminType)) {
      return false;
    }
    WebUsers target = adminManageMapper.searchWebUser(account);
    if (target == null) {
      return false;
    }
    if (target.getAdminType() == '1' && !"1".equals(adminType)
        && adminManageMapper.countSuperAdmin() <= 1) {
      log.warn("[Admin] 拒绝降级最后一个超级管理员：{}", account);
      return false;
    }
    adminManageMapper.changeAdmin(account, phone, adminType);
    return true;
  }

  public List<WebUsers> screenWebUser(String adminType) {
    return adminManageMapper.screenWebUser(adminType);
  }

  public int countWebUser() {
    return adminManageMapper.countWebUser();
  }

  public List<WebUsers> getWebUser(int start, int number) {
    int safeStart = Math.max(start, 1);
    int safeNumber = (number <= 0 || number > 100) ? 10 : number;
    return adminManageMapper.getWebUser(safeStart - 1, safeNumber);
  }

  // 修改个人信息（账号由令牌确定，不接受来自请求体的账号）
  public boolean changeSelfInfo(String phone, String email, String password, String account) {
    if (account == null || account.isBlank()) {
      return false;
    }
    // password 为空表示“不改密码”，保持原哈希不变。
    if (password == null || password.isEmpty()) {
      adminManageMapper.changeSelfInfoKeepPassword(phone, email, account);
      return true;
    }
    String policyError = PasswordPolicy.validateAdminPassword(password);
    if (policyError != null) {
      log.warn("[Admin] 账号 {} 修改密码失败：{}", account, policyError);
      return false;
    }
    adminManageMapper.changeSelfInfo(phone, email, passwordEncoderUtil.encode(password), account);
    return true;
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
