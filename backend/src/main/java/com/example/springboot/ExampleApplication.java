package com.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 说明：项目使用 neo4j-java-driver 5.x（org.neo4j.driver）原生驱动，
 * 不再引入 Spring Data Neo4j。连接由
 * com.example.springboot.config.Neo4jConfig 统一提供 Driver Bean。
 */
@SpringBootApplication
public class ExampleApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExampleApplication.class, args);
  }

}
