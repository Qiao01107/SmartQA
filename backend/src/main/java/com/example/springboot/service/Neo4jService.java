package com.example.springboot.service;

import com.example.springboot.dao.EntityManageMapper;
import com.example.springboot.dao.RelationManageMapper;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Neo4j 图数据库访问。
 *
 * <p>重要：Neo4j 5.x 已弃用内部函数 {@code id(n)}（该 id 从 0 开始、删除后可复用、不稳定）。
 * 本类统一改用节点属性 {@code bid}（业务自增 id）作为 MySQL {@code entity} 表的主键，
 * 通过一个 {@code (:__Counter {name:'entity'})} 节点原子自增，保证与 MySQL 主键一致且不冲突。
 *
 * <p>连接由 {@link com.example.springboot.config.Neo4jConfig} 统一创建和关闭；
 * 本类不再硬编码地址/账号/密码。
 */
@Service
public class Neo4jService {

  private final Driver driver;

  @Autowired
  private EntityManageMapper entityManageMapper;

  @Autowired
  private RelationManageMapper relationManageMapper;

  public Neo4jService(Driver driver) {
    this.driver = driver;
  }

  /** 取得下一个业务 id（原子自增，从 1 开始）。 */
  private int nextBid() {
    try (Session session = driver.session()) {
      Result result = session.run(
          "MERGE (c:__Counter {name:'entity'}) "
              + "ON CREATE SET c.value = 1 "
              + "ON MATCH SET c.value = c.value + 1 "
              + "RETURN c.value AS value");
      return result.next().get("value").asInt();
    }
  }

  // 添加一个实体，返回该实体业务 id（bid）
  public int addEntity(String label, String name) {
    int bid = nextBid();
    try (Session session = driver.session()) {
      session.run("CREATE (n:`" + label + "` {name:'" + name + "', bid:" + bid + "})").consume();
    }
    return bid;
  }

  // 根据标签和名字查找实体，返回实体业务 id，若不存在返回 -1
  public int matchEntity(String label, String name) {
    try (Session session = driver.session()) {
      Result result = session.run(
          "MATCH (n:`" + label + "`) WHERE n.name='" + name + "' RETURN n.bid AS bid");
      if (result.hasNext()) {
        return result.next().get("bid").asInt();
      }
      return -1;
    }
  }

  // 提供一个实体业务 id，统计入度
  public int countInDegree(int id) {
    try (Session session = driver.session()) {
      Result result = session.run("MATCH ()-->(n) WHERE n.bid=" + id + " RETURN count(*) AS c");
      return result.next().get("c").asInt();
    }
  }

  // 提供一个实体业务 id，统计出度
  public int countOutDegree(int id) {
    try (Session session = driver.session()) {
      Result result = session.run("MATCH (n)-->() WHERE n.bid=" + id + " RETURN count(*) AS c");
      return result.next().get("c").asInt();
    }
  }

  // 提供一个实体业务 id，返回该实体上有多少条关系
  public int countRelation(int id) {
    return countInDegree(id) + countOutDegree(id);
  }

  // 提供一个实体业务 id，删除该实体
  public void deleteEntity(int id) {
    try (Session session = driver.session()) {
      session.run("MATCH (n) WHERE n.bid=" + id + " DETACH DELETE n").consume();
    }
  }

  // 提供一个实体业务 id、新标签和新名字，修改其标签和名字
  public void updateEntity(int id, String oldLabel, String newLabel, String newName) {
    try (Session session = driver.session()) {
      session.run("MATCH (n) WHERE n.bid=" + id + " REMOVE n:`" + oldLabel
          + "` SET n:`" + newLabel + "`, n.name = '" + newName + "'").consume();
    }
  }

  // 提供起始/结束实体业务 id、关系标签和关系名，添加关系
  public void addRelation(int startId, int endId, String label, String name) {
    try (Session session = driver.session()) {
      session.run("MATCH (n),(b) WHERE n.bid=" + startId + " AND b.bid=" + endId
          + " CREATE (n)-[r:`" + label + "` {name:'" + name + "'}]->(b)").consume();
    }
  }

  // 查找关系，若存在返回 1（存在标记），不存在返回 -1
  public int matchRelation(int startId, int endId, String label, String name) {
    try (Session session = driver.session()) {
      Result result = session.run(
          "MATCH (n)-[r:`" + label + "`]->(b) WHERE n.bid=" + startId + " AND b.bid=" + endId
              + " AND r.name='" + name + "' RETURN count(r) AS c");
      if (result.hasNext() && result.next().get("c").asInt() > 0) {
        return 1;
      }
      return -1;
    }
  }

  // 删除关系
  public void deleteRelation(int startId, int endId, String label, String name) {
    try (Session session = driver.session()) {
      session.run("MATCH (n)-[r:`" + label + "`]->(b) WHERE n.bid=" + startId + " AND b.bid=" + endId
          + " AND r.name='" + name + "' DELETE r").consume();
    }
  }

  // 修改关系标签和名字
  public void updateRelation(int startId, int endId, String label, String name) {
    try (Session session = driver.session()) {
      session.run("MATCH (n)-[r]->(b) WHERE n.bid=" + startId + " AND b.bid=" + endId
          + " SET r:`" + label + "`, r.name='" + name + "'").consume();
    }
  }

  // 删除该实体上的全部关系
  public void deleteAllRelation(int id) {
    try (Session session = driver.session()) {
      session.run("MATCH (n)-[r]->(b) WHERE n.bid=" + id + " DELETE r").consume();
      session.run("MATCH (b)-[r]->(n) WHERE n.bid=" + id + " DELETE r").consume();
    }
  }

  // 遍历所有实体，同步到 MySQL entity 表
  public void allEntity() {
    try (Session session = driver.session()) {
      Result result = session.run("MATCH (n) RETURN n.bid AS bid, labels(n) AS labels, n.name AS name");
      while (result.hasNext()) {
        Record record = result.next();
        int id = record.get("bid").asInt();
        String label = record.get("labels").asList().get(0).toString();
        String name = record.get("name").asString();
        entityManageMapper.addEntity(id, label, name);
      }
    }
  }

  // 遍历所有关系，同步到 MySQL entity_relation 表
  public void allRelation() {
    try (Session session = driver.session()) {
      Result result = session.run(
          "MATCH (n)-[r]->(n1) RETURN n.bid AS s, n1.bid AS e, type(r) AS t, r.name AS name");
      while (result.hasNext()) {
        Record record = result.next();
        int id1 = record.get("s").asInt();
        int id2 = record.get("e").asInt();
        String label = record.get("t").asString();
        String name = record.get("name").asString();
        relationManageMapper.addRelation(id1, id2, label, name);
      }
    }
  }

}
