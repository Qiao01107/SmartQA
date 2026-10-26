package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;

public class WebUsers {

  private String name;
  private String account;
  private String phone;
  private String head;
  private String email;
  private Date registerTime;
  private char adminType;

  // 密码不回传给前端，避免泄露（见 @JsonIgnore）。
  @JsonIgnore
  private String password;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getHead() {
    return head;
  }

  public void setHead(String head) {
    this.head = head;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Date getRegisterTime() {
    return registerTime;
  }

  public void setRegisterTime(Date registerTime) {
    this.registerTime = registerTime;
  }

  public char getAdminType() {
    return adminType;
  }

  public void setAdminType(char adminType) {
    this.adminType = adminType;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public WebUsers() {
  }

  public WebUsers(String name, String account, String phone, String head,
      String email, Date registerTime, char adminType, String password) {
    this.name = name;
    this.account = account;
    this.phone = phone;
    this.head = head;
    this.email = email;
    this.registerTime = registerTime;
    this.adminType = adminType;
    this.password = password;
  }
}
