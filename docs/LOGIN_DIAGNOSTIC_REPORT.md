# 登录失败诊断报告（LOGIN_DIAGNOSTIC_REPORT）

> 日期：2026-10-26（第二次更新）
> 范围：只读排查，未修改任何业务源码、SQL、数据库记录或密码。
> 结论：**项目中不存在任何预置的管理员账号密码，也不存在知识实体/关系的种子数据**；当前登录失败存在两层原因：① 代码/脚本层面从未写入种子管理员；② 当前运行中的后端进程（IDE 启动）未注入 `DB_PASSWORD`，连不上本地 MySQL。

---

## 1. 排查结论（一句话）

**现有文件与代码无法提供任何可用的管理员登录账号；`web_user` 表是管理员账号的唯一存储位置，但建表脚本 `database/数据库.sql` 没有任何 INSERT 种子数据，项目文档也未记录初始账号。** 因此“不知道登录账号密码”并非使用者遗漏，而是项目本身就没有初始化管理员账号。

---

## 2. 逐项排查结果

### 2.1 数据库脚本（`database/数据库.sql`）

- 精确统计：**11 个 `CREATE TABLE`，0 个 `INSERT` / `INSERT INTO` / `VALUES` 语句**。脚本是纯 DDL（只建表、不写数据）。
- 11 张表：`chatting`、`entity`、`entity_relation`、`feedback_users`、`log`、`question`、`question_template`、`search_time`、`user_collectible`、`web_user`、`wechat_users`。
- 因此脚本**不包含**以下任何种子数据：
  - ❌ 初始管理员账号（`web_user`）
  - ❌ 普通用户（`wechat_users`）
  - ❌ 知识实体（`entity`）与关系（`entity_relation`）
  - ❌ 问题（`question`）与问题模板（`question_template`）
- 管理员账号存储位置：`web_user` 表（字段含 `account`、`password`、`adminType` 等），**不是** `wechat_users`（后者是微信小程序端普通用户）。
- `web_user` 表结构关键字段：
  - `account` varchar(20) 主键
  - `password` varchar(20)（**明文存储，非哈希**）
  - `adminType` char(1)（`0`=普通管理员，`1`=超级管理员）

> 补充：知识实体/关系在系统中的**权威存储是 Neo4j 图数据库**（`Neo4jService` 读写，`matchEntity` 等 Cypher 操作）；MySQL 里的 `entity`/`entity_relation` 表是另一套存储。两者在脚本中都无种子数据，需分别初始化（见第 4 节）。

### 2.2 登录相关后端代码

- 登录接口：`WebLoginController` → `POST /web_login`，参数为 `account`、`password`、`type`（char，`0`/`1`）。
- 验证逻辑：`WebLoginService.login()` 调用 `AdminManageMapper.findAllWebUser()` 遍历 `web_user` 表**所有记录**，进行**明文密码**逐条比对：
  ```java
  if (account.equals(i.getAccount()) && password.equals(i.getPassword())
      && type == i.getAdminType()) { return i; }
  ```
- 数据访问：`AdminManageMapper.xml` 中 `findAllWebUser` = `SELECT * FROM web_user`。
- 关键含义：**登录是否成功完全取决于 `web_user` 表中是否存在匹配账号**。若表为空，则任何账号都登录失败（返回 `null`，前端提示“登录失败”）。

### 2.3 项目文档与 README

- `README.md`、`PROJECT_STRUCTURE.md`、`MIGRATION_REPORT.md`、`LOCALIZATION_FIX_REPORT.md`、`MODERNIZATION_REPORT.md`：**均未记录初始管理员账号或密码**。
- `REMOTE_CONNECTION_AUDIT.md` 明确记录了与本结论一致的证据：
  - 第 181 行：“该 SQL **没有 `INSERT` 种子数据**，因此不能据此认为 `web_user` 中已有可登录管理员。”
  - 第 457 行（风险清单）：“`web_user` 表无种子管理员 → 网页端登录查不到账号，登录失败”。
- 文档中唯一出现过的“演示账号”是前端 `store.js` / `global_data.js` 里的 `test` / `test`（`password: 'test'`），这是**前端演示/占位数据，并非真实管理员账号**，且 Phase 1E 已将其置空。**不能把它当作管理员账号使用。**

### 2.4 前端登录页面与请求

- 前端登录页 `web/src/components/page/Login.vue` 发送：
  ```js
  this.$axios.post('/api/web_login', {
      account: param.username,
      password: param.password,
      type: radio        // '1' 默认（超级管理员）
  })
  ```
- 与后端 `POST /web_login` 的 `account`/`password`/`type` 参数名、结构**一致**；`/api` 前缀由 Vite 代理（`vite.config.js`）重写后转发到 `http://127.0.0.1:8081`，路径重写正确。
- 结论：**前后端接口路径、参数名、响应结构是一致的，不存在接口不匹配问题。**

### 2.5 当前登录请求的实际 HTTP 行为

对运行中的后端（`127.0.0.1:8081`，PID 26508）做只读探测：

- `POST /web_login`（占位账号）：返回 **HTTP 500**，响应体为：
  ```
  MyBatisSystemException ... CannotGetJdbcConnectionException:
  Failed to obtain JDBC Connection
  ```
- `POST /count_web_user`：同样返回 **HTTP 500**，同样的 JDBC 连接失败。

**失败类型判定**：当前失败属于 **MySQL 连接失败**，而非“账号密码错误 / 账号不存在 / 接口异常”。

**后端进程启动方式确认**（本轮核实）：PID 26508 的启动命令为
```
D:\CodeTools\Java\jdk-21.0.11\bin\java.exe -cp @...spring-boot-....argfile com.example.springboot.ExampleApplication
```
这是 **IDE / Spring Boot 以 classpath 方式运行 `target/classes`**（非 `java -jar`）。其连接失败根因是：该进程启动时**未注入 `DB_PASSWORD` 环境变量**（或注入了无效值），HikariCP 无法通过认证取得 JDBC 连接。应用本身（Spring Boot 3.5.16 + Java 21）启动正常、Tomcat 绑定 8081 正常，仅数据源不可用。

> 注意：即使修好 MySQL 连接，由于 `web_user` 表无种子数据，登录仍会因“查不到账号”而失败（返回 `null`，HTTP 200 但前端提示“登录失败”）。这是第二层、也是更根本的问题。

### 2.6 本地数据库只读检查（未执行的原因）

- 本地 MySQL 正在运行（`127.0.0.1:3306`，PID 7932），有 `mysql` 客户端（`D:/CodeTools/MySQL/MySQL Server 8.0/bin/mysql.exe`，8.0.46）。
- 但本轮环境里 `DB_PASSWORD` 未设置，且项目内**不存在**任何记录本地 MySQL 密码的文件（无 `.env`、无 `application-local.yml`、无 `.idea` 运行配置、无启动脚本；全项目仅 1 个 SQL 文件 `database/数据库.sql`，且为纯 DDL）。
- 按本次约束“不猜测账号密码”，**未尝试任何密码连接本地 MySQL，未执行任何数据库查询**。
- 因此本地 `smartques` 库各表“记录数”本次**未由我实测**。用户在 Navicat 中看到“表都是空的”，与脚本无种子数据一致；但表的空/非空最终应以数据库实测为准（需用户提供连接凭据后核对）。

### 2.7 连接配置核对（application.yml vs Navicat）

- `backend/src/main/resources/application.yml` 与 `backend/target/classes/application.yml` **完全一致**，均指向：
  - `url: jdbc:mysql://127.0.0.1:3306/smartques`（库名 `smartques`）
  - `username: ${DB_USER:root}`（默认 root）
  - `password: ${DB_PASSWORD}`（环境变量，无默认值）
- 与 Navicat 中查看的本地 `smartques` 库**一致**——即**不存在“连错库/连错地址”问题**。配置目标正确，缺的只是运行时 `DB_PASSWORD`。

---

## 3. 根因总结与情况判定

**情况判定：属于“只有表结构 + 初始化数据未导入”**，而非“连接错误数据库”。

| 层次 | 问题 | 证据 |
| --- | --- | --- |
| ① 根本原因 | 项目无管理员种子账号，也无任何业务种子数据 | `database/数据库.sql`：11 建表 + 0 INSERT |
| ② 直接原因（当前） | 运行中的后端进程连不上 MySQL | HTTP 500 `CannotGetJdbcConnectionException`；进程为 IDE classpath 启动，未注入 `DB_PASSWORD` |
| ③ 配置缺失 | 本地 MySQL 密码未以环境变量提供 | `DB_PASSWORD` 未设置，项目内无任何记录密码的文件 |
| ④ 连接目标 | 连接目标正确（`127.0.0.1:3306/smartques`），无连错库 | `application.yml` 与 `target/classes` 一致，与 Navicat 库名一致 |

---

## 4. 建议修复步骤（待确认后执行）

### 4.1 让后端正确连接本地 MySQL（前置）

1. 确认本地 `smartques` 库存在，以及 `root` 账号密码；
2. 以正确密码启动后端（IDE 运行时需在运行配置的 Environment Variables 里加 `DB_PASSWORD`，或用命令行）：
   ```bash
   DB_USER=root DB_PASSWORD=<本地真实密码> NEO4J_PASSWORD=<本地Neo4j密码> \
     java -jar backend/target/springboot-0.0.1-SNAPSHOT.jar --server.port=8081
   ```
   （注意 WorkBuddy 环境可能注入 `SERVER__PORT`，需显式 `--server.port=8081`）

### 4.2 初始化管理员账号（核心，需用户确认）

由于系统无任何可登录账号，需写入一个初始管理员。基于当前**明文比对**的密码验证逻辑，安全、最小范围的方案：

- **方案 A（最小改动，遵循现有逻辑）**：仅插入一条超级管理员记录到 `web_user`（`adminType='1'`）。因系统当前是明文存储+明文比对，密码只能按明文写入（与现有 `addWebUser` 行为一致）；账号名与密码**由用户指定**，我不会编造或猜测。
- **方案 B（安全增强，推荐但改动较大）**：在插入管理员前，先将 `WebLoginService.login` 与 `AdminManageService.addWebUser` 的密码处理改为哈希+加盐，再插入哈希后的管理员。这是更安全的长期方案，但属于代码改动，超出本轮“只读”范围，需另行立项。

无论哪种方案，都需用户明确指定账号名与密码；**我不会自行编造、重置或猜测密码，也不会直接插入明文密码（除非用户明确确认按方案 A 执行）**。

### 4.3 知识库数据初始化（单独说明）

知识实体/关系在系统中分两处，均无种子数据，需分别处理：

1. **Neo4j 图数据（权威来源）**：实体/关系由 `Neo4jService` 读写（Cypher），图谱页面（`GraphView`）读取 `/api/get_entity`、`/api/get_relation`。当前 Neo4j 容器（3.3.9）若无数据，图谱为空。初始化方式：通过后端接口新增，或导出/导入 Cypher 数据（阶段 D 迁移时一并规划）。
2. **MySQL 的 `entity` / `entity_relation` 表**：脚本仅建表、无数据。若系统某些功能（如 CSV 导入 `CsvController`）会写入这两张表，则需通过导入功能或 SQL 补数据。
3. **`question` / `question_template`（问答内容）**：同样无种子数据，需通过管理端“问题管理/模板管理”录入，或导入。

> 说明：管理后台的“实体管理/关系管理”页面实际读写的是 **Neo4j**（通过 `EntityManageController`/`RelationManageController` → `Neo4jService`），MySQL 的 `entity`/`entity_relation` 表是否被活动代码使用需进一步核对（本阶段未逐条追踪）。

### 4.4 后续安全改进（非本轮范围，仅提示）

- `web_user.password` 目前明文存储、明文比对（`WebLoginService.login`），应改为哈希 + 加盐校验；
- 登录接口当前无鉴权/会话校验（返回整个 `WebUsers` 对象含 `password` 字段，存在信息泄露隐患，前端 `store.commit('setUserinfomation', response.data)` 会把密码存入前端内存）。

---

## 5. 证据清单（文件/行号）

| 证据 | 位置 |
| --- | --- |
| 建表脚本无 INSERT（精确 11 建表 + 0 INSERT） | `database/数据库.sql`（全文 171 行，仅 DDL） |
| `web_user` 表结构 | `database/数据库.sql:142-153` |
| 登录接口路径/参数 | `backend/.../controller/WebLoginController.java:25-36` |
| 明文比对登录逻辑 | `backend/.../service/WebLoginService.java:35-44` |
| `findAllWebUser` SQL | `backend/.../resources/mapper/AdminManageMapper.xml:43-45` |
| 新增管理员逻辑（明文直写） | `backend/.../service/AdminManageService.java:18-31` |
| 前端登录请求 | `web/src/components/page/Login.vue:70-75` |
| 文档确认“无种子管理员” | `docs/REMOTE_CONNECTION_AUDIT.md:181、457` |
| MySQL 密码仅环境变量 | `backend/src/main/resources/application.yml:11-12` |
| 连接目标与 Navicat 一致（无连错库） | `application.yml:9` 与 `target/classes/application.yml:9` |

---

## 6. 明确说明：能证明 / 不能证明

**能证明：**
- 项目代码、SQL 脚本、文档中均**不存在**预置的管理员账号密码，也不存在知识实体/关系/问题的种子数据；
- `database/数据库.sql` 精确为 11 建表 + 0 INSERT（纯 DDL）；
- 登录接口前后端契约一致（路径/参数/结构）；
- 当前登录失败的直接原因是后端进程的 MySQL 连接失败（`DB_PASSWORD` 未注入）；
- 管理员账号的唯一存储位置是 `web_user` 表，密码为明文比对、无哈希；
- 后端连接目标正确（`127.0.0.1:3306/smartques`），与 Navicat 库名一致，**非连错库**。

**不能证明（本阶段未做）：**
- 本地 `smartques` 库各表当前的真实记录数（因无 MySQL 密码，未连接实测；Navicat 中“表为空”是用户观察，与脚本无种子数据一致，但最终以数据库实测为准）；
- 是否存在用户此前手动导入的账号或数据。

---

## 7. 下一步

等待用户确认：
1. 是否提供本地 MySQL 的连接密码（用于只读核对 `web_user` 及 `entity` 等表实际记录数）？
2. 是否授权初始化管理员账号？若授权，请明确：账号名、密码，以及采用方案 A（明文，最小改动）还是方案 B（先改哈希再插入，改动较大）。
3. 知识库数据（Neo4j 实体/关系、MySQL 问答内容）是否也需要初始化/导入？如需，请说明数据来源。

在此之前，本轮不执行任何写入或密码相关操作。
