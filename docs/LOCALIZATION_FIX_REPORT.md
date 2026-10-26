# SmartQA Phase 1E：本地化修复与运行准备报告

> 项目：`D:\School\3\up\软工\SmartQA`
> 日期：2026-10-22
> 性质：本地化修复 + 静态验证；**未启动 Spring Boot / Vue / 小程序，未连接 MySQL / Neo4j，未执行数据库读写，未安装依赖**。
> 修改前备份：`D:\School\3\up\软工\.smartqa-backup-phase1e-20261022-1957`（位于项目外，含修改前文件，未纳入 Git；内含旧凭据，验证后应删除或严格限制访问）。
> 原始项目：`D:\School\3\up\软工\project\智能问答知识库管理系统` **未修改**。

---

## 1. 修改前的问题

依据 `docs/REMOTE_CONNECTION_AUDIT.md` 与当前磁盘复核，修复前存在：

| 类别 | 问题 |
| --- | --- |
| Neo4j 后端 | `Neo4jService.java` 13 个方法各自新建 Driver，其中 12 个硬编码 `bolt://47.113.226.5:7687` 与明文密码；`WeChatReplyService` 字段初始化时另建一个本地 Driver，且依赖未定义的 `NEO4J_PASSWORD` |
| MySQL | 源码已指向 `127.0.0.1:3306/smartques`，但密码明文；`target/classes/application.yml` 仍是远程地址 + 旧账号密码 |
| Vue 代理 | `vue.config.js` 端口 80，`/api` 代理到 `http://47.113.226.5:8081` |
| Vue 图谱 | `Neo4j.vue`、`EntityForm.vue`、`RelationForm.vue` 在浏览器中直连 `bolt://47.113.226.5:7687` 并硬编码数据库密码 |
| 小程序 | 7 个页面、29 处活动 URL 指向 `https://www.bulaqiu.top/api/...`，且带后端不存在的 `/api` 前缀 |
| 第三方密钥 | 讯飞 `apiKey/apiSecret`、榛子云 `appId/appSecret` 明文写在 Java 中 |
| HanLP | `root=/root/spring_java/data-for-1.7.5`，`fuzzyWords.txt` 不存在；缺失时 `getFuzzyWords()` 会 NPE |
| 构建产物 | `backend/target/classes/application.yml` 保留远程 MySQL 配置 |
| 文档 | `README.md`、`docs/PROJECT_STRUCTURE.md` 含旧地址/明文凭据描述 |

---

## 2. 修改的具体文件与行号

> 行号为修改后的当前行号；“新增”表示新文件。

### 2.1 后端 Neo4j 与 MySQL

| 文件 | 行号 | 修改 |
| --- | --- | --- |
| `backend/src/main/java/com/example/springboot/config/Neo4jConfig.java` | 25-41（新增） | 新增唯一 `Driver` Bean，读取 `NEO4J_URI` / `NEO4J_USER` / `NEO4J_PASSWORD`，`destroyMethod="close"`；缺密码抛明确异常，不回退旧密码 |
| `backend/src/main/java/com/example/springboot/service/Neo4jService.java` | 29-33 | 构造器注入唯一 `Driver`；删除 13 处 `GraphDatabase.driver` 与硬编码密码 |
| `Neo4jService.java` | 35、46、59、70、90、98、107、117、131、140、149、159、179 | 每个方法使用 try-with-resources `Session`；写操作使用 `consume()` 确保提交 |
| `backend/src/main/java/com/example/springboot/service/WeChatReplyService.java` | 44-50 | 构造器注入 `Driver` 与 `HANLP_FUZZY_WORDS`，删除字段级硬编码连接 |
| `WeChatReplyService.java` | 156、189、220、263 | 查询方法使用独立 `Session` 并在 try-with-resources 中关闭 |
| `backend/src/main/java/com/example/springboot/service/EntityManageService.java` | 24-25 | `new Neo4jService()` 改为 `@Autowired Neo4jService` |
| `backend/src/main/java/com/example/springboot/service/RelationManageService.java` | 22-23 | 同上 |
| `backend/src/main/java/com/example/springboot/service/CsvService.java` | 24-25 | 同上 |
| `backend/src/main/java/com/example/springboot/ExampleApplication.java` | 14-18 | 排除 Spring Data Neo4j 自动配置，避免 1.5.0 驱动与 SDN 6.1 的 4.x 期望冲突 |
| `backend/src/main/resources/application.yml` | 9-12 | 保持本地 `127.0.0.1:3306/smartques`；`username: ${DB_USER:root}`；`password: ${DB_PASSWORD}` |
| `backend/src/main/resources/hanlp.properties` | 3-5 | `root=./hanlp-data`，注释说明可用 `HANLP_ROOT` 覆盖 |

### 2.2 后端第三方与 HanLP

| 文件 | 行号 | 修改 |
| --- | --- | --- |
| `backend/src/main/java/com/example/springboot/service/UrlService.java` | 21-44 | 删除讯飞明文密钥；改 `XFYUN_API_KEY` / `XFYUN_API_SECRET`；缺配置抛明确异常，不调用旧账号 |
| `backend/src/main/java/com/example/springboot/controller/XunFeiController.java` | 23-38 | `/url` 路径不变；新增 `IllegalStateException` → 503 JSON 错误 |
| `backend/src/main/java/com/example/springboot/service/WebLoginService.java` | 22-32、63-68 | 删除榛子云明文 `appId/appSecret`；改 `ZHENZI_SMS_*` 环境变量；缺配置返回明确提示，不调用第三方 |
| `WeChatReplyService.java` | 112-138 | `getFuzzyWords()` 改为 UTF-8 读取、缺失时返回空列表并警告，不再 NPE |
| `WeChatReplyService.java` | 60-79、112-138 | HanLP 分词失败时抛出明确的“HanLP 分词不可用”异常；模糊词缺失时跳过替换 |
| `WeChatReplyService.java` | 366-394 | `hotSearch()` 的 `subList(0, 4)` 改为 `subList(0, Math.min(4, size))`，避免空数据越界 |
| `backend/hanlp-data/README.md` | 全文（新增） | 说明所需 HanLP 数据、`fuzzyWords.txt` 格式、缺失行为；不包含伪造词典 |

### 2.3 Vue 代理与图谱

| 文件 | 行号 | 修改 |
| --- | --- | --- |
| `web/vue.config.js` | 7、10 | 端口 80 → 8080；代理目标 → `http://127.0.0.1:8081`；保留 `/api` → 空的重写 |
| `web/src/components/common/GraphView.vue` | 全文（新增） | 使用 `vis-network/standalone` 渲染后端数据；不再连接 Bolt；组件销毁时 `network.destroy()` |
| `web/src/utils/graphData.js` | 7、73、88、117（新增） | `loadGraph` 复用 `/api/get_entity`、`/api/get_relation`；提供邻域/关系/条件过滤 |
| `web/src/components/page/Neo4j.vue` | 42-48、53-58、94-122 | 删除 Neovis/Bolt/密码；改为 `GraphView` + `loadGraph`/`filterGraph`；保留实体/关系/标签筛选与数量限制 |
| `web/src/components/page/EntityForm.vue` | 61-68、105-110、256-277 | 查看实体弹窗改为 `GraphView`；按实体 id 从后端数据取邻域；删除 `neo4jLoad` |
| `web/src/components/page/RelationForm.vue` | 56-63、143-148、391-412 | 查看关系弹窗改为 `GraphView`；按起止实体/关系筛选；删除 `neo4jLoad` |
| `web/src/utils/neovis.js`、`neovis-without-dependencies.js` | 对应默认配置 | 将库内置 `neo4jPassword`/`neo4jUri` 默认值置空（文件保留但已无任何 import） |
| `web/src/utils/neovis*.js.map` | 对应 sourcesContent | 同步置空默认凭据/URI，避免 source map 残留默认密码 |
| `web/src/store.js`、`web/src/api/global_data.js` | 16 / 11 | 演示密码 `test` 置空 |

### 2.4 小程序

| 文件 | 行号 | 修改 |
| --- | --- | --- |
| `miniprogram/utils/config.js` | 12-14、30-39（新增） | 开发者工具 `http://127.0.0.1:8081`；真机 `DEVICE_BASE_URL`；发布 `PRODUCTION_BASE_URL` 留空 |
| `miniprogram/pages/home/home.js` | 3、124-1075 | 引入 `baseUrl`；20 处活动 URL 去掉 `https://www.bulaqiu.top/api` |
| `miniprogram/pages/history/history.js` | 2、20、43、70 | 引入 `baseUrl`；3 处 URL 替换 |
| `miniprogram/pages/login/login.js` | 2、65 | 引入 `baseUrl`；`/wechat_login` |
| `miniprogram/pages/enroll/enroll.js` | 2、86 | 引入 `baseUrl`；`/wechat_register` |
| `miniprogram/pages/changepw/changepw.js` | 2、51 | 引入 `baseUrl`；`/wechat_change_password` |
| `miniprogram/pages/collection/collection.js` | 2、19 | 引入 `baseUrl`；`/collectible` |
| `miniprogram/pages/feedback/feedback.js` | 1、26、113 | 引入 `baseUrl`；`/wechat_feedbackView`、`/wechat_feedback` |

### 2.5 文档

| 文件 | 修改 |
| --- | --- |
| `README.md` | 更新 Phase 1E 状态、端口 8080、环境变量表；删除旧地址/明文凭据描述 |
| `docs/PROJECT_STRUCTURE.md` | 更新代理/小程序/MySQL/Neo4j/HanLP 描述；删除旧 IP、明文密码、Linux 路径 |
| `web/README.md` | 改为本地 `http://127.0.0.1:8080/` 与 8081 代理说明 |
| `docs/REMOTE_CONNECTION_AUDIT.md` | 顶部增加历史状态说明，指向本报告；保留修复前证据 |

---

## 3. 修改原因

| 修改 | 原因 |
| --- | --- |
| 统一 Neo4j `Driver` + 环境变量 | 消除 12 处远程硬编码、重复连接池和密码泄露；满足“本地默认 + 显式配置” |
| 缺 `NEO4J_PASSWORD` 直接报错 | 不允许旧密码回退，避免“看似能启动、实际连旧库” |
| `try-with-resources` + `consume()` | 确保 Session 关闭、写操作提交，避免连接泄漏和隐式回滚 |
| 排除 Spring Data Neo4j 自动配置 | 项目使用 driver 1.5.0，SDN 6.1.6 期望 4.x；排除未使用的自动配置，降低启动类路径冲突风险（未运行验证） |
| MySQL 密码环境变量化 | 源码/构建产物不再含真实密码；缺变量时启动失败而非静默连错库 |
| Vue 图谱改用后端实体/关系接口 | 禁止浏览器直连 Bolt、禁止前端保存数据库密码；复用已有接口，避免新增后端写接口 |
| 小程序统一 `baseUrl` | 删除 29 处原作者域名；开发者工具/真机/生产分离；去掉后端不存在的 `/api` 前缀 |
| 第三方密钥环境变量化 | 移除已泄露的讯飞/榛子云凭据；未配置时明确失败，不调用旧账号 |
| HanLP 相对路径 + 缺失容错 | 消除 Linux 绝对路径；`fuzzyWords.txt` 缺失不再 NPE；核心词典缺失给出明确错误 |
| 文档脱敏 | 清除新项目文档中的旧地址/凭据描述，避免误导和二次泄露 |

---

## 4. 修改前后行为差异

| 方面 | 修改前 | 修改后 |
| --- | --- | --- |
| Neo4j 连接 | 每个方法新建 Driver；12 个方法连原作者服务器；密码明文 | 全应用一个 Driver；默认本地 `bolt://127.0.0.1:7687`；密码仅环境变量；缺失时启动/建 Bean 失败并提示 |
| Neo4j 写操作 | `session.run` 后不消费结果、不关闭 Session | `try (Session ...)` + 写操作 `consume()`；正常关闭 |
| `WeChatReplyService` | 字段初始化即建 Driver/Session；`fuzzyWords` 缺失 NPE | 构造器注入 Driver；查询时独立 Session；fuzzyWords 缺失时跳过替换并告警；HanLP 核心缺失时明确异常 |
| MySQL | 密码明文 | `DB_PASSWORD` 必填；`DB_USER` 默认 root；缺密码启动失败 |
| Vue 代理 | 端口 80，代理原作者 8081 | 端口 8080，代理本地 8081；`/api` 重写不变 |
| Vue 图谱 | 浏览器 Bolt 直连，前端保存密码 | 调用 `/api/get_entity`、`/api/get_relation`，vis-network 渲染；前端无数据库凭据 |
| 图谱数据语义 | 实时 Neo4j 查询 | 读取 MySQL `entity` / `entity_relation` 镜像数据；若 MySQL 未同步，图谱可能为空（见第 10 节） |
| 小程序 | 29 处 `https://www.bulaqiu.top/api/...` | `baseUrl + '/...'`；开发者工具默认 `127.0.0.1:8081`，真机用 `DEVICE_BASE_URL`，发布留空 |
| 讯飞 `/url` | 使用硬编码密钥，缺配置时行为不明确 | 缺 `XFYUN_*` 时返回 503 JSON；成功时仍返回原 String URL |
| 榛子云短信 | 使用硬编码密钥 | 缺 `ZHENZI_SMS_*` 时返回“短信服务未配置”，不调用第三方 |
| HanLP | Linux 绝对路径 | `./hanlp-data` 相对路径，可用 `HANLP_ROOT` 覆盖；未附带数据 |
| `hotSearch` | 少于 4 条时 `IndexOutOfBoundsException` | 返回最多 4 条，不越界 |

---

## 5. 编译结果

### 5.1 执行的命令与结果

| 步骤 | 命令 | 结果 |
| --- | --- | --- |
| 清理 | `mvn -o -DskipTests clean` | **失败**：`maven-clean-plugin:3.1.0` 未在本地缓存，离线不可用 |
| 清理（替代） | 手工删除 `backend/target`（仅构建产物） | 成功；因 `target` ACL 限制，删除操作使用了一次完整访问权限 |
| 编译 | `mvn -o -DskipTests compile` | **BUILD SUCCESS**；`Compiling 67 source files`，输出 `target/classes` |
| 再次编译 | 修改 `Test.java` 注释后重跑 `mvn -o -DskipTests compile` | **BUILD SUCCESS**；67 个源文件 |

> 说明：未升级 Maven/Java/Spring Boot，未修改仓库源，未下载新依赖；`mvn clean` 插件缺失是环境缓存问题，已在报告中记录。

### 5.2 构建产物检查

- `backend/target/classes/application.yml`：已由 `mvn compile` 重新生成，指向 `127.0.0.1:3306/smartques`，使用 `${DB_USER:root}` / `${DB_PASSWORD}`。
- `backend/target/classes/hanlp.properties`：`root=./hanlp-data`。
- 扫描 `backend/target`：`47.113.226.5` / `bulaqiu.top` / 旧 Neo4j 密码 / 旧 MySQL 密码 / `/root/spring_java` 命中数 **0**。

---

## 6. API 兼容性检查结果

| 检查项 | 结果 | 说明 |
| --- | --- | --- |
| Controller 路径 | **未变** | 除 `XunFeiController` 新增 `@ExceptionHandler` 外，所有 `@RequestMapping`/`@GetMapping` 路径保持不变 |
| 请求参数 | **未变** | Controller 方法仍从 `@RequestBody Map`/`HttpSession` 读取原字段 |
| 成功响应结构 | **未变** | `Neo4jService` 返回类型、`WeChatReplyService` 公共方法返回类型、Controller 返回值均保留 |
| 失败响应 | **新增明确错误** | `/url` 未配置密钥时返回 503 `{"error": "..."}`；这是可选功能的新失败行为 |
| `Neo4jService` 公共方法 | **全部保留** | `addEntity`、`matchEntity`、`countInDegree`、`countOutDegree`、`countRelation`、`deleteEntity`、`updateEntity`、`addRelation`、`matchRelation`、`deleteRelation`、`updateRelation`、`deleteAllRelation`、`allEntity`、`allRelation` |
| `WeChatReplyService` 公共方法 | **全部保留** | `addSearch`、`getApart`、`apart`、`getFuzzyWords`、`fuzzyWords`、`countNum`、`interrogativeQue`、`queOne`、`que`、`queTwo`、`queThree`、`isLatestWeek`、`similar`、`hotSearch` |
| 构造函数 | **新增** | `Neo4jService(Driver)`、`WeChatReplyService(Mapper, Driver, String)`、`UrlService(...)`、`WebLoginService(...)`；由 Spring 注入 |
| Vue 业务 API 路径 | **未变** | 仍为 `/api/...`；`vue.config.js` 的 `/api` → 空重写保留 |
| 小程序接口路径 | **有意调整** | 从 `/api/xxx` 改为 `/xxx`，因为后端 Controller 使用根路径；请求方法、参数、响应处理未变 |
| 图谱页面入口 | **保留** | `/neo4j`、实体表“查看”、关系表“查看”入口保留；渲染由 Neovis 改为 vis-network |
| 图谱数据源 | **行为差异** | 由实时 Neo4j 改为 MySQL `entity`/`entity_relation` 镜像；这是为避免浏览器直连 Bolt 的必要调整 |

---

## 7. 是否仍存在远程连接

### 7.1 活动源码/配置扫描

对以下范围做关键词扫描：

- `backend/src`
- `web/src`
- `miniprogram`
- `web/vue.config.js`

扫描项：`47.113.226.5`、`bulaqiu.top`、旧 Neo4j 密码关键字（`<OLD_NEO4J_PASSWORD>`）、旧 MySQL 密码关键字（`<OLD_MYSQL_PASSWORD>`）、`/root/spring_java`。

结果：**活动源码/配置命中数 0**。

对 `backend/target` 同样扫描：**命中数 0**。

### 7.2 仍存在的非活动引用

| 位置 | 内容 | 性质 |
| --- | --- | --- |
| `docs/REMOTE_CONNECTION_AUDIT.md` | 修复前的 `47.113.226.5`、`bulaqiu.top`、行号 | 历史审计证据；顶部已标注“修复前状态” |
| `docs/MIGRATION_REPORT.md` | 旧 HanLP Linux 路径、旧小程序域名 | 迁移历史记录，不是活动配置 |
| `database/数据库.sql:7` | `Source Host: 47.113.226.5:3306` | Navicat 导出注释，不是连接配置 |
| `web/src/utils/neovis*.js` / `.map` | 默认 `neo4jUser` / `neo4jPassword` / `neo4jUri` 均已置空；仅剩 `bolt://localhost` 文档注释 | 文件已无任何 import，属于未使用的 vendored 库；不发起连接 |
| `web/src/components/page/Upload.vue:18` | `jsonplaceholder.typicode.com` 示例上传地址 | 未挂载组件，非活动连接 |

> 结论：**活动代码已无原作者服务器连接**；历史文档/备份中的旧地址和旧凭据不代表当前运行配置，但需要按第 8 节处理。

---

## 8. 是否存在残留凭据

### 8.1 活动源码

- MySQL 密码：改为 `${DB_PASSWORD}`，源码无真实值。
- Neo4j 密码：`Neo4jConfig` 从 `${NEO4J_PASSWORD}` 读取，源码无真实值。
- 讯飞：`XFYUN_API_KEY` / `XFYUN_API_SECRET`，源码无真实值。
- 榛子云：`ZHENZI_SMS_APP_ID` / `ZHENZI_SMS_APP_SECRET`，源码无真实值。
- 前端：无 `server_password` 值；Neovis 默认密码/URI 已置空；`store.js`/`global_data.js` 演示密码已置空。
- 构建产物：`target/classes` 已重新生成，无旧密码。

### 8.2 仍需人工处理的敏感信息

| 凭据/信息 | 处理 |
| --- | --- |
| 旧 MySQL 账号密码 | 已从新源码/文档移除，但历史文档、备份目录、原始项目中可能仍存在；**应由账号所有者轮换** |
| 旧 Neo4j 账号密码 | 同上；旧密码曾在前端 bundle/文档中出现，视为已泄露，**必须轮换** |
| 讯飞 `apiKey/apiSecret` | 已从源码移除；曾提交到项目，**必须轮换** |
| 榛子云 `appId/appSecret` | 已从源码移除；曾提交到项目，**必须轮换** |
| 备份目录 `.smartqa-backup-phase1e-20261022-1957` | 含修改前文件（含旧凭据），位于项目外、未纳入 Git；验证完成后建议删除或严格限制访问 |
| 原始项目 `project/智能问答知识库管理系统` | 按要求未修改，仍保留原作者历史内容；不要公开或提交 |

> 本报告不输出任何真实密码、密钥或 Token。

---

## 9. HanLP 数据是否完整

**不完整。**

| 项目 | 状态 |
| --- | --- |
| `backend/hanlp-data/` | 仅新增 `README.md`，**没有** `data-for-1.7.5` 词典/模型 |
| `hanlp.properties` | `root=./hanlp-data`，可用 `HANLP_ROOT` 覆盖；路径本身 Windows 兼容 |
| `fuzzyWords.txt` | **不存在**；缺失时 `getFuzzyWords()` 返回空列表并告警，跳过模糊词替换 |
| HanLP 核心词典 | **缺失**；`NLPTokenizer.segment()` 会失败，`/apart` 抛出“HanLP 分词不可用”的明确异常 |
| 是否伪造数据 | **否**；未创建任何假词典/假词表 |
| 问答是否恢复 | **不能声称已恢复**；核心数据补齐前，问答分词不可用 |

需要准备：HanLP 1.7.x 完整 `data` 目录、`CustomDictionaryPath` 中列出的自定义词典，以及 `fuzzyWords.txt`（格式见 `backend/hanlp-data/README.md`）。

---

## 10. 尚未解决的问题

1. **HanLP 数据缺失**：未准备 `data-for-1.7.5` 和 `fuzzyWords.txt`，问答未实际验证。
2. **未连接数据库**：未验证 MySQL 账号密码、Neo4j 认证、表数据是否完整；未执行任何读写。
3. **未启动后端**：Spring Boot 2.5.6 在 Java 21 下的运行兼容性未验证；只确认 `mvn compile` 成功。
4. **Vue 未构建**：`web/node_modules` 不存在，未执行 `npm install`；`vis-network/standalone` 在 Vue CLI 3/Node 24 下的构建兼容性未验证。
5. **小程序未编译**：未用微信开发者工具打开；真机 `DEVICE_BASE_URL` 仍是占位 `192.168.1.100`；`project.config.json` 的 `urlCheck` 仍为 `true`，本地调试需手动关闭域名校验。
6. **生产小程序域名未配置**：`PRODUCTION_BASE_URL` 为空，发布前必须填 HTTPS 合法域名。
7. **图谱数据语义变化**：图谱现在读取 MySQL `entity`/`entity_relation` 镜像，不再实时查询 Neo4j；如果 MySQL 未同步，图谱可能为空。原“实时 Neo4j 图谱”能力未被完全等价保留。
8. **未使用的 Neovis 文件**：`web/src/utils/neovis*.js` 仍保留，但已无 import；`package.json` 仍有 `neovis.js` 依赖，未清理。
9. **管理员种子数据**：`database/数据库.sql` 无 `INSERT`；`web_user` 若为空，网页登录仍会失败（需确认已另行导入）。
10. **历史文档/备份旧凭据**：`docs/REMOTE_CONNECTION_AUDIT.md`、`docs/MIGRATION_REPORT.md`、数据库 SQL 注释、备份目录仍含旧地址/旧凭据痕迹；需轮换并限制访问。
11. **`/test` 接口无鉴权**：`Test.java` 的 `/test` 仍可触发 `allRelation()`（现在只连本地 Neo4j），属于既有安全风险，本阶段未删除。
12. **控制器普遍无鉴权**：既有问题，未在本阶段处理。
13. **`pom.xml` 仍声明 `<java.version>11</java.version>`**：当前用 Java 21 编译成功，但未升级 Spring Boot/依赖；运行期兼容性未验证。

---

## 11. Spring Boot 启动前置条件

启动前必须满足：

1. **MySQL 运行**：`127.0.0.1:3306/smartques` 可访问；
2. **设置 `DB_PASSWORD`**（必填），可选 `DB_USER`（默认 root）；
3. **Neo4j 运行**：`bolt://127.0.0.1:7687`，认证模式与密码明确；
4. **设置 `NEO4J_PASSWORD`**（必填），可选 `NEO4J_URI` / `NEO4J_USER`；
5. **`backend/target` 已按新源码构建**（本次已重新生成）；
6. **HanLP 数据**：若要验证 `/apart`，需准备 `backend/hanlp-data/data` 与 `fuzzyWords.txt`；不准备时后端可启动，但问答会明确失败；
7. **不要设置任何指向原作者服务器的环境变量**；
8. 首次启动建议在无外网/屏蔽旧 IP 的环境下进行，确认没有意外出站连接。

> 当前环境未设置 `DB_PASSWORD` / `NEO4J_PASSWORD`，因此**现在直接启动会在配置校验阶段失败**，这是预期的安全行为，不是代码缺陷。

---

## 12. Vue 启动前置条件

1. 后端已在 `http://127.0.0.1:8081` 启动；
2. 在 `web/` 下执行 `npm install`（本次未安装，不能声称构建通过）；
3. 确认 Node 24 与 Vue CLI 3 / `vis-network` 的兼容性；若不兼容，需在后续阶段评估，不得擅自升级 Vue；
4. `8080` 端口可用；
5. `vue.config.js` 的 `/api` 代理指向 `http://127.0.0.1:8081`；
6. 登录需要 `web_user` 表中有账号；
7. 图谱页面需要 MySQL `entity`/`entity_relation` 有数据；无数据时显示空状态，不报“功能正常”。

---

## 13. 微信小程序启动前置条件

1. 用微信开发者工具打开 `miniprogram/`（未修改 appid）；
2. 后端在 `http://127.0.0.1:8081` 启动；
3. 开发者工具：`miniprogram/utils/config.js` 的 `DEVTOOLS_BASE_URL` 默认 `http://127.0.0.1:8081`；需在开发者工具中关闭“不校验合法域名、web-view、TLS 及 HTTPS 证书”；
4. 真机：把 `DEVICE_BASE_URL` 改成电脑局域网 IP（当前占位 `192.168.1.100`），手机与电脑同网，防火墙放行 8081；
5. 发布：在 `PRODUCTION_BASE_URL` 填 HTTPS 合法域名，并在微信后台配置 request 合法域名；不得使用原作者域名；
6. 小程序请求已去掉 `/api` 前缀，直接匹配后端根路径；
7. 登录/注册需要 `wechat_users` 表可写；本阶段未做数据库写入测试。

---

## 14. 最终结论

| 问题 | 结论 |
| --- | --- |
| **P0 问题是否全部解决？** | **活动源码/配置层面：是。** 旧 IP/域名/明文密码/Linux 路径在 `backend/src`、`web/src`、`miniprogram`、`target` 中命中数为 0。历史文档/备份仍保留旧地址/旧凭据痕迹，不属于活动连接，但需轮换/清理。 |
| **MySQL 是否完全本地化？** | **配置与构建产物：是。** `application.yml` 和 `target/classes/application.yml` 均指向 `127.0.0.1:3306/smartques`，密码只来自 `DB_PASSWORD`。**未连接验证。** |
| **Neo4j 是否完全本地化？** | **代码/配置：是。** 唯一 `Driver` 默认 `bolt://127.0.0.1:7687`，全部调用复用；无旧地址/密码。**未连接验证；缺 `NEO4J_PASSWORD` 时按设计启动失败。** |
| **Vue 是否完全本地化？** | **代码层面：是。** 代理指向本地 8081，图谱不再直连 Bolt，前端无数据库密码。**未安装依赖、未构建，不能声称运行通过。** |
| **小程序是否完全本地化？** | **源码层面：是。** 7 个文件 29 处活动 URL 已改为 `baseUrl + '/...'`，`bulaqiu.top` 命中 0。**未编译；真机 IP、域名校验、生产 HTTPS 域名仍需配置。** |
| **原业务接口是否保留？** | **是。** Controller 路径/请求参数/成功响应结构未变；`Neo4jService` 14 个公共方法、`WeChatReplyService` 14 个公共方法均保留。仅新增构造器和 `/url` 未配置时的 503 错误；图谱数据源改为 MySQL 镜像，属于行为差异。 |
| **Java 21 编译是否成功？** | **是。** `mvn -o -DskipTests compile` BUILD SUCCESS（67 个源文件）。`mvn clean` 因离线缺少 `maven-clean-plugin:3.1.0` 失败，已手工删除 `target` 后编译。 |
| **是否能够安全进入 Spring Boot 启动阶段？** | **有条件可以。** 当前活动代码不会再连接原作者服务器；设置 `DB_PASSWORD`、`NEO4J_PASSWORD` 并准备 HanLP 数据后，可进入启动验证。**当前未启动，不能声称运行验证通过。** |

### 14.1 建议的下一步

1. 设置本地 `DB_PASSWORD`、`NEO4J_PASSWORD`（以及需要时的 `NEO4J_USER`），不要写入源码；
2. 补齐 `backend/hanlp-data` 与 `fuzzyWords.txt`，再验证 `/apart`；
3. 在隔离网络下启动 Spring Boot，先验证 `/web_login`、实体/关系只读接口，不做写入型测试；
4. `npm install` 后验证 Vue 构建与三联调；
5. 用微信开发者工具验证小程序，再配置真机 IP/生产 HTTPS 域名；
6. 轮换所有曾暴露的旧凭据，删除或严格限制备份目录访问；
7. 若要恢复“实时 Neo4j 图谱”，后续增加后端只读图数据 API，而不是让浏览器直连 Bolt。

---

## 附录：修改文件清单

**后端（Java/配置）**：`Neo4jConfig.java`（新增）、`Neo4jService.java`、`WeChatReplyService.java`、`EntityManageService.java`、`RelationManageService.java`、`CsvService.java`、`UrlService.java`、`WebLoginService.java`、`XunFeiController.java`、`ExampleApplication.java`、`application.yml`、`hanlp.properties`、`Test.java`（注释）、`backend/hanlp-data/README.md`（新增）。

**Vue**：`vue.config.js`、`GraphView.vue`（新增）、`graphData.js`（新增）、`Neo4j.vue`、`EntityForm.vue`、`RelationForm.vue`、`neovis.js`、`neovis-without-dependencies.js`、对应 `.map`、`store.js`、`api/global_data.js`。

**小程序**：`utils/config.js`（新增）、`home.js`、`history.js`、`login.js`、`enroll.js`、`changepw.js`、`collection.js`、`feedback.js`。

**文档**：`README.md`、`docs/PROJECT_STRUCTURE.md`、`web/README.md`、`docs/REMOTE_CONNECTION_AUDIT.md`（历史状态说明）、本报告。

**未修改**：`pom.xml`、`package.json`、`package-lock.json`、`database/数据库.sql`、MySQL 表结构/数据、Docker、Windows 环境变量、ACL、原始项目。
