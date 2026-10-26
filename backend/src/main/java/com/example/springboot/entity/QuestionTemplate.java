package com.example.springboot.entity;

public class QuestionTemplate {

  private int id;
  private String questionTemplate;
  private String preciseQuestion;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getQuestionTemplate() {
    return questionTemplate;
  }

  public void setQuestionTemplate(String questionTemplate) {
    this.questionTemplate = questionTemplate;
  }

  public String getPreciseQuestion() {
    return preciseQuestion;
  }

  public void setPreciseQuestion(String preciseQuestion) {
    this.preciseQuestion = preciseQuestion;
  }

  public QuestionTemplate() {
  }

  public QuestionTemplate(int id, String questionTemplate, String preciseQuestion) {
    this.id = id;
    this.questionTemplate = questionTemplate;
    this.preciseQuestion = preciseQuestion;
  }
}
