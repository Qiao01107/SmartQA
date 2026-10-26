package com.example.springboot.entity;

import java.util.Date;

public class Log {

  private String account;
  private String name;
  private String operation;
  private Date operationTime;
  private String usedInterface;
  private String operationStatus;

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public Date getOperationTime() {
    return operationTime;
  }

  public void setOperationTime(Date operationTime) {
    this.operationTime = operationTime;
  }

  public String getUsedInterface() {
    return usedInterface;
  }

  public void setUsedInterface(String usedInterface) {
    this.usedInterface = usedInterface;
  }

  public String getOperationStatus() {
    return operationStatus;
  }

  public void setOperationStatus(String operationStatus) {
    this.operationStatus = operationStatus;
  }

  public Log() {
  }

  public Log(String account, String name, String operation, Date operationTime,
      String usedInterface, String operationStatus) {
    this.account = account;
    this.name = name;
    this.operation = operation;
    this.operationTime = operationTime;
    this.usedInterface = usedInterface;
    this.operationStatus = operationStatus;
  }
}
