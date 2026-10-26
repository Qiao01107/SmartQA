package com.example.springboot.service;

import com.example.springboot.dao.AdminManageMapper;
import com.example.springboot.entity.WebUsers;
import com.zhenzi.sms.ZhenziSmsClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WebLoginService {

  private final AdminManageMapper adminManageMapper;
  private final String smsApiUrl;
  private final String smsAppId;
  private final String smsAppSecret;
  private final String smsTemplateId;

  public WebLoginService(AdminManageMapper adminManageMapper,
      @Value("${ZHENZI_SMS_URL:https://sms_developer.zhenzikj.com}") String smsApiUrl,
      @Value("${ZHENZI_SMS_APP_ID:}") String smsAppId,
      @Value("${ZHENZI_SMS_APP_SECRET:}") String smsAppSecret,
      @Value("${ZHENZI_SMS_TEMPLATE_ID:7385}") String smsTemplateId) {
    this.adminManageMapper = adminManageMapper;
    this.smsApiUrl = smsApiUrl;
    this.smsAppId = smsAppId;
    this.smsAppSecret = smsAppSecret;
    this.smsTemplateId = smsTemplateId;
  }

  // 网页端登录
  public WebUsers login(String account, String password, char type) {
    List<WebUsers> allWebUser = adminManageMapper.findAllWebUser();
    for (WebUsers i : allWebUser) {
      if (account.equals(i.getAccount()) && password.equals(i.getPassword())
          && type == i.getAdminType()) {
        return i;
      }
    }
    return null;
  }

  // 忘记密码
  public String forgotPassword(String account, String phoneNumber) {
    List<WebUsers> webUsers = adminManageMapper.findAllWebUser();
    int flag = 0;
    for (WebUsers i : webUsers) {
      if (i.getAccount().equals(account) && i.getPhone().equals(phoneNumber)) {
        flag = 1;
        break;
      }
    }
    if (flag == 1) {
      return sendCode(phoneNumber);
    } else {
      return "账号与手机号不匹配!";
    }
  }

  public String sendCode(String phoneNumber) {
    if (isBlank(smsAppId) || isBlank(smsAppSecret)) {
      System.err.println("[SMS] 榛子云短信未配置：请设置 ZHENZI_SMS_APP_ID / ZHENZI_SMS_APP_SECRET；"
          + "本次未调用第三方接口。");
      return "短信服务未配置，请联系管理员";
    }
    String apiUrl = isBlank(smsApiUrl) ? "https://sms_developer.zhenzikj.com" : smsApiUrl;
    ZhenziSmsClient client = new ZhenziSmsClient(apiUrl, smsAppId, smsAppSecret);
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("number", phoneNumber);
    params.put("templateId", smsTemplateId);
    String[] templateParams = new String[2];
    Random rand = new Random();
    templateParams[0] = "";
    for (int i = 1; i <= 6; ++i) {
      templateParams[0] += "" + rand.nextInt(10);
    }
    templateParams[1] = "5";
    params.put("templateParams", templateParams);
    try {
      client.send(params);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return templateParams[0];
  }

  public boolean changePassword(String account, String newPassword) {
    adminManageMapper.changePassword(account, newPassword);
    return true;
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

}
