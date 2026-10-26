package com.example.springboot.entity;

import java.util.Date;

public class UserCollectible {

  private String phone;
  private String question;
  private String reply;
  private String collectDate;

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public String getReply() {
    return reply;
  }

  public void setReply(String reply) {
    this.reply = reply;
  }

  public String getCollectDate() {
    return collectDate;
  }

  public void setCollectDate(String collectDate) {
    this.collectDate = collectDate;
  }

  public UserCollectible() {
  }

  public UserCollectible(String phone, String question, String reply, String collectDate) {
    this.phone = phone;
    this.question = question;
    this.reply = reply;
    this.collectDate = collectDate;
  }
}
