package com.example.springboot.tools;

public class EntityAndRelation {

  private int startEntityId;
  private String startEntityLabel;
  private String startEntityName;
  private int endEntityId;
  private String endEntityLabel;
  private String endEntityName;
  private String relationLabel;
  private String relationName;

  public EntityAndRelation() {
  }

  public EntityAndRelation(int startEntityId, String startEntityLabel, String startEntityName,
      int endEntityId, String endEntityLabel, String endEntityName,
      String relationLabel, String relationName) {
    this.startEntityId = startEntityId;
    this.startEntityLabel = startEntityLabel;
    this.startEntityName = startEntityName;
    this.endEntityId = endEntityId;
    this.endEntityLabel = endEntityLabel;
    this.endEntityName = endEntityName;
    this.relationLabel = relationLabel;
    this.relationName = relationName;
  }

  public int getStartEntityId() {
    return startEntityId;
  }

  public void setStartEntityId(int startEntityId) {
    this.startEntityId = startEntityId;
  }

  public int getEndEntityId() {
    return endEntityId;
  }

  public void setEndEntityId(int endEntityId) {
    this.endEntityId = endEntityId;
  }

  public String getStartEntityLabel() {
    return startEntityLabel;
  }

  public void setStartEntityLabel(String startEntityLabel) {
    this.startEntityLabel = startEntityLabel;
  }

  public String getStartEntityName() {
    return startEntityName;
  }

  public void setStartEntityName(String startEntityName) {
    this.startEntityName = startEntityName;
  }

  public String getEndEntityLabel() {
    return endEntityLabel;
  }

  public void setEndEntityLabel(String endEntityLabel) {
    this.endEntityLabel = endEntityLabel;
  }

  public String getEndEntityName() {
    return endEntityName;
  }

  public void setEndEntityName(String endEntityName) {
    this.endEntityName = endEntityName;
  }

  public String getRelationLabel() {
    return relationLabel;
  }

  public void setRelationLabel(String relationLabel) {
    this.relationLabel = relationLabel;
  }

  public String getRelationName() {
    return relationName;
  }

  public void setRelationName(String relationName) {
    this.relationName = relationName;
  }
}
