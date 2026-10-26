package com.example.springboot.entity;

import java.util.Date;

public class Chatting {

  private String phone;
  private String speaker;
  private String content;
  private String speakDate;

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getSpeaker() {
    return speaker;
  }

  public void setSpeaker(String speaker) {
    this.speaker = speaker;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getSpeakDate() {
    return speakDate;
  }

  public void setSpeakDate(String speakDate) {
    this.speakDate = speakDate;
  }

  public Chatting() {
  }

  public Chatting(String phone, String speaker, String content, String speakDate) {
    this.phone = phone;
    this.speaker = speaker;
    this.content = content;
    this.speakDate = speakDate;
  }
}
