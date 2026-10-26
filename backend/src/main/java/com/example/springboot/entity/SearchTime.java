package com.example.springboot.entity;

import java.util.Date;

public class SearchTime {

  private int id;
  private String question;
  private String reply;

  private Date searchTime;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  public Date getSearchTime() {
    return searchTime;
  }

  public void setSearchTime(Date searchTime) {
    this.searchTime = searchTime;
  }

  public SearchTime() {
  }

  public SearchTime(int id, String question, String reply, Date searchTime) {
    this.id = id;
    this.question = question;
    this.reply = reply;
    this.searchTime = searchTime;
  }
}
