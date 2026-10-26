package com.example.springboot.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 本地 Neo4j 连接配置。
 *
 * <p>连接信息只从环境变量读取：
 * <ul>
 *   <li>NEO4J_URI，默认 bolt://127.0.0.1:7687</li>
 *   <li>NEO4J_USER，默认 neo4j</li>
 *   <li>NEO4J_PASSWORD，必须显式配置，不提供旧密码回退</li>
 * </ul>
 *
 * <p>整个应用共用一个 Driver，由 Spring 在容器关闭时调用 close() 释放连接池。
 */
@Configuration
public class Neo4jConfig {

  @Bean(destroyMethod = "close")
  public Driver neo4jDriver(
      @Value("${NEO4J_URI:bolt://127.0.0.1:7687}") String uri,
      @Value("${NEO4J_USER:neo4j}") String user,
      @Value("${NEO4J_PASSWORD:}") String password) {
    if (password == null || password.trim().isEmpty()) {
      throw new IllegalStateException(
          "Neo4j 连接未配置：请设置环境变量 NEO4J_PASSWORD"
              + "（可选 NEO4J_URI、NEO4J_USER）。未配置时不会尝试任何旧密码。");
    }
    if (uri == null || uri.trim().isEmpty()) {
      throw new IllegalStateException("Neo4j 连接未配置：NEO4J_URI 不能为空。");
    }
    String normalizedUri = uri.trim();
    System.out.println("[Neo4j] 使用本地/显式配置的连接：" + normalizedUri + "，用户：" + user);
    return GraphDatabase.driver(normalizedUri, AuthTokens.basic(user, password));
  }
}
