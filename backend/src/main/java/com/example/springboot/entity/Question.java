package com.example.springboot.entity;

public class Question {

  private int id;
  private String question;
  private String answer;
  private String questionStatus;

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

  public String getAnswer() {
    return answer;
  }

  public void setAnswer(String answer) {
    this.answer = answer;
  }

  public String getQuestionStatus() {
    return questionStatus;
  }

  public void setQuestionStatus(String questionStatus) {
    this.questionStatus = questionStatus;
  }

  public Question() {
  }

  public Question(int id, String question, String answer, String questionStatus) {
    this.id = id;
    this.question = question;
    this.answer = answer;
    this.questionStatus = questionStatus;
  }
}
