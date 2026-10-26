package com.example.springboot.entity;

public class Entity {

  private int id;
  private String label;
  private String name;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Entity() {
  }

  public Entity(int id, String label, String name) {
    this.id = id;
    this.label = label;
    this.name = name;
  }
}
