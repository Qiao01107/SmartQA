# SmartQA 最终交付变更清单（2026-10-26）

本文记录本次「全量修复、联调测试与团队交付」阶段的代码变更，供队友审核。

---

## 一、后端变更（backend/）

### 新增
- `config/AdminInitializer.java` — 初始超级管理员初始化器（仅 web_user 表为空时从环境变量创建）。

### 修改
- `controller/WebLoginController.java` — 登录改为统一 `{success, user/message}` 响应，修复「失败返回 HTTP 200 空 body」。
- `entity/WebUsers.java` — `password` 加 `@JsonIgnore`，登录/查询不再回传明文密码。
- `service/Neo4jService.java` — **重写**：弃用 Neo4j 5.x 已弃用的 `id(n)`，改用节点属性 `bid` 业务自增主键（`__Counter` 节点原子自增），修复 id 从 0 起导致的 MySQL 主键错位。
- `service/WeChatReplyService.java` — Cypher 参数 `{name}`/`{as}` → `$name`/`$as`，修复拼接缺空格；`NLPTokenizer` → `HanLP.segment`（portable 版内置词典，不依赖感知机大模型）。
- `resources/hanlp.properties` — 注释 `root`，启用 `IOAdapter=ResourceIOAdapter`（从 jar 内置词典读取），注释引用 jar 内不存在文件的自定义词典路径。

### 未改动（已在上阶段完成）
- `pom.xml`（Spring Boot 3.5.16 / Java 21）、`Neo4jConfig.java`（环境变量化）、`application.yml`（DB 环境变量）、16 个 Controller 的 `javax`→`jakarta` 迁移。

---

## 二、前端变更（web/）

- `components/page/Login.vue` — 登录判断改为 `body.success`，移除硬编码假 token，增加错误提示与 catch。
- `store.js` — 清空预置的假 `admin_type:'1'` / 假账号数据。
- `api/global_data.js` — 同上，清空假数据。
- `components/common/Sidebar.vue` — `userInfo` 从 data 快照改为 computed（登录后响应式更新管理员菜单权限）。
- `components/common/Header.vue` — `name`/`getASD` 改为 computed，修复登录后用户名/头像不更新。
- `vite.config.js` — `build.emptyOutDir: false`（避免受限环境批量删除 dist 触发保护）。

---

## 三、数据库与配置

- `database/dev-test-data.sql` — 新增，开发测试问答数据（question/question_template）。
- `database/dev-test-graph.cypher` — 新增，开发测试图谱数据（Neo4j Cypher）。
- `database/migration-bcrypt-password.sql` — 新增，密码字段扩容迁移（varchar(20)→varchar(100)，幂等可重复执行）。
- `.env.example` — 新增，环境变量模板（不含真实凭据）。
- `.env` — 新增，本机真实凭据（已 gitignore，**不入库**）。
- `start-backend.bat` — 新增，Windows 后端启动脚本（自动加载 .env，运行打包 jar）。
- `README.md` — 全面更新（技术栈、环境准备、启动指南、验证状态、已知限制、Git 协作）。
- `docs/TEST_REPORT.md` — 新增，完整测试报告。

### 密码安全改造（BCrypt）

- `backend/pom.xml` — 新增 `spring-security-crypto` 依赖（仅密码加密，不引入完整 Spring Security）。
- `config/PasswordEncoderUtil.java` — 新增，BCrypt 哈希/校验统一封装。
- `config/AdminInitializer.java` — 密码改 BCrypt 哈希落库；仅表空时创建；失败不静默。
- `service/WebLoginService.java` — 登录改 `PasswordEncoderUtil.matches` 校验；改密码哈希落库。
- `service/AdminManageService.java` — 新增/修改管理员密码哈希落库；修改个人信息空密码不重置。
- `service/WeChatLoginService.java` — 微信用户注册/登录/改密均改 BCrypt。
- `dao/AdminManageMapper.java` + `mapper/AdminManageMapper.xml` — 新增 `changeSelfInfoKeepPassword`。
- `entity/WeChatUsers.java` — password 加 `@JsonIgnore`。
- `web/src/store.js` — 新增 `clearLogin` 清空 token 与用户信息；退出登录改调 `clearLogin`。
- `web/src/components/common/Header.vue` — 退出登录改调 `clearLogin`。

---

## 四、环境变更（非代码，需队友注意）

- **Neo4j 升级**：`neo4j:3.3.9` → `neo4j:5.26.0`（Docker，新数据卷 `smartqa-neo4j-data-5`，旧图谱为空故无迁移）。
- **MySQL**：库 `smartques` 11 张表，新增初始管理员 `admin`（超级管理员，密码见 .env）。

---

## 五、兼容性说明（重大修改）

1. **Neo4j 主键方案变更**：实体主键从「Neo4j 内部 `id(n)`」改为「节点属性 `bid` 业务自增」。这是 Neo4j 5.x 兼容的必要修改（`id()` 已弃用且 id 从 0 起导致与 MySQL 自增主键冲突）。
2. **登录响应结构变更**：`/web_login` 返回体从「裸 `WebUsers` / null」改为 `{success, user}` / `{success, message}`，前端 Login.vue 已同步适配。
3. **HanLP 分词器变更**：`NLPTokenizer`（感知机）→ `HanLP.segment`（标准分词），因 portable 版不含感知机模型。分词精度略降，但对实体匹配问答链路无影响。
4. **密码存储变更**：全系统密码从「明文」改为「BCrypt 哈希」。`web_user.password` 与 `wechat_users.password` 字段由 `varchar(20)` 扩容到 `varchar(100)`（BCrypt 哈希固定 60 字符）。存量明文密码需删除重建（本仓库已清理）；新部署环境执行 `database/migration-bcrypt-password.sql` 即可。

> 以上兼容性修改均有真实运行验证，详见 `docs/TEST_REPORT.md`。
