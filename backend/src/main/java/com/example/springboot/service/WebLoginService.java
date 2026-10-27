package com.example.springboot.service;

import com.example.springboot.config.PasswordEncoderUtil;
import com.example.springboot.dao.AdminManageMapper;
import com.example.springboot.entity.WebUsers;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 管理端账号密码相关逻辑。
 *
 * <p>只负责“根据账号+密码校验身份”和“写入新的 BCrypt 哈希”，
 * 不签发令牌（令牌由控制器在鉴权成功后调用 {@code JwtService} 生成），
 * 也不负责短信验证码（见 {@link PasswordResetService}）。
 */
@Service
public class WebLoginService {

  private final AdminManageMapper adminManageMapper;
  private final PasswordEncoderUtil passwordEncoderUtil;

  public WebLoginService(AdminManageMapper adminManageMapper,
      PasswordEncoderUtil passwordEncoderUtil) {
    this.adminManageMapper = adminManageMapper;
    this.passwordEncoderUtil = passwordEncoderUtil;
  }

  /**
   * 网页端登录：用 BCrypt 校验密码。
   *
   * <p>同时校验登录页选择的身份类型与库中 adminType 一致，
   * 避免“普通管理员勾选超级管理员”这种前端声明被直接信任。
   *
   * @return 校验通过的管理员；失败返回 null（调用方不得区分“账号不存在”与“密码错误”）
   */
  public WebUsers login(String account, String password, char type) {
    if (account == null || password == null) {
      return null;
    }
    List<WebUsers> allWebUser = adminManageMapper.findAllWebUser();
    for (WebUsers i : allWebUser) {
      if (!account.equals(i.getAccount()) || type != i.getAdminType()) {
        continue;
      }
      if (passwordEncoderUtil.matches(password, i.getPassword())) {
        return i;
      }
    }
    return null;
  }

  /** 按账号查询管理员（供登录成功后取角色、以及找回密码流程使用）。 */
  public WebUsers findByAccount(String account) {
    if (account == null) {
      return null;
    }
    return adminManageMapper.searchWebUser(account);
  }

  /** 直接以 BCrypt 哈希覆盖密码（仅限已通过身份校验的调用方使用）。 */
  public boolean changePassword(String account, String newPassword) {
    if (account == null || newPassword == null || newPassword.isEmpty()) {
      return false;
    }
    adminManageMapper.changePassword(account, passwordEncoderUtil.encode(newPassword));
    return true;
  }
}
