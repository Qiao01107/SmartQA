package com.example.springboot.entity;

import java.util.Date;

public class FeedbackUsers {

  private String phone;
  private String opinion;
  private String status;
  private String question;
  private String feedbackTime;
  private String evaluation;

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getOpinion() {
    return opinion;
  }

  public void setOpinion(String opinion) {
    this.opinion = opinion;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public String getFeedbackTime() {
    return feedbackTime;
  }

  public void setFeedbackTime(String feedbackTime) {
    this.feedbackTime = feedbackTime;
  }

  public String getEvaluation() {
    return evaluation;
  }

  public void setEvaluation(String evaluation) {
    this.evaluation = evaluation;
  }

  public FeedbackUsers() {
  }

  public FeedbackUsers(String phone, String opinion, String status, String question,
      String feedbackTime, String evaluation) {
    this.phone = phone;
    this.opinion = opinion;
    this.status = status;
    this.question = question;
    this.feedbackTime = feedbackTime;
    this.evaluation = evaluation;
  }
}
