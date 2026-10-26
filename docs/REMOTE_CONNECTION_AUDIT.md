# SmartQA Phase 1D：远程连接与本地化配置专项审计报告

> **状态说明（Phase 1E 后）**：本报告记录的是修复前的审计状态，其中的远程地址、行号和“未修改”描述已被 Phase 1E 修复覆盖。当前修复结果、残余风险和启动前置条件请以 `docs/LOCALIZATION_FIX_REPORT.md` 为准；本报告仅作为历史证据保留。

> 审计对象：`D:\School\3\up\软工\SmartQA`
> 审计日期：2026-10-22
> 审计性质：**只读静态审计**。未修改任何 Java、Vue、小程序、YAML、properties 或 XML 文件；未连接 MySQL / Neo4j；未访问任何远程服务器；未启动 Spring Boot、Vue 或小程序；未安装或升级依赖；未修改环境变量、ACL 或 Docker。
> 本报告中的路径均相对 `SmartQA/`；所有密码、密钥、Token 均已脱敏为 `<REDACTED>`。

---

## 1. 审计摘要

### 1.1 检查范围与文件数量

已排除 `target/`、`dist/`、`node_modules/`、`.git/` 等构建、缓存和版本库目录。实际扫描结果：

| 类别 | 数量 | 说明 |
| --- | ---: | --- |
| 项目文件总数 | 466 | 排除上述目录后的全部普通文件（不含本报告） |
| Java 源文件 | 68 | `backend/src/main/java`、`backend/src/test` |
| Vue 单文件组件 | 22 | `web/src/**/*.vue` |
| JavaScript 文件 | 28 | `web/`、`miniprogram/`、构建配置 |
| JSON 文件 | 16 | 小程序页面配置、`project.config.json`、`package.json` 等 |
| WXML 文件 | 11 | 小程序页面 |
| XML 文件 | 15 | MyBatis Mapper 与 `pom.xml` |
| YAML 文件 | 1 | `backend/src/main/resources/application.yml` |
| properties 文件 | 2 | `hanlp.properties`、Maven Wrapper 配置 |
| Markdown / 归档文档 / 图片等 | 其余 | `docs/` 下 268 个文档、归档与图片文件 |

> 源码/配置文本文件共约 **163 个**。此外单独核查了 `backend/target/classes/` 中的构建产物副本（共 2 个关键副本：`application.yml`、`hanlp.properties`），因为构建产物中的陈旧连接信息可能在 IDE 直接运行时生效。

### 1.2 关键发现数字

| 指标 | 数量 | 说明 |
| --- | ---: | --- |
| 文本中 `47.113.226.5` / `bulaqiu.top` 引用 | 62 行 / 17 个文件 | 含源码、配置、文档、SQL 注释 |
| **活动中（源码/配置内）的原作者服务器连接** | **45 处** | 后端 Neo4j 12、前端 Neo4j 3、Vue 代理 1、小程序 29 |
| 构建产物中的远程 MySQL 连接 | 1 处 | `backend/target/classes/application.yml:7` |
| 仅存在于文档/注释/历史文件中的引用 | 17 处 | 文档/历史 16 处 + 注释 1 处（`home.js:725`）；不作为当前生效配置，但存在凭据泄露与误导风险 |
| 硬编码凭据出现位置 | 21 处 / 6 类 | MySQL 1、Neo4j 后端 12、Neo4j 前端 3、讯飞 2、榛子云 2、构建产物 MySQL 1 |
| 前端库内置默认凭据 | 2 处 | Neovis 默认 `bolt://localhost:7687` / `neo4j` / `<DEFAULT>`，非真实密码 |
| 前端演示用硬编码账号密码 | 2 处 | `test` / `test`，非真实生产凭据 |
| 活动中的操作系统绝对路径 | 2 处 | 均为 Linux `/root/spring_java/...`，Windows 不可用 |
| `GraphDatabase.driver` 调用总数 | 14 处 | 后端 13 + `WeChatReplyService` 1 |
| 其中仍指向远程 Neo4j | 12 处 | 全部在 `Neo4jService.java` |
| 其中已指向本地 Neo4j | 2 处 | 均依赖 `NEO4J_PASSWORD`，当前环境未定义 |

> 统计口径：以上“62 行 / 17 个文件”基于**写入本报告之前**的项目文本文件；本报告自身也包含 `47.113.226.5`、`bulaqiu.top` 等关键词，后续回归检索时应排除 `docs/REMOTE_CONNECTION_AUDIT.md`，否则计数会被本报告放大。

### 1.3 风险分级

| 等级 | 定义 | 本次数量（按问题类别） |
| --- | --- | ---: |
| **P0** | 可能访问原作者服务器，或硬编码了真实凭据（是否仍有效未验证）；必须在任何联调前处理 | 7 类（46 处作者服务器连接 + 2 组第三方密钥） |
| **P1** | 会导致本地启动、登录、问答、图谱等核心链路失败，或造成敏感信息泄露 | 9 项 |
| **P2** | 不影响基本启动，但存在安全、可维护性、依赖或文档问题 | 10 项 |
| **P3** | 文档、注释、示例、未挂载组件等低风险问题 | 4 项 |

### 1.4 关键词命中情况（对应任务“重点搜索”）

| 搜索项 | 命中情况 | 说明 |
| --- | --- | --- |
| `47.113.226.5` | 28 行 | 源码活动连接 16 行（后端 12 + 前端 3 + 代理 1）+ 文档/历史引用 12 行 |
| `bulaqiu.top` | 34 行 / 10 个文件 | 29 处活动小程序 URL + 1 处注释 + 4 处文档引用 |
| 两者合计（并集） | 62 行 / 17 个文件 | 与 1.2 节一致 |
| `jdbc:mysql://` | 2 行 | `application.yml:8`（本地）、`target/classes/application.yml:7`（远程） |
| `bolt://` | 17 行 | 后端 14 行（12 远程 + 2 本地）+ 前端 3 行 |
| `neo4j://` | 0 | 未使用该协议 |
| `http://` | 多处 | Vue 代理、Maven 仓库、MyBatis DTD、第三方库 license、旧部署文档等；已分类 |
| `https://` | 多处 | 小程序 29 处、讯飞、榛子云、CDN、文档链接等；已分类 |
| `GraphDatabase.driver` | 14 行 | `Neo4jService` 13 + `WeChatReplyService` 1 |
| `AuthTokens.basic` | 14 行 | 与上述 14 个驱动调用一一对应 |
| `DriverManager.getConnection` | 0 | 项目使用 Spring 数据源 + MyBatis，没有手写 JDBC 连接 |
| `axios` | 多处 | `request.js`、`main.js`、各组件 `this.$axios`；基址为相对 `/api`，远程化发生在 Vue 代理 |
| `wx.request` | 29 处活动 | 7 个小程序页面，全部指向 `https://www.bulaqiu.top/api/...` |
| `fetch` | 0 | 未使用浏览器原生 fetch |
| `OkHttpClient` | 0 个实际客户端 | `UrlService` 仅用 `okhttp3.HttpUrl` 拼接签名 URL，未发起 HTTP 请求 |
| `RestTemplate` | 0 | 未使用 |
| `root/spring_java` | 2 行 | `hanlp.properties:3`、`WeChatReplyService.java:87` |
| `fuzzyWords.txt` | 1 行 | `WeChatReplyService.java:87`；项目内不存在该文件 |
| 硬编码用户名/密码/Token/API Key | 21 处 / 6 类 | 详见第 2 节与第 11 节；另有 2 处 Neovis 库内置默认值 |

### 1.5 一句话结论

**MySQL 源码配置已指向本地，但 `target/classes` 仍残留远程连接；Neo4j 远未完成本地化，14 个驱动调用中 12 个仍硬编码远程地址与明文密码；Vue 仍有远程代理和 3 处浏览器直连远程 Bolt；小程序 29 处仍请求 `www.bulaqiu.top`；HanLP 仍依赖不存在的 Linux 绝对路径。当前不具备安全启动 Spring Boot 的条件。**

---

## 2. 问题明细

> 说明：
> - “当前连接目标”中，IP、域名、端口保留以便定位；账号、密码、密钥统一脱敏。
> - “是否需要修改”中的“是”表示下一阶段应处理；“建议”表示非阻塞优化。
> - `target/` 本不在源码审计范围，但其内容会直接影响“现在启动”的安全性，因此单独列出。

| 文件路径 | 行号 | 配置类型 | 当前连接目标（脱敏） | 风险等级 | 是否需要修改 | 建议方案 |
| --- | --- | --- | --- | --- | --- | --- |
| `backend/src/main/resources/application.yml` | 8-10 | MySQL 数据源 | `jdbc:mysql://127.0.0.1:3306/smartques`；`root` / `<REDACTED>` | P1 | 是 | 已指向本地；密码改为环境变量或 `application-local.yml`，禁止明文提交 |
| `backend/src/main/java/com/example/springboot/service/Neo4jService.java` | 38-39（`matchEntity`） | Neo4j 后端 | `bolt://47.113.226.5:7687`；`neo4j` / `<REDACTED>` | P0 | 是 | 改为统一的本地 `Driver` Bean，地址/账号/密码全部外部化 |
| `Neo4jService.java` | 52-53（`countInDegree`） | Neo4j 后端 | `bolt://47.113.226.5:7687`；`neo4j` / `<REDACTED>` | P0 | 是 | 同上，删除重复驱动创建 |
| `Neo4jService.java` | 64-65（`countOutDegree`） | Neo4j 后端 | `bolt://47.113.226.5:7687`；`neo4j` / `<REDACTED>` | P0 | 是 | 同上 |
| `Neo4jService.java` | 85-86（`deleteEntity`） | Neo4j 后端 | `bolt://47.113.226.5:7687`；`neo4j` / `<REDACTED>` | P0 | 是 | 同上 |
| `Neo4jService.java` | 94-95（`updateEntity`） | Neo4j 后端 | `bolt://47.113.226.5:7687`；`neo4j` / `<REDACTED>` | P0 | 是 | 同上 |
| `Neo4jService.java` | 105-106（`addRelation`） | Neo4j 后端 | `bolt://47.113.226.5:7687`；`neo4j` / `<REDACTED>` | P0 | 是 | 同上 |
| `Neo4jService.java` | 117-118（`matchRelation`） | Neo4j 后端 | `bolt://47.113.226.5:7687`；`neo4j` / `<REDACTED>` | P0 | 是 | 同上 |
| `Neo4jService.java` | 133-134（`deleteRelation`） | Neo4j 后端 | `bolt://47.113.226.5:7687`；`neo4j` / `<REDACTED>` | P0 | 是 | 同上 |
| `Neo4jService.java` | 144-145（`updateRelation`） | Neo4j 后端 | `bolt://47.113.226.5:7687`；`neo4j` / `<REDACTED>` | P0 | 是 | 同上 |
| `Neo4jService.java` | 155-156（`deleteAllRelation`） | Neo4j 后端 | `bolt://47.113.226.5:7687`；`neo4j` / `<REDACTED>` | P0 | 是 | 同上 |
| `Neo4jService.java` | 167-168（`allEntity`） | Neo4j 后端 | `bolt://47.113.226.5:7687`；`neo4j` / `<REDACTED>` | P0 | 是 | 同上 |
| `Neo4jService.java` | 189-190（`allRelation`） | Neo4j 后端 | `bolt://47.113.226.5:7687`；`neo4j` / `<REDACTED>` | P0 | 是 | 同上；该方法是 `/test` 的远程触发点 |
| `Neo4jService.java` | 25-29（`addEntity`） | Neo4j 后端 | `bolt://127.0.0.1:7687`；`neo4j` / `${NEO4J_PASSWORD}`（未定义） | P1 | 是 | 已本地化但环境变量缺失；补充环境变量或配置项，并做空值校验 |
| `backend/src/main/java/com/example/springboot/service/WeChatReplyService.java` | 32-37 | Neo4j 后端（字段初始化） | `bolt://127.0.0.1:7687`；`neo4j` / `${NEO4J_PASSWORD}`（未定义） | P1 | 是 | 不要在字段初始化时建连；改为构造注入的共享 Bean，并在启动时校验配置 |
| `web/src/components/page/Neo4j.vue` | 40、122-124 | Neo4j 前端直连 | `bolt://47.113.226.5:7687`；`neo4j` / `<REDACTED>` | P0 | 是 | 禁止浏览器直连图数据库；改由后端代理/接口查询，至少改为本地地址并移除前端密码 |
| `web/src/components/page/EntityForm.vue` | 61、374-376 | Neo4j 前端直连 | `bolt://47.113.226.5:7687`；`neo4j` / `<REDACTED>` | P0 | 是 | 同上 |
| `web/src/components/page/RelationForm.vue` | 56、407-409 | Neo4j 前端直连 | `bolt://47.113.226.5:7687`；`neo4j` / `<REDACTED>` | P0 | 是 | 同上 |
| `web/src/utils/neovis.js`、`neovis-without-dependencies.js` | 78 / 1 | 前端库默认 Neo4j 配置 | `bolt://localhost:7687`；`neo4j` / `<DEFAULT>`（库内置默认值，非真实密码） | P2 | 建议 | 移除/覆盖默认凭据；不要依赖库默认认证 |
| `web/vue.config.js` | 10 | Vue 开发代理 | `http://47.113.226.5:8081` | P0 | 是 | 改为 `http://127.0.0.1:8081`，并考虑用环境变量读取 |
| `web/vue.config.js` | 7 | Vue 开发端口 | `port: 80` | P1 | 是 | 改为 `8080` 或其他非特权端口，避免 Windows 管理员权限问题 |
| `miniprogram/pages/home/home.js` | 122、137、198、245、295、332、380、417、464、500、547、585、614、647、699、724、870、1007、1020、1073（20 处活动） | 小程序 API | `https://www.bulaqiu.top/api/...` | P0 | 是 | 抽取 `BASE_URL`，统一替换为本地后端；详见第 6 节 |
| `miniprogram/pages/history/history.js` | 18、41、68 | 小程序 API | `https://www.bulaqiu.top/api/...` | P0 | 是 | 同上 |
| `miniprogram/pages/login/login.js` | 63 | 小程序 API | `https://www.bulaqiu.top/api/wechat_login` | P0 | 是 | 同上 |
| `miniprogram/pages/enroll/enroll.js` | 84 | 小程序 API | `https://www.bulaqiu.top/api/wechat_register` | P0 | 是 | 同上 |
| `miniprogram/pages/changepw/changepw.js` | 49 | 小程序 API | `https://www.bulaqiu.top/api/wechat_change_password` | P0 | 是 | 同上 |
| `miniprogram/pages/collection/collection.js` | 17 | 小程序 API | `https://www.bulaqiu.top/api/collectible` | P0 | 是 | 同上 |
| `miniprogram/pages/feedback/feedback.js` | 24、111 | 小程序 API | `https://www.bulaqiu.top/api/wechat_feedbackView`、`.../wechat_feedback` | P0 | 是 | 同上 |
| `backend/src/main/resources/hanlp.properties` | 3 | HanLP 数据根目录 | `root=/root/spring_java/data-for-1.7.5`（Linux 绝对路径） | P1 | 是 | 改为 Windows 可用路径或相对路径；数据目录需随项目部署 |
| `backend/src/main/java/com/example/springboot/service/WeChatReplyService.java` | 87 | 本地文件路径 | `/root/spring_java/data-for-1.7.5/fuzzyWords.txt` | P1 | 是 | 改为 classpath 资源或可配置相对路径；文件缺失时返回空列表而不是 NPE |
| `backend/src/main/java/com/example/springboot/service/UrlService.java` | 20-22 | 第三方 API + 硬编码密钥 | `https://iat-api.xfyun.cn/v2/iat`；`apiKey` / `apiSecret` = `<REDACTED>` | P0 | 是 | 讯飞接口本身正常；密钥必须移到环境变量并**立即轮换**（已在源码中暴露） |
| `backend/src/main/java/com/example/springboot/service/WebLoginService.java` | 49-52 | 第三方 API + 硬编码密钥 | `https://sms_developer.zhenzikj.com`；`appId` / `appSecret` = `<REDACTED>` | P0 | 是 | 榛子云短信本身正常；密钥移到环境变量并轮换；本地可禁用短信或使用测试桩 |
| `backend/target/classes/application.yml` | 7-9 | 构建产物中的 MySQL | `jdbc:mysql://47.113.226.5:3306/smartques`；`zzt` / `<REDACTED>` | P0 | 是 | 删除 `target/` 或在下一阶段 `mvn clean` 后重新构建；确保运行使用 `src/main/resources` 的新配置 |
| `web/src/store.js`、`web/src/api/global_data.js` | `store.js:16`、`global_data.js:11` | 前端演示凭据 | `password: 'test'`（演示数据） | P2 | 建议 | 删除演示账号密码，改为空对象或从登录结果填充 |
| `web/public/index.html` | 7 | 第三方 CDN | `//at.alicdn.com/t/font_830376_qzecyukz0s.css` | P2 | 建议 | 仅图标字体，可本地化以支持离线；不影响业务接口 |
| `web/src/components/page/Upload.vue` | 18 | 示例上传地址 | `http://jsonplaceholder.typicode.com/api/posts/` | P3 | 可选 | 该组件未被路由挂载，属于示例代码；可删除或保留 |
| `backend/pom.xml` | 128 | Maven 仓库（构建期） | `http://m2.neo4j.org/content/repositories/releases/` | P2 | 建议 | 该仓库使用明文 HTTP 且已过时；依赖已缓存时可移除，改用 Maven Central |
| `backend/pom.xml` | 94-109 | Neo4j 依赖组合 | `neo4j-java-driver:1.5.0` + `org.neo4j:neo4j:3.3.4` + `spring-boot-starter-data-neo4j` | P2 | 建议 | 三套 Neo4j 相关依赖并存，存在类路径冲突风险；下一阶段统一驱动版本并移除未使用的嵌入式依赖 |
| `docs/PROJECT_STRUCTURE.md` | 123、133、171-174 | 文档中的明文凭据 | 旧 MySQL、旧 Neo4j 账号密码 `<REDACTED>` | P1 | 是 | 文档脱敏；相关旧密码应视为已泄露并轮换 |
| `web/README.md` | 2 | 文档中的旧部署地址 | `http://47.113.226.5:8080/` | P3 | 建议 | 更新为本地地址或删除历史部署说明 |
| `database/数据库.sql` | 7 | SQL 导出元数据 | `Source Host: 47.113.226.5:3306` | P3 | 否 | 仅为 Navicat 导出注释，不是活动连接；可在下次导出时去除 |
| `README.md` | 220-224 | 文档描述已过期 | 仍称 MySQL/Neo4j/Vue/小程序“未修改” | P3 | 建议 | 当前 `application.yml` 已被修改，文档与代码不一致；下一阶段同步更新 |
| `miniprogram/project.config.json` | 12 | 小程序域名校验 | `"urlCheck": true` | P1 | 是（本地开发） | 本地调试需在开发者工具中关闭“不校验合法域名”；发布时改为 HTTPS 白名单域名 |

---

## 3. MySQL 检查结果

### 3.1 当前生效的源码配置

`backend/src/main/resources/application.yml:5-10`：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/smartques?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: <REDACTED>
```

| 检查项 | 结果 | 判定 |
| --- | --- | --- |
| 主机 | `127.0.0.1` | 已本地化 |
| 端口 | `3306` | 与本地 MySQL 默认端口一致 |
| 数据库名 | `smartques` | 与本地库名一致 |
| 时区 | `Asia/Shanghai` | 合理 |
| 用户名 | `root` | 硬编码 |
| 密码 | 明文 `<REDACTED>` | 硬编码，弱口令风险 |
| 环境变量 | 未使用 `${...}` | 不满足外部化要求 |

### 3.2 其他数据库连接配置

| 位置 | 内容 | 是否活动 | 判定 |
| --- | --- | --- | --- |
| `backend/src/main/resources/application.yml:8` | `jdbc:mysql://127.0.0.1:3306/smartques` | 是 | 本地 |
| `backend/target/classes/application.yml:7` | `jdbc:mysql://47.113.226.5:3306/smartques` | 构建产物 | **远程残留，P0** |
| `database/数据库.sql:7` | `Source Host: 47.113.226.5:3306` | 否 | 导出注释，非连接配置 |
| 其他 Java/YAML/properties/XML | 未发现第二个 `jdbc:mysql://` | — | 通过 |

> 说明：本次按要求**未连接 MySQL、未验证账号密码**。上述“已本地化”仅指配置目标地址，不代表认证一定成功。

补充核对：`database/数据库.sql` 共包含 11 个 `CREATE TABLE`（`chatting`、`entity`、`entity_relation`、`feedback_users`、`log`、`question`、`question_template`、`search_time`、`user_collectible`、`web_user`、`wechat_users`），与“11 张表”的描述一致；但该 SQL **没有 `INSERT` 种子数据**，因此不能据此认为 `web_user` 中已有可登录管理员。

### 3.3 MySQL 结论

**源码配置层面已基本本地化，但不能称为“完全本地化”**：`backend/target/classes/application.yml` 仍保留远程地址和旧账号密码；如果 IDE 直接运行已有 `target/classes`、或运行旧打包产物，可能绕过 `src/main/resources` 而连接原作者服务器。下一阶段必须清理构建产物并重新构建。

---

## 4. Neo4j 检查结果

### 4.1 全部 `GraphDatabase.driver` 调用逐项清单

后端共 **14 处** `GraphDatabase.driver` 调用：`Neo4jService.java` 13 处，`WeChatReplyService.java` 1 处。

| # | 文件:行号 | 所在方法/字段 | URI | 认证来源 | 是否本地 | 触发时机 | 判定 |
| ---: | --- | --- | --- | --- | --- | --- | --- |
| 1 | `Neo4jService.java:25-29` | `addEntity` | `bolt://127.0.0.1:7687` | `neo4j` / `${NEO4J_PASSWORD}` | 是 | 新增实体时 | 本地，但环境变量未定义 |
| 2 | `Neo4jService.java:38-39` | `matchEntity` | `bolt://47.113.226.5:7687` | `neo4j` / 明文 `<REDACTED>` | **否** | 新增/查询实体时 | **P0 远程** |
| 3 | `Neo4jService.java:52-53` | `countInDegree` | `bolt://47.113.226.5:7687` | 明文 `<REDACTED>` | **否** | 删除实体/统计关系时 | **P0 远程** |
| 4 | `Neo4jService.java:64-65` | `countOutDegree` | `bolt://47.113.226.5:7687` | 明文 `<REDACTED>` | **否** | 删除实体/统计关系时 | **P0 远程** |
| 5 | `Neo4jService.java:85-86` | `deleteEntity` | `bolt://47.113.226.5:7687` | 明文 `<REDACTED>` | **否** | 删除实体时 | **P0 远程** |
| 6 | `Neo4jService.java:94-95` | `updateEntity` | `bolt://47.113.226.5:7687` | 明文 `<REDACTED>` | **否** | 修改实体时 | **P0 远程** |
| 7 | `Neo4jService.java:105-106` | `addRelation` | `bolt://47.113.226.5:7687` | 明文 `<REDACTED>` | **否** | 新增关系时 | **P0 远程** |
| 8 | `Neo4jService.java:117-118` | `matchRelation` | `bolt://47.113.226.5:7687` | 明文 `<REDACTED>` | **否** | 新增/修改关系时 | **P0 远程** |
| 9 | `Neo4jService.java:133-134` | `deleteRelation` | `bolt://47.113.226.5:7687` | 明文 `<REDACTED>` | **否** | 删除关系时 | **P0 远程** |
| 10 | `Neo4jService.java:144-145` | `updateRelation` | `bolt://47.113.226.5:7687` | 明文 `<REDACTED>` | **否** | 修改关系时 | **P0 远程** |
| 11 | `Neo4jService.java:155-156` | `deleteAllRelation` | `bolt://47.113.226.5:7687` | 明文 `<REDACTED>` | **否** | 强制删除实体时 | **P0 远程** |
| 12 | `Neo4jService.java:167-168` | `allEntity` | `bolt://47.113.226.5:7687` | 明文 `<REDACTED>` | **否** | 全量同步实体时 | **P0 远程** |
| 13 | `Neo4jService.java:189-190` | `allRelation` | `bolt://47.113.226.5:7687` | 明文 `<REDACTED>` | **否** | `/test` 接口调用时 | **P0 远程** |
| 14 | `WeChatReplyService.java:32-37` | 类字段 `driver` | `bolt://127.0.0.1:7687` | `neo4j` / `${NEO4J_PASSWORD}` | 是 | Bean 初始化时 | 本地，但环境变量未定义 |

**统计：14 处调用中，仅 2 处本地，12 处仍远程；12 处远程全部使用硬编码明文密码。**

### 4.2 关键判断

| 审计问题 | 结论 | 依据 |
| --- | --- | --- |
| 是否存在旧的远程 Bolt 地址 | **是** | `Neo4jService.java:38,52,64,85,94,105,117,133,144,155,167,189` 共 12 处 |
| 是否全部改为本地连接 | **否** | 仅 `addEntity`（第 25 行）与 `WeChatReplyService` 字段（第 32 行）为本地 |
| 密码是否使用环境变量 | **仅 2 处** | 上述两处使用 `System.getenv("NEO4J_PASSWORD")`；其余 12 处为明文 |
| 是否存在重复的连接创建代码 | **是，且严重** | `Neo4jService` 每个方法都 `GraphDatabase.driver(...)` 新建驱动，从不 `close()`；每次业务调用都新建连接池 |
| 是否存在启动时自动连接远程 Neo4j 的风险 | **后端启动阶段未发现远程自动连接；但请求触发会立即连远程** | `Neo4jService` 的远程驱动在方法内创建；`WeChatReplyService` 字段在启动时创建本地驱动；前端 3 个页面在打开图谱时直连远程 |
| 是否有其他 Neo4j 连接 | **有，前端** | `Neo4j.vue:122`、`EntityForm.vue:374`、`RelationForm.vue:407` |

### 4.3 业务链路影响

1. `EntityManageService.addEntity()` 先调用 `matchEntity()`（远程），再根据结果调用 `addEntity()`（本地）。这意味着“新增实体”会先读取原作者服务器，再尝试写本地，形成**读写分裂**。
2. `RelationManageService.addRelation()` 依次调用 `matchEntity()`、`addEntity()`、`matchRelation()`、`addRelation()`，其中 3 个远程、1 个本地。
3. `Test.java:20-24` 的 `POST /test` 无鉴权，直接调用 `allRelation()`（远程），并把远程结果写入本地 MySQL。这是一个**可直接触发远程访问和本地写入的入口**。
4. `WeChatReplyService` 的 `driver` 是实例字段，`@Service` 单例在启动时初始化；`NEO4J_PASSWORD` 未定义时，认证信息为 `null`。驱动是否在 `session()` 阶段立即建连取决于驱动实现，因此**存在启动阶段失败的可能，也存在启动成功但首次问答失败的可能**；无论哪种，`/apart` 都不可用。

### 4.4 本地 Neo4j 认证风险（已验证 + 静态推断）

- **已验证**：当前审计进程环境中 `NEO4J_PASSWORD` **未定义**（未输出其值）。
- **静态推断**：`AuthTokens.basic("neo4j", null)` 在驱动 1.5.0 中通常不会在构造阶段抛出明确异常（空值可能被序列化为 null 凭据），但认证阶段会失败。本次未运行验证。
- **结论**：若本地 Neo4j 容器启用了认证（Docker 部署通常需要 `NEO4J_AUTH`），则 `addEntity` 和 `WeChatReplyService` 的所有查询都会认证失败；若容器显式禁用了认证，则可能“碰巧可用”。本次未检查 Docker 容器配置，也未做连接测试。

---

## 5. Vue 前端检查结果

### 5.1 代理配置

`web/vue.config.js:5-18`：

| 检查项 | 当前值 | 判定 |
| --- | --- | --- |
| `devServer.port` | `80` | 需要管理员权限，P1 |
| `proxy['/api'].target` | `http://47.113.226.5:8081` | **原作者服务器，P0** |
| 注释中的本地目标 | `// target:'http://localhost:8081'` | 仅注释，未生效 |
| `changeOrigin` | `true` | 正常 |
| `pathRewrite` | `'/api' -> ''` | 与后端无 `/api` 前缀一致 |
| `ws` | `true` | 正常 |

**影响**：开发模式下，Vue 所有 `/api/...` 请求（包括登录、修改密码、反馈、日志等）都会被代理到 `47.113.226.5:8081`。这意味着本地输入的管理员账号密码、用户数据可能被发送到原作者服务器。

### 5.2 baseURL 与硬编码 API 地址

| 文件:行号 | 内容 | 判定 |
| --- | --- | --- |
| `web/src/store.js:6` | `baseURL: '/api'` | 相对路径，正常 |
| `web/src/api/global_data.js:1` | `const baseURL = '/api'` | 相对路径，正常 |
| `web/src/utils/request.js:6` | 注释掉的 `https://www.easy-mock.com/...` | 未生效，P3 |
| 各 `.vue` 组件 | 统一使用 `this.$axios.post('/api/...')` | 无其他硬编码后端域名 |
| `web/src/api/index.js` | 空文件 | 无 API 模块，正常 |

**结论**：Vue 的业务 API 基址是相对 `/api`，本身没有硬编码远程后端；但 `vue.config.js` 的代理目标使这些相对请求全部流向原作者服务器。**代理是 Vue 侧最关键的单点问题。**

### 5.3 浏览器直连 Neo4j（P0）

三个页面在 `neo4jLoad()` 中直接使用 Neovis 连接 Neo4j：

| 文件:行号 | 触发方式 | 配置 |
| --- | --- | --- |
| `web/src/components/page/Neo4j.vue:40,122-124` | 打开 `/neo4j` 页面；`frame` 的 `@load` 事件调用（实际是否触发取决于浏览器对 `<frame>` 的处理） | `server_url: 'bolt://47.113.226.5:7687'`；`server_user: 'neo4j'`；`server_password: '<REDACTED>'` |
| `web/src/components/page/EntityForm.vue:61,374-376` | 打开实体图谱弹窗时加载 frame；`@load` 调用 | 同上 |
| `web/src/components/page/RelationForm.vue:56,407-409` | 打开关系图谱弹窗时加载 frame；`@load` 调用 | 同上 |

问题不仅是“地址远程”，还包括：

1. **密码进入前端 bundle**：任何访问者都能从浏览器源码中直接读到 Neo4j 密码。
2. **浏览器直连数据库**：绕过后端鉴权，数据库端口需对浏览器网络可达。
3. **无法审计**：图查询由客户端发起，后端没有统一入口、限流或日志。

此外，vendor 进来的 Neovis 包本身也带默认 Neo4j 配置：`web/src/utils/neovis.js:78` 与 `web/src/utils/neovis-without-dependencies.js:1` 含 `bolt://localhost:7687`、用户 `neo4j`、库内置默认密码（`<DEFAULT>`，非真实密码）。组件传参会覆盖它，但默认值仍进入前端 bundle，建议清理或显式置空。

**建议**：把图查询改为后端接口（后端持有 Neo4j 凭据），前端只调用 `/api/graph/...`；至少在下一阶段先将地址改为 `bolt://127.0.0.1:7687` 并从构建时环境变量注入密码，绝不能继续硬编码。

### 5.4 前端依赖与外部资源

| 问题 | 位置 | 判定 |
| --- | --- | --- |
| `web/node_modules` 不存在 | 目录检查 | P1：`npm run serve` 前必须 `npm install`，但本阶段禁止安装 |
| `import axios from '../node_modules/axios'` | `web/src/main.js:14` | P1：依赖 `node_modules` 实际存在，不能用常规模块解析回退 |
| `import qs from '../node_modules/qs'` | `web/src/main.js:15` | 同上 |
| 图标字体 CDN | `web/public/index.html:7` | P2：仅样式资源，离线时可能缺失图标 |
| `Upload.vue` 示例上传地址 | `web/src/components/page/Upload.vue:18` | P3：组件未挂载到路由 |

> 补充：当前 Vue 通过 `devServer.proxy` 访问后端，浏览器不直接跨域。若改为直连 `http://127.0.0.1:8081`，需要后端 CORS 支持；目前只有 `XunFeiController.java:19` 标注了 `@CrossOrigin`，其他 Controller 没有。因此本地化时建议**保留代理**，只把代理目标改为本地。

---

## 6. 微信小程序检查结果

### 6.1 需要修改的接口地址清单

小程序没有统一配置文件，**7 个页面文件、29 处活动 URL** 全部硬编码 `https://www.bulaqiu.top/api/...`；另有 1 处被注释（`home.js:725`）。

| 文件 | 行号 | 请求的后端路径 | 活动数量 |
| --- | --- | --- | ---: |
| `miniprogram/pages/home/home.js` | 122、137、198、245、295、332、380、417、464、500、547、585、614、647、699、724、870、1007、1020、1073 | `/apart`、`/get_collectible`、`/add_chatting`、`/remove_collectible`、`/add_collectible`、`/url`、`/hotSearch` | 20 |
| `miniprogram/pages/history/history.js` | 18、41、68 | `/wechat_chatting`、`/apart`、`/clearHistory` | 3 |
| `miniprogram/pages/login/login.js` | 63 | `/wechat_login` | 1 |
| `miniprogram/pages/enroll/enroll.js` | 84 | `/wechat_register` | 1 |
| `miniprogram/pages/changepw/changepw.js` | 49 | `/wechat_change_password` | 1 |
| `miniprogram/pages/collection/collection.js` | 17 | `/collectible` | 1 |
| `miniprogram/pages/feedback/feedback.js` | 24、111 | `/wechat_feedbackView`、`/wechat_feedback` | 2 |
| **合计** | — | — | **29** |

> `home.js:725` 是重复的注释行，不计入活动数量。

### 6.2 路径前缀问题（容易踩坑）

后端 Controller 的映射是**根路径**，例如：

- `WeChatLoginController.java:21` → `/wechat_login`
- `WeChatReplyController.java:28` → `/apart`
- `WeChatReplyController.java:22` → `/hotSearch`
- `XunFeiController.java:18` → `/url`

而小程序请求的是 `https://www.bulaqiu.top/api/wechat_login`，多出 `/api` 前缀。原作者部署时很可能由 Nginx 把 `/api` 前缀去掉后转发到 8081。Vue 的 `pathRewrite` 也做了同样的事（`/api` → 空）。

**因此不能只把域名换成 `http://127.0.0.1:8081` 而保留 `/api`**，否则会 404。本地化有两种可行方案：

**方案 A（推荐，改动直观）**：统一 `BASE_URL = 'http://127.0.0.1:8081'`，把 29 处 URL 改为 `${BASE_URL}/wechat_login` 等（去掉 `/api`）。

**方案 B（保留 `/api`）**：在后端设置 `server.servlet.context-path: /api`，或在本地加一层 Nginx/反向代理，把 `/api/...` 重写为 `/...`；同时要同步调整 Vue 代理的 `pathRewrite`，避免双重前缀。

### 6.3 小程序域名校验

`miniprogram/project.config.json:12` 为 `"urlCheck": true`。本地调试时：

1. 微信开发者工具 → 详情 → 本地设置 → 勾选“不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书”；
2. 或将本地后端配置为 HTTPS 并加入小程序后台白名单（发布阶段必须）。

真机调试不能使用 `127.0.0.1`，应使用电脑局域网 IP（如 `http://192.168.x.x:8081`），并确保手机与电脑同一网络、Windows 防火墙放行 8081。

### 6.4 其他小程序外部地址

| 地址 | 位置 | 判定 |
| --- | --- | --- |
| `https://developers.weixin.qq.com/...` | `sitemap.json:2` | 文档链接，非请求 |
| `https://test.com/onLogin` | `load.js:33` | 注释掉的模板代码，未生效 |
| `wss://iat-api.xfyun.cn/...` | `home.js:742`，由后端 `/url` 动态返回 | 正常第三方语音识别，不是硬编码 |

---

## 7. HanLP 路径检查结果

### 7.1 配置与代码中的路径

| 文件:行号 | 路径 | 当前状态 | 影响 |
| --- | --- | --- | --- |
| `backend/src/main/resources/hanlp.properties:3` | `root=/root/spring_java/data-for-1.7.5` | **目录不存在**（项目内、当前盘符下均未找到） | HanLP 按该根目录查找核心词典、自定义词典；Windows 下 `/root/...` 会被解析为当前盘符根目录下的 `root\spring_java\...` |
| `backend/src/main/java/com/example/springboot/service/WeChatReplyService.java:87` | `/root/spring_java/data-for-1.7.5/fuzzyWords.txt` | **文件不存在**（全项目未找到 `fuzzyWords.txt`） | `getFuzzyWords()` 读取失败 |
| `backend/src/main/resources/hanlp.properties:13` | `CustomDictionaryPath=data/dictionary/custom/CustomDictionary.txt; 现代汉语补充词库.txt; 全国地名大全.txt ns; 人名词典.txt; 机构名词典.txt; My.txt; 上海地名.txt ns;data/dictionary/person/nrf.txt nrf;` | 依赖上述根目录 | 多个自定义词典缺失 |
| `backend/src/main/java/com/example/springboot/service/CsvService.java:49` | 注释 `E:\\桌面\\团队项目\\entity.csv` | 仅注释 | 无实际影响，P3 |

> 补充：`backend/target/classes/hanlp.properties:3` 也保留了相同的 `root=/root/spring_java/data-for-1.7.5`；如果 IDE 直接使用旧构建产物，HanLP 同样会失效。

### 7.2 Windows 兼容性

- HanLP 的 `root` 支持相对路径（官方说明见 `hanlp.properties:1` 的链接），但当前值是 Linux 绝对路径。
- Windows 下 Java 会把 `/root/spring_java/...` 当作“当前驱动器根目录下的 `root\spring_java\...`”，例如 `D:\root\spring_java\data-for-1.7.5`，通常不存在。
- 路径分隔符应统一使用 `/`，例如 `root=D:/School/3/up/软工/SmartQA/backend/hanlp-data`，或相对路径 `root=./hanlp-data`。
- 项目中没有 `data-for-1.7.5` 目录；`hanlp-portable-1.7.2.jar` 自带的是 mini 词典（`data/dictionary/CoreNatureDictionary.mini.txt.bin` 等），不能替代 `hanlp.properties` 指向的完整数据目录。

### 7.3 运行时失败路径（静态推断）

`WeChatReplyService.getApart()`（第 46-56 行）在每次问答时调用 `NLPTokenizer.segment()`，随后调用 `fuzzyWords()`（第 54 行），后者调用 `getFuzzyWords()`（第 85-118 行）：

1. `new FileReader("/root/spring_java/data-for-1.7.5/fuzzyWords.txt")` 抛出 `FileNotFoundException`；
2. 异常被 `catch` 后仅 `printStackTrace()`，`fileReader` 仍为 `null`；
3. 第 95 行 `assert fileReader != null;` 默认不启用（JVM 未加 `-ea`）；
4. 第 96 行 `new BufferedReader(fileReader)` 抛出 `NullPointerException`。

因此，**即使 Neo4j 和 MySQL 都正常，`/apart` 和 `/hotSearch` 也会因为模糊词文件缺失而失败**。这是本地问答链路的确定性阻断点（代码路径确定，运行结果待验证）。

---

## 8. 第三方服务与外部地址分类

> 原则：不是所有 HTTPS 都是错误。以下按“是否必须替换”分类，避免把正常第三方 API、文档链接、构建仓库误判为远程后门。

### 8.1 必须移除或替换的原作者服务器连接（P0）

| 地址/位置 | 类型 | 说明 |
| --- | --- | --- |
| `bolt://47.113.226.5:7687`（后端 12 处） | Neo4j 远程 | `Neo4jService.java` 12 个方法 |
| `bolt://47.113.226.5:7687`（前端 3 处） | Neo4j 浏览器直连 | `Neo4j.vue`、`EntityForm.vue`、`RelationForm.vue` |
| `http://47.113.226.5:8081` | Vue 开发代理 | `vue.config.js:10` |
| `https://www.bulaqiu.top/api/...`（29 处） | 小程序 API | 7 个页面文件 |
| `jdbc:mysql://47.113.226.5:3306/smartques` | 构建产物 MySQL | `backend/target/classes/application.yml:7` |
| `http://47.113.226.5:8080/` | 旧部署文档 | `web/README.md:2`，仅文档 |

### 8.2 正常第三方 API（保留，但需外部化密钥并轮换）

| 地址 | 位置 | 用途 | 判定 |
| --- | --- | --- | --- |
| `https://iat-api.xfyun.cn/v2/iat` | `UrlService.java:20` | 讯飞语音听写，后端生成带签名的 WebSocket URL | 正常第三方；但 `apiKey`/`apiSecret` 硬编码且已泄露，必须轮换并改环境变量 |
| `https://sms_developer.zhenzikj.com` | `WebLoginService.java:49` | 榛子云短信验证码（忘记密码） | 正常第三方；`appId`/`appSecret` 硬编码且已泄露，必须轮换并改环境变量 |
| `//at.alicdn.com/t/font_830376_qzecyukz0s.css` | `web/public/index.html:7` | Element UI 图标字体 CDN | 正常静态资源；可本地化，P2 |
| `http://jsonplaceholder.typicode.com/api/posts/` | `Upload.vue:18` | Element UI 上传示例 | 示例组件未被路由挂载，P3 |

### 8.3 仅文档、注释、示例或历史数据中的地址（不生效）

| 地址 | 位置 | 性质 |
| --- | --- | --- |
| `http://47.113.226.5:8080/` | `web/README.md:2` | 旧部署说明 |
| `47.113.226.5:3306` | `database/数据库.sql:7` | Navicat 导出元数据 |
| `https://www.easy-mock.com/mock/...` | `web/src/utils/request.js:6` | 注释掉的 mock 地址 |
| `https://test.com/onLogin` | `miniprogram/pages/load/load.js:33` | 注释掉的模板代码 |
| `http://mybatis.org/dtd/mybatis-3-mapper.dtd` | 各 Mapper XML 第 2 行 | XML DTD 声明，非运行时业务请求 |
| `https://visjs.github.io/...`、`http://almende.com`、`https://github.com/...`、`http://www.apache.org/licenses/...` | `web/src/utils/neovis.js`、`neovis-without-dependencies.js` 注释 | 第三方库版权/文档链接 |
| `https://element.eleme.cn/...`、`http://element.eleme.io/...` | `I18n.vue:20`、`Upload.vue:13,27` | 组件文档链接 |
| `https://developers.weixin.qq.com/...` | `miniprogram/sitemap.json:2` | 微信文档链接 |
| `http://m2.neo4j.org/...` | `pom.xml:128` | 构建期 Maven 仓库，非运行时；已过时且为 HTTP，建议移除 |
| `https://repo.maven.apache.org/...` | `.mvn/wrapper/maven-wrapper.properties:1-2` | Maven Wrapper 下载地址，构建期正常 |
| `registry.npm.taobao.org` 等 | `web/package-lock.json` 的 `resolved` | 旧 npm 镜像，安装期可能失效；P2 |

### 8.4 当前无法确定用途或不应继续使用

| 地址 | 位置 | 说明 |
| --- | --- | --- |
| `https://www.bulaqiu.top` | 小程序 29 处 | 已确认是原作者后端域名；本地化时必须替换 |
| `47.113.226.5` 的 8080/8081/7687/3306 | 多处 | 原作者服务器；不应再访问 |
| `https://www.easy-mock.com` | 注释 | 原 mock 服务，已停用；保留注释无害但会误导 |

---

## 9. 本地运行阻碍

> “已验证”= 本次通过读取文件、检查目录/环境变量确认；“静态推断”= 依据代码路径推断，未实际启动验证。

| # | 阻碍 | 影响 | 证据 | 判定 |
| ---: | --- | --- | --- | --- |
| 1 | `NEO4J_PASSWORD` 未定义 | 本地 Neo4j 认证为空；`addEntity` 与 `WeChatReplyService` 全部查询可能失败 | `Neo4jService.java:25-29`、`WeChatReplyService.java:32-36`；当前环境变量检查 | 已验证（变量缺失）+ 静态推断（认证失败） |
| 2 | `WeChatReplyService` 字段级建连 | 启动时创建 Driver/Session；若驱动在 `session()` 阶段建连且认证失败，可能阻断启动 | `WeChatReplyService.java:32-37` | 静态推断 |
| 3 | 12 处远程 Neo4j | 实体/关系/图谱管理会访问原作者服务器，可能读写他人数据 | `Neo4jService.java:38-190` | 静态推断（代码路径确定） |
| 4 | HanLP `root` 指向不存在的 Linux 目录 | 分词器/自定义词典初始化失败，问答不可用 | `hanlp.properties:3`；目录检查 | 已验证（目录不存在）+ 静态推断（初始化失败） |
| 5 | `fuzzyWords.txt` 缺失 | `/apart`、`/hotSearch` 在 `getFuzzyWords()` 处 NPE | `WeChatReplyService.java:87-96`；全项目未找到该文件 | 已验证（文件不存在）+ 静态推断（NPE） |
| 6 | `target/classes/application.yml` 仍是远程 MySQL | IDE 直接运行旧 `target/classes` 时可能连接原作者数据库 | `backend/target/classes/application.yml:7-9` | 已验证（文件内容） |
| 7 | `web/node_modules` 不存在 | `npm run serve` 无法启动；`main.js` 直接从 `../node_modules` 导入 axios/qs | `web/src/main.js:14-15`；目录检查 | 已验证（目录不存在） |
| 8 | Vue 开发端口 80 | Windows 下非管理员通常无法绑定 80 | `vue.config.js:7` | 静态推断 |
| 9 | 小程序 `urlCheck: true` 且请求旧域名 | 本地后端不在合法域名列表，请求被开发者工具拦截 | `project.config.json:12`；29 处 `bulaqiu.top` | 已验证（配置）+ 静态推断（拦截） |
| 10 | 小程序 `/api` 前缀与后端根路径不一致 | 只换域名会 404 | 小程序 29 处 URL；Controller 映射无 `/api` | 已验证（路由对比） |
| 11 | `web_user` 表无种子管理员 | 网页端登录查不到账号，登录失败 | `database/数据库.sql` 无 `INSERT INTO`；`WebLoginService.java:20-28` | 静态推断（可能已另行导入，需确认） |
| 12 | Spring Boot 2.5.6 + Java 21 + 多套 Neo4j 依赖 | 运行时兼容性未验证；`neo4j-java-driver:1.5.0` 与 `spring-data-neo4j 6.1.6` 期望的 4.x 驱动不一致 | `pom.xml:17`、`pom.xml:94-109` | 静态推断（编译成功不等于运行成功） |
| 13 | `hotSearch()` 固定 `subList(0, 4)` | 热搜不足 4 条时抛 `IndexOutOfBoundsException` | `WeChatReplyService.java:345` | 静态推断（非配置问题，顺带发现） |
| 14 | 所有 Controller 无鉴权 | 任意本地/局域网访问者可调用 `/test`、增删改接口 | 各 Controller 无安全配置；`Test.java:20` | 已验证（代码） |

### 9.1 建议的“最小启动前检查清单”

在下一阶段真正启动 Spring Boot 之前，至少确认：

1. `NEO4J_PASSWORD` 已设置，且与本地 Neo4j 容器认证一致；
2. `mvn clean` 已删除旧 `target/classes`，运行使用新的 `src/main/resources/application.yml`；
3. 本地 MySQL `smartques` 的 `root` 密码与配置一致（本阶段未验证）；
4. `data-for-1.7.5` 与 `fuzzyWords.txt` 已放到 Windows 可访问路径，或代码已能优雅降级；
5. 12 处远程 Neo4j、Vue 代理、3 处前端 Bolt、29 处小程序域名已替换，或至少已阻断到 `47.113.226.5` / `bulaqiu.top` 的出站访问；
6. 已确认 `web_user` 表中有可登录的管理员账号。

**在上述条件满足前，不建议启动 Spring Boot。**

---

## 10. 推荐修改顺序

> 本节只是下一阶段的建议，**本阶段未执行任何修改**。

### 10.1 P0：先切断原作者服务器与硬编码凭据

| 顺序 | 动作 | 涉及文件 | 验收标准 |
| ---: | --- | --- | --- |
| P0-1 | 把 Vue 代理改到本地 | `web/vue.config.js:10` | `grep -R "47.113.226.5" web/` 不再命中活动配置 |
| P0-2 | 移除 3 处前端 Neo4j 直连，改为后端图查询接口 | `Neo4j.vue`、`EntityForm.vue`、`RelationForm.vue` | 前端 bundle 中不再出现 Neo4j 地址/密码 |
| P0-3 | 合并 `Neo4jService` 的 13 个 Driver 为一个本地共享 Driver | `Neo4jService.java` | 全类只有 1 处 `GraphDatabase.driver`，且 URI 为本地 |
| P0-4 | 统一 `WeChatReplyService` 的 Neo4j 连接 | `WeChatReplyService.java:32-37` | 不再在字段初始化时建连；复用共享 Driver |
| P0-5 | 小程序抽取 `BASE_URL` 并替换 29 处 | 7 个小程序页面文件 | `grep -R "bulaqiu.top" miniprogram/` 为空 |
| P0-6 | 清理 `target/classes` 中的远程 MySQL | `backend/target/` | 重新构建后 `target/classes/application.yml` 为本地地址 |
| P0-7 | 轮换已暴露的凭据 | 旧 MySQL、旧 Neo4j、讯飞、榛子云 | 新凭据只通过环境变量/密钥管理注入，不再入库 |
| P0-8 | 限制出站访问 | 主机/网络层（下一阶段） | 本地运行时无法访问 `47.113.226.5` 与 `bulaqiu.top` |

### 10.2 P1：让本地链路真正可运行

| 顺序 | 动作 | 涉及文件 | 验收标准 |
| ---: | --- | --- | --- |
| P1-1 | 外部化 MySQL 密码 | `application.yml:10` | 使用 `${MYSQL_PASSWORD}` 或 `application-local.yml` |
| P1-2 | 设置并校验 `NEO4J_PASSWORD` | `Neo4jService.java:29`、`WeChatReplyService.java:36` | 缺失时启动即报清晰错误，而不是运行到一半失败 |
| P1-3 | 本地化 HanLP 数据目录 | `hanlp.properties:3` | Windows 路径存在，分词可初始化 |
| P1-4 | 本地化 `fuzzyWords.txt` | `WeChatReplyService.java:87` | 文件存在且可读；缺失时返回空列表并记录警告 |
| P1-5 | 调整 Vue 端口并安装依赖 | `vue.config.js:7`、`web/` | `npm install` 后 `npm run serve` 可启动 |
| P1-6 | 小程序切换本地地址并关闭域名校验 | 7 个页面、`project.config.json:12` | 开发者工具可登录、可问答 |
| P1-7 | 处理 `/api` 前缀 | 小程序 URL 或后端 context-path | 本地请求不再 404 |
| P1-8 | 准备管理员种子账号 | `database/` 或初始化 SQL | 网页端可登录 |
| P1-9 | 文档凭据脱敏并轮换 | `docs/PROJECT_STRUCTURE.md:123,133,171-174` | 文档中不再出现明文密码 |

### 10.3 P2：后续优化

1. 把 Neo4j 驱动改为 Spring Bean，统一生命周期并在关闭时 `close()`；
2. 删除 `store.js`/`global_data.js` 中的演示账号密码；
3. 本地化 `at.alicdn.com` 图标字体，或移除未使用样式；
4. 移除 `pom.xml:128` 的 `http://m2.neo4j.org` 仓库，统一 Neo4j 驱动版本，移除未使用的嵌入式依赖；
5. 在 Java 21 下实际启动验证 Spring Boot 2.5.6 的兼容性，必要时升级 Spring Boot 或改用 Java 17；
6. 修复 `hotSearch()` 的 `subList(0,4)` 越界问题；
7. 修复 Cypher 字符串拼接（`Neo4jService` 多处 `"..." + label + "..."`）带来的注入风险；
8. 数据库密码不应明文存储/明文比较（`WebLoginService.java:20-28`、`WeChatLoginService.java:18-24`）；
9. 更新 `README.md`、`docs/PROJECT_STRUCTURE.md`，消除“未修改”的过期描述；
10. 清理 Neovis 内置默认 Neo4j 凭据（`neovis.js:78`、`neovis-without-dependencies.js:1`），避免默认认证信息进入前端 bundle。

### 10.4 P3：低优先级

1. `Upload.vue` 示例地址：未挂载，可删除；
2. `web/README.md` 旧部署地址：更新或删除；
3. `database/数据库.sql:7` 的 Source Host：下次导出时去除；
4. 注释中的 easy-mock、test.com 地址：清理即可。

---

## 11. 最终结论

| 问题 | 结论 | 关键依据 |
| --- | --- | --- |
| **MySQL 是否已经完全本地化？** | **源码层面基本本地化，但整体不算“完全”。** `application.yml` 已指向 `127.0.0.1:3306/smartques`；但 `target/classes/application.yml` 仍是 `47.113.226.5:3306`，且密码明文硬编码。 | `backend/src/main/resources/application.yml:8-10`；`backend/target/classes/application.yml:7-9` |
| **Neo4j 是否已经完全本地化？** | **否。** 14 个 `GraphDatabase.driver` 调用中 12 个仍是远程；前端还有 3 处浏览器直连远程。仅 `addEntity` 与 `WeChatReplyService` 字段使用本地地址，且依赖未定义的 `NEO4J_PASSWORD`。 | `Neo4jService.java:38-190`（12 处）；`WeChatReplyService.java:32-37`；`Neo4j.vue:122`；`EntityForm.vue:374`；`RelationForm.vue:407` |
| **后端是否还存在旧远程服务连接？** | **是。** 主要是 12 处远程 Neo4j；`/test` 会触发 `allRelation()` 远程读取并写本地 MySQL。MySQL 源码已无远程地址，但构建产物仍有。讯飞/榛子云是正常第三方，不是原作者服务器。 | `Neo4jService.java:189-190`；`Test.java:20-24`；`target/classes/application.yml:7` |
| **Vue 是否仍有旧代理地址？** | **是。** `/api` 代理仍指向 `http://47.113.226.5:8081`；另有 3 个组件直连 `bolt://47.113.226.5:7687` 并硬编码密码。 | `web/vue.config.js:10`；`Neo4j.vue:122-124`；`EntityForm.vue:374-376`；`RelationForm.vue:407-409` |
| **小程序是否仍有旧 API 域名？** | **是。** 7 个文件、29 处活动 URL 仍为 `https://www.bulaqiu.top/api/...`，另有 1 处注释。 | `home.js` 20 处；`history.js` 3 处；`login.js`、`enroll.js`、`changepw.js`、`collection.js` 各 1 处；`feedback.js` 2 处 |
| **是否发现凭据泄露风险？** | **是，且风险较高。** 21 处硬编码凭据：MySQL 1、Neo4j 后端 12、Neo4j 前端 3、讯飞 2、榛子云 2、构建产物 MySQL 1；`docs/PROJECT_STRUCTURE.md` 还包含旧 MySQL/Neo4j 明文密码。前端 Neo4j 密码会进入浏览器 bundle；另有 2 处 Neovis 库内置默认值（非真实密码）。 | 见第 2 节；`UrlService.java:21-22`；`WebLoginService.java:50-51`；`docs/PROJECT_STRUCTURE.md:123,133,171-174` |
| **是否建议现在启动 Spring Boot？** | **不建议。** 启动后：实体/关系操作可能访问原作者 Neo4j；`/apart` 因 `fuzzyWords.txt` 缺失必然失败；`NEO4J_PASSWORD` 未定义导致本地 Neo4j 认证不确定；旧 `target/classes` 可能连远程 MySQL。 | 见第 9 节清单 |

### 11.1 最终判定

1. **当前代码库不适合直接联调**。P0 问题不是“潜在风险”，而是已经存在于活动代码路径中的远程连接与明文凭据。
2. **“已手动修改 application.yml”只完成了一半**：源码 MySQL 已本地化，但 Neo4j 仅改了 `addEntity` 1 个方法和 `WeChatReplyService` 1 个字段，`Neo4jService` 其余 12 个方法仍远程；此外还遗漏了 `target/classes`、前端 3 处、Vue 代理、小程序 29 处。
3. **最危险的三个点**：
   - `Neo4jService.java` 12 处远程 Bolt + 明文密码；
   - `vue.config.js` 把所有 `/api` 请求代理到原作者服务器；
   - 小程序 29 处请求把用户手机号、密码、聊天内容发往 `bulaqiu.top`。
4. **建议下一阶段按 P0 → P1 顺序修复，并在修复后做一次“断网/屏蔽旧 IP”验证**：在无法访问 `47.113.226.5` 与 `bulaqiu.top` 的条件下，后端、Vue、小程序仍能完成登录、问答、图谱查看，才算真正本地化。

---

## 附录 A：审计方法与边界

- **已执行**：逐文件读取关键配置与源码；全项目关键词检索（`47.113.226.5`、`bulaqiu.top`、`jdbc:mysql://`、`bolt://`、`neo4j://`、`GraphDatabase.driver`、`AuthTokens.basic`、`DriverManager.getConnection`、`axios`、`wx.request`、`fetch`、`OkHttpClient`、`RestTemplate`、`root/spring_java`、`fuzzyWords.txt`、`password`、`apiKey`、`secret` 等）；目录/文件存在性检查；环境变量存在性检查；构建产物副本比对；归档文档关键词扫描。
- **未执行**：任何数据库连接、任何 HTTP/WebSocket 请求、任何服务启动、任何依赖安装、任何代码/配置修改、任何 Git 操作、任何 Docker 操作、任何 ACL/环境变量修改。
- **未覆盖**：`.git` 历史提交（按要求忽略；嵌套 `.git` 可能仍含旧凭据）；`docs/archive` 中 `.doc`/`.docx` 的逐字全文（仅做关键词扫描，未发现 `47.113.226.5`/`bulaqiu.top` 字符串，但存在个人姓名等信息）；Neo4j 容器实际认证模式；MySQL 实际账号密码；Spring Boot 实际启动结果。
- **误报排除**：MyBatis DTD URL、第三方库 license 链接、Element UI 文档链接、微信 sitemap 文档链接均不作为远程连接问题。

## 附录 B：本次审计未修改的文件声明

本阶段**只新增了本报告** `docs/REMOTE_CONNECTION_AUDIT.md`，未修改：

- `backend/` 下任何 Java、YAML、properties、XML；
- `web/` 下任何 JS、Vue、JSON、`vue.config.js`；
- `miniprogram/` 下任何 JS、JSON、WXML；
- `database/`、`README.md`、`docs/` 下既有文档；
- Windows 环境变量、ACL、Docker 容器、Git 仓库。

后续修复请在新阶段按第 10 节顺序执行，并在每次修改后重新运行本报告中的关键词检索作为回归检查。
