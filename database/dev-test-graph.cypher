// ============================================================
// SmartQA 开发测试图谱数据（Neo4j Cypher）
// 仅用于本地开发与联调测试，请勿导入生产环境。
// 使用方式：neo4j-shell / Cypher Shell / HTTP API 执行。
// ============================================================

// 清理旧的开发测试数据（仅删除本示例涉及的节点，避免误删业务数据）
MATCH (n) WHERE n.name IN ['张三', '李四', '教授', '副教授', '计算机学院', '软件工程'] DETACH DELETE n;

// 实体节点（label 与 name 用于图谱问答匹配）
CREATE (:Person {name: '张三'});
CREATE (:Person {name: '李四'});
CREATE (:Title {name: '教授'});
CREATE (:Title {name: '副教授'});
CREATE (:Department {name: '计算机学院'});
CREATE (:Major {name: '软件工程'});

// 关系（用于 /apart 问答链路：张三的职称是教授）
MATCH (a:Person {name: '张三'}), (b:Title {name: '教授'})
CREATE (a)-[:职称 {name: '职称'}]->(b);

MATCH (a:Person {name: '李四'}), (b:Title {name: '副教授'})
CREATE (a)-[:职称 {name: '职称'}]->(b);

MATCH (a:Person {name: '张三'}), (b:Department {name: '计算机学院'})
CREATE (a)-[:所属 {name: '所属'}]->(b);

MATCH (a:Department {name: '计算机学院'}), (b:Major {name: '软件工程'})
CREATE (a)-[:开设 {name: '开设'}]->(b);
