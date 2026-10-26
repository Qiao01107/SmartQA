package com.example.springboot.entity;

public class EntityRelation {

  private int startEntity;
  private int endEntity;
  private String label;
  private String relation;

  public int getStartEntity() {
    return startEntity;
  }

  public void setStartEntity(int startEntity) {
    this.startEntity = startEntity;
  }

  public int getEndEntity() {
    return endEntity;
  }

  public void setEndEntity(int endEntity) {
    this.endEntity = endEntity;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getRelation() {
    return relation;
  }

  public void setRelation(String relation) {
    this.relation = relation;
  }

  public EntityRelation() {
  }

  public EntityRelation(int startEntity, int endEntity, String label, String relation) {
    this.startEntity = startEntity;
    this.endEntity = endEntity;
    this.label = label;
    this.relation = relation;
  }
}
