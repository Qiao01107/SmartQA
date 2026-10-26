# 项目结构与架构说明（PROJECT_STRUCTURE）

> 本文所有结论均来自整理后仓库中的实际源码与配置文件，未凭空推测；
> 但**未实际启动/联调验证**，运行期行为以实际部署为准。

---

## 1. 总体架构

```text
┌─────────────────────┐   HTTP(S) / 可配置基地址   ┌──────────────────────────┐
│   微信小程序         │ ───────────────────────▶ │                          │
│  miniprogram/        │                          │   Spring Boot 后端        │
└─────────────────────┘                          │   backend/ (端口 8081)    │
                                                 │                          │
┌─────────────────────┐   /api (dev 代理 8081)    │  Controller → Service     │
│   网页管理后台       │ ───────────────────────▶ │      → Mapper / Neo4j     │
│  web/ (Vue2, 端口8080) │                          │                          │
└─────────────────────┘                          └───────┬──────────┬───────┘
                                                         │          │
                                                  ┌──────▼───┐  ┌───▼──────┐
                                                  │  MySQL   │  │  Neo4j   │
                                                  │ smartques│  │  图谱     │
                                                  └──────────┘  └──────────┘
                                                         ▲
                                                         │ HanLP 分词/模糊匹配
                                                    ┌────┴─────┐
                                                    │  HanLP   │
                                                    │ data 目录 │
                                                    └──────────┘
```

三端职责清晰：小程序与网页都通过后端 REST 接口访问数据；后端同时访问 MySQL（业务表）与 Neo4j（知识图谱），并用 HanLP 做中文处理。

---

## 2. 模块职责

### 2.1 `backend/`（后端）

- **controller/**：对外 REST 接口，按业务域拆分；
- **service/**：业务逻辑，其中：
  - `Neo4jService`：知识图谱实体/关系的增删改查（Cypher 查询）；
  - `WeChatReplyService`：小程序智能问答核心（HanLP 分词 + 模糊匹配 + Neo4j 查询）；
  - `CsvService`：CSV 数据导入；
  - 其余为各业务 Service；
- **dao/**：MyBatis Mapper 接口，与 `resources/mapper/*.xml` 对应；
- **entity/**：数据库实体（与 MySQL 表一一对应）；
- **tools/**：`EntityAndRelation`、`HotQuestion` 等工具类；
- **resources/**：`application.yml`（数据源）、`hanlp.properties`（HanLP 配置）、`mapper/*.xml`（SQL）。

### 2.2 `web/`（网页前端）

- **src/components/page/**：业务页面组件（管理后台各功能页）；
- **src/components/common/**：公共组件（含 i18n、指令等）；
- **src/router/index.js**：路由与页面映射；
- **src/api/**：接口封装（`global_data.js` 定义 `baseURL = '/api'`）；
- **src/store.js**：Vuex 状态（含 `token`、`userInfo`）；
- **src/utils/**：工具函数，含 Neo4j 图可视化相关（`neovis.js`、`neovis-without-dependencies.js`）；
- **src/main.js**：入口，注册 Element-UI、Vuex、路由守卫；
- **vue.config.js**：开发服务器端口 8080，`/api` 代理到本地后端。

### 2.3 `miniprogram/`（小程序）

- **pages/**：页面目录，`app.json` 注册了 11 个页面；
- **icons/**：图标资源；
- **utils/**：工具函数；
- **app.js / app.json / app.wxss**：全局入口与样式；
- **project.config.json / sitemap.json**：微信开发者工具项目配置。

---

## 3. 主要目录与关键文件

| 路径 | 作用 |
| --- | --- |
| `backend/pom.xml` | Maven 依赖（Spring Boot 2.5.6、MyBatis、MySQL、Neo4j、HanLP 等） |
| `backend/src/main/resources/application.yml` | 服务端口 8081、MySQL 数据源 |
| `backend/src/main/resources/hanlp.properties` | HanLP 数据根目录与词典配置 |
| `backend/src/main/resources/mapper/*.xml` | 14 个 MyBatis SQL 映射文件 |
| `backend/src/main/java/.../controller/` | 19 个控制器（管理端 + 小程序端 + 辅助） |
| `backend/src/main/java/.../service/Neo4jService.java` | Neo4j 图数据库访问 |
| `backend/src/main/java/.../service/WeChatReplyService.java` | 智能问答核心逻辑 |
| `web/src/main.js` | Vue 入口、路由守卫 |
| `web/src/router/index.js` | 前端路由表 |
| `web/src/api/global_data.js` | `baseURL='/api'` |
| `web/vue.config.js` | 端口 8080、代理目标（本地 8081） |
| `miniprogram/app.json` | 页面注册、tabBar、导航栏 |
| `miniprogram/pages/home/home.js` | 小程序核心问答页逻辑 |
| `database/数据库.sql` | MySQL 建表脚本（11 张表） |

### 数据库表（来自 `database/数据库.sql`）

`chatting`、`entity`、`entity_relation`、`feedback_users`、`log`、`question`、`question_template`、`search_time`、`user_collectible`、`web_user`、`wechat_users`（库名 `smartques`）。

---

## 4. 前后端调用关系

### 网页前端 → 后端

- 前端所有接口以 `/api` 为前缀（`src/api/global_data.js`、`src/store.js`）；
- 开发环境由 `web/vue.config.js` 的 `devServer.proxy` 把 `/api` 代理到 `http://127.0.0.1:8081`（并 `pathRewrite` 去掉 `/api`）；
- 生产环境需由反向代理/网关将 `/api` 转发到后端。

### 小程序 → 后端

- 小程序通过 `miniprogram/utils/config.js` 统一解析 API 基地址；
- 开发者工具默认 `http://127.0.0.1:8081`，真机默认使用 `DEVICE_BASE_URL`（需改成电脑局域网 IP），发布地址 `PRODUCTION_BASE_URL` 留空待配置；
- 后端 Controller 使用根路径，因此小程序请求不再保留 `/api` 前缀。

### 后端内部

- `Controller` → `Service` → `dao`（MyBatis）读写 MySQL；
- `Service` → `Neo4jService` 读写 Neo4j；
- `WeChatReplyService` 额外调用 HanLP 做分词/模糊匹配。

---

## 5. MySQL 的使用位置

- **连接配置**：`backend/src/main/resources/application.yml`
  - `url: jdbc:mysql://127.0.0.1:3306/smartques?...`
  - `username: ${DB_USER:root}`、`password: ${DB_PASSWORD}`（环境变量，源码不再包含真实密码）
- **访问方式**：`dao/` 下的 Mapper 接口 + `resources/mapper/*.xml` 中的 SQL；
- **表结构**：`database/数据库.sql`（11 张表，未修改表结构）。

---

## 6. Neo4j 的使用位置

- **连接配置（环境变量，统一 Driver）**：
  - `backend/src/main/java/com/example/springboot/config/Neo4jConfig.java`：
    `NEO4J_URI`（默认 `bolt://127.0.0.1:7687`）、`NEO4J_USER`（默认 `neo4j`）、`NEO4J_PASSWORD`（必填）；
  - `Neo4jService` 通过构造器注入唯一 `Driver`，每个方法使用 try-with-resources Session；
  - `WeChatReplyService` 复用同一个 `Driver`，不再在字段初始化时硬编码连接。
- **用途**：存储知识图谱的实体（Entity）与关系（EntityRelation），提供匹配、新增、删除、查询等 Cypher 操作；
- **前端可视化**：`web/src/components/common/GraphView.vue` + `web/src/utils/graphData.js`，使用 vis-network 渲染后端实体/关系接口返回的数据；浏览器不再直连 Bolt。

---

## 7. HanLP 的使用位置

- **依赖**：`backend/pom.xml` 中 `com.hankcs:hanlp:portable-1.7.2`；
- **配置**：`backend/src/main/resources/hanlp.properties`，其中
  `root=./hanlp-data`（相对路径，Windows 兼容；可用 `HANLP_ROOT` 环境变量覆盖）；
- **调用**：
  - `backend/src/main/java/com/example/springboot/service/WeChatReplyService.java` 使用 `NLPTokenizer` 分词；
  - `fuzzyWords.txt` 通过 `HANLP_FUZZY_WORDS`（默认 `hanlp-data/fuzzyWords.txt`）读取，缺失时跳过模糊词替换并输出明确警告；
- **数据准备**：见 `backend/hanlp-data/README.md`，项目未附带完整词典数据，也未伪造数据。

---

## 8. 配置文件位置

| 配置 | 文件 |
| --- | --- |
| 后端端口 / MySQL | `backend/src/main/resources/application.yml` |
| MyBatis 映射位置 | `application.yml` 中 `mapper-locations: classpath:mapper/*.xml` |
| HanLP | `backend/src/main/resources/hanlp.properties` |
| 前端开发代理 / 端口 | `web/vue.config.js` |
| 前端接口前缀 | `web/src/api/global_data.js`、`web/src/store.js`（`/api`） |
| 小程序页面/tabBar | `miniprogram/app.json` |
| 小程序项目配置 | `miniprogram/project.config.json` |

---

## 9. 本地化后的配置与剩余依赖

> Phase 1E 已处理原作者远程地址和明文凭据；以下为当前状态。

| 位置 | 当前配置 | 说明 |
| --- | --- | --- |
| `backend/.../application.yml` | `127.0.0.1:3306/smartques`，`${DB_USER:root}` / `${DB_PASSWORD}` | 本地 MySQL，密码只来自环境变量 |
| `backend/.../config/Neo4jConfig.java` | `NEO4J_URI` / `NEO4J_USER` / `NEO4J_PASSWORD` | 默认 `bolt://127.0.0.1:7687`，缺密码启动失败 |
| `backend/.../Neo4jService.java` | 构造器注入唯一 `Driver` | 不再重复创建 Driver，Session 用后关闭 |
| `backend/.../WeChatReplyService.java` | 复用同一 `Driver`；`HANLP_FUZZY_WORDS` | 模糊词缺失时跳过替换并告警 |
| `backend/.../hanlp.properties` | `root=./hanlp-data` | 相对路径，`HANLP_ROOT` 可覆盖 |
| `backend/hanlp-data/` | 仅 README，无词典数据 | 需自行准备 HanLP 数据与 `fuzzyWords.txt` |
| `web/vue.config.js` | 端口 8080，代理 `http://127.0.0.1:8081` | 保留 `/api` → 空的重写 |
| `web/src/components/common/GraphView.vue` | vis-network 渲染后端数据 | 浏览器不再直连 Bolt，不保存密码 |
| `web/src/main.js` | `import axios from '../node_modules/axios'` | 仍依赖 `web/node_modules`，需 `npm install` |
| `miniprogram/utils/config.js` | 开发者工具/真机/生产基地址 | 7 个页面不再硬编码原作者域名 |

---

## 10. 未覆盖事项

- 未启动后端/MySQL/Neo4j/HanLP，接口是否可用未验证；
- 未执行前端构建与小程序编译（`web/node_modules` 不存在）；
- 未核对 `web/package-lock.json` 中依赖是否可安装（锁文件来自旧 npm 源，可能需换源）；
- 未准备 HanLP 完整词典与 `fuzzyWords.txt`，问答分词尚未实际验证；
- 未分析全部页面组件的业务细节（仅按目录与入口归类）。
