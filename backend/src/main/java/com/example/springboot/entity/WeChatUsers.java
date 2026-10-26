package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;

public class WeChatUsers {

  private String weChatName;
  private String phone;
  private String userStatus;
  private Date registerTime;
  private String profilePicture;

  // 密码不回传给前端，避免泄露。
  @JsonIgnore
  private String password;

  private String cause;

  public String getWeChatName() {
    return weChatName;
  }

  public void setWeChatName(String weChatName) {
    this.weChatName = weChatName;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getUserStatus() {
    return userStatus;
  }

  public void setUserStatus(String userStatus) {
    this.userStatus = userStatus;
  }

  public Date getRegisterTime() {
    return registerTime;
  }

  public void setRegisterTime(Date registerTime) {
    this.registerTime = registerTime;
  }

  public String getProfilePicture() {
    return profilePicture;
  }

  public void setProfilePicture(String profilePicture) {
    this.profilePicture = profilePicture;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getCause() {
    return cause;
  }

  public void setCause(String cause) {
    this.cause = cause;
  }

  public WeChatUsers() {
  }

  public WeChatUsers(String weChatName, String phone, String userStatus,
      Date registerTime, String profilePicture, String password, String cause) {
    this.weChatName = weChatName;
    this.phone = phone;
    this.userStatus = userStatus;
    this.registerTime = registerTime;
    this.profilePicture = profilePicture;
    this.password = password;
    this.cause = cause;
  }
}
