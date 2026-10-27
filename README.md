# 智能问答知识库管理系统（SmartQA）

> 一个面向高校/单位的智能问答知识库管理系统，已完成 **现代化升级**（Java 21 + Spring Boot 3.5 + Vue 3 + Vite 7 + Element Plus + Neo4j 5.26）并通过**真实运行验证**。
>
> 详细升级记录见 `docs/MODERNIZATION_REPORT.md`；登录诊断见 `docs/LOGIN_DIAGNOSTIC_REPORT.md`；双端审计见 `docs/DUAL_CLIENT_AUDIT.md`；完整测试结果见 `docs/TEST_REPORT.md`。

---

## 一、项目简介

SmartQA 由三端组成：

- **后端**：Spring Boot 3.5 + MyBatis + MySQL 8 + Neo4j 5.26 + HanLP，提供管理后台与小程序所需的 REST 接口；
- **网页前端（管理后台）**：Vue 3 + Vite 7 + Element Plus + vue-router 4 + vuex 4，负责知识图谱（实体/关系）、问题与模板、反馈、日志、管理员等管理；
- **小程序前端**：微信小程序（原生 WXML/WXSS/JS），面向普通用户提供智能问答、历史记录、收藏、个人信息、修改密码、意见反馈。

知识点以「实体—关系」形式存储在 Neo4j 图数据库；MySQL 保存业务表（用户、问题、反馈、日志等）；HanLP 用于中文分词与模糊匹配。

---

## 二、目录结构

```text
SmartQA/
├── backend/                    # 后端 Spring Boot 3.5 工程
│   ├── src/main/java/com/example/springboot/
│   │   ├── config/             # Neo4jConfig、AdminInitializer（初始管理员）
│   │   ├── controller/         # REST 控制器
│   │   ├── dao/                # MyBatis Mapper 接口
│   │   ├── entity/             # 实体类
│   │   ├── service/            # 业务逻辑（Neo4jService、WeChatReplyService 等）
│   │   └── tools/              # 工具类
│   ├── src/main/resources/     # application.yml、hanlp.properties、mapper/*.xml
│   ├── pom.xml
│   └── target/                 # 构建产物（已 gitignore）
├── web/                        # 网页前端 Vue 3 + Vite 7 工程
│   ├── src/                    # api / assets / components / router / utils
│   ├── package.json
│   ├── vite.config.js          # 端口 8080，/api 代理到 8081
│   └── dist/                   # 构建产物（已 gitignore）
├── miniprogram/                # 微信小程序
│   ├── pages/                  # load/login/enroll/home/user/history/... 共 11 页
│   ├── utils/config.js         # API 基地址（开发/真机/生产分离）
│   ├── app.js / app.json
│   └── project.config.json
├── database/
│   ├── 数据库.sql              # MySQL 建表脚本（DDL，无种子数据）
│   ├── dev-test-data.sql       # 开发测试数据（question/question_template）
│   └── dev-test-graph.cypher    # 开发测试图谱数据（Neo4j Cypher）
├── docs/                       # 各类报告与说明文档
├── .env.example                # 环境变量模板（不含真实凭据）
├── .env                        # 本机真实凭据（已 gitignore）
├── start-backend.bat           # Windows 后端启动脚本
├── .gitignore
└── README.md
```

---

## 三、技术栈（已实际验证）

| 端 | 技术 | 版本 |
| --- | --- | --- |
| 后端 | Java | 21.0.11 |
| 后端 | Maven | 3.9.16 |
| 后端 | Spring Boot | 3.5.16 |
| 后端 | MyBatis Spring Boot Starter | 3.0.5 |
| 后端 | MySQL Connector/J | 9.x（随 Boot 管理） |
| 后端 | Neo4j Java Driver | 5.28.15 |
| 后端 | Neo4j Server（Docker） | 5.26.0 |
| 后端 | HanLP | portable-1.7.2（内置词典） |
| 前端 | Node.js | 24.14.0 |
| 前端 | npm | 11.9.0 |
| 前端 | Vue | 3.5.44 |
| 前端 | Vite | 7.3.7 |
| 前端 | Element Plus | 2.14.7 |
| 前端 | vue-router / vuex / vue-i18n | 4.6.4 / 4.1.0 / 9.14.5 |
| 前端 | vis-network | 9.1.13 |

---

## 四、环境准备

### 4.1 基础环境

- **JDK 21**（`D:\CodeTools\Java\jdk-21.0.11`）
- **Maven 3.9.16**（`D:\CodeTools\Apache\apache-maven-3.9.16`）
- **Node.js 24.14.0 + npm 11.9.0**（`D:\CodeTools\nodejs`）
- **MySQL 8.0**（本地 `127.0.0.1:3306`）
- **Docker**（用于运行 Neo4j 5.26 容器）
- **微信开发者工具**（小程序）

### 4.2 数据库准备

1. **MySQL**：创建数据库并导入建表脚本
   ```bash
   mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS smartques DEFAULT CHARSET utf8mb4;"
   mysql -uroot -p smartques < database/数据库.sql
   ```
2. **Neo4j 5.26（Docker）**：
   ```bash
   docker run -d --name smartqa-neo4j \
     -p 127.0.0.1:7474:7474 -p 127.0.0.1:7687:7687 \
     -e NEO4J_AUTH=neo4j/你的密码 \
     -v smartqa-neo4j-data:/data \
     neo4j:5.26.0
   ```
   > 国内网络可先用镜像加速拉取：`docker pull docker.m.daocloud.io/neo4j:5.26.0 && docker tag docker.m.daocloud.io/neo4j:5.26.0 neo4j:5.26.0`

### 4.3 环境变量配置

复制 `.env.example` 为 `.env`，填入真实值：

```bash
cp .env.example .env
```

关键变量（`.env` 已被 `.gitignore` 排除，**禁止提交 Git**）：

| 变量 | 用途 | 必填 |
| --- | --- | --- |
| `DB_USER` / `DB_PASSWORD` | MySQL 账号/密码 | 是 |
| `NEO4J_URI` / `NEO4J_USER` / `NEO4J_PASSWORD` | Neo4j 连接 | 是 |
| `JWT_SECRET` | 后端鉴权签名密钥，**至少 32 字节**（泄露=可伪造任意登录令牌） | **是（生产必须）** |
| `JWT_TTL_MINUTES` | 令牌有效期（分钟），默认 30，上限 120 | 否 |
| `INIT_ADMIN_ACCOUNT` / `INIT_ADMIN_PASSWORD` | 初始超级管理员（仅表为空时创建，密码需 ≥8 位且含字母数字） | 否 |
| `CORS_ALLOWED_ORIGINS` | 允许跨域的前端源，逗号分隔 | 否 |
| `XFYUN_API_KEY` / `XFYUN_API_SECRET` | 讯飞语音（可选） | 否 |
| `ZHENZI_SMS_APP_ID` / `ZHENZI_SMS_APP_SECRET` | 榛子云短信（可选，未配置时"忘记密码"不可用） | 否 |

> `JWT_SECRET` 生成方式：`openssl rand -base64 48` 或 `python -c "import secrets;print(secrets.token_urlsafe(48))"`。
> 未配置时后端仍能启动，但会打印 WARN 并生成**一次性随机密钥**——进程重启后所有令牌立即失效，仅可用于本地临时调试。

---

## 五、启动指南

### 5.1 后端（端口 8081）

```bash
# 方式一：启动脚本（自动加载 .env，推荐）
start-backend.bat

# 方式二：手动（先导出 .env 中的变量，再运行 jar）
cd backend
java -jar target/springboot-0.0.1-SNAPSHOT.jar --server.port=8081
```

> **初始管理员**：仅当 `web_user` 表为空时，`AdminInitializer` 才会用 `INIT_ADMIN_ACCOUNT`/`INIT_ADMIN_PASSWORD` 创建超级管理员。
> 密码以 **BCrypt 哈希**落库；重复启动不会覆盖已有账号、不会重置密码。
> **不再提供 `admin`/`admin` 这类默认弱口令**：未配置或密码不满足「≥8 位且含字母和数字」时，
> 初始化器只记录 ERROR 日志并跳过创建（fail-closed），请在启动日志中确认结果。

### 5.2 前端（端口 8080）

```bash
cd web
npm install
npm run dev        # 开发模式，/api 代理到 127.0.0.1:8081
# 或
npm run build      # 生产构建，产物在 dist/
```

打开浏览器访问 `http://127.0.0.1:8080`，用超级管理员账号登录。

### 5.3 小程序

1. 用微信开发者工具打开 `miniprogram/`；
2. `miniprogram/utils/config.js` 已按「开发者工具 / 真机 / 生产」分离基地址，默认指向 `http://127.0.0.1:8081`；
3. **注意**：`project.config.json` 中的 `appid` 为原作者占位值，正式发布前请替换为自己的 AppID（见「待外部条件」）。

---

## 六、开发测试数据导入

```bash
# MySQL 问答数据（question / question_template）
mysql -uroot -p smartques < database/dev-test-data.sql

# Neo4j 示例图谱（实体 + 关系，用于问答链路测试）
# 通过 Cypher Shell 或 Neo4j Browser 执行 database/dev-test-graph.cypher
```

> 所有开发测试数据均明确标注为开发用途，请勿导入生产环境。

---

## 七、已验证功能（真实执行记录）

| 功能 | 结果 |
| --- | --- |
| 后端编译 / 打包（Maven） | ✅ PASS |
| 后端启动 + MySQL 连接（HikariPool） | ✅ PASS |
| Neo4j 5.26 bolt 连接 + 读写 | ✅ PASS |
| 超级管理员登录（adminType='1'） | ✅ PASS |
| 普通管理员登录（adminType='0'） | ✅ PASS |
| 登录失败明确响应（HTTP 401 + JSON） | ✅ PASS |
| 管理员增删改查 + 角色筛选 | ✅ PASS |
| 实体增删查（bid 业务主键） | ✅ PASS |
| 关系增删查 | ✅ PASS |
| 图谱问答（HanLP + Neo4j） | ✅ PASS |
| 模板问答（question_template） | ✅ PASS |
| 微信用户注册 / 登录 / 修改密码 | ✅ PASS |
| 前端构建（Vite 7） | ✅ PASS |
| 前端 dev server + /api 代理 | ✅ PASS |
| 小程序 11 页面 JS 语法检查 | ✅ PASS |
| **后端鉴权：未登录访问敏感接口 → 401** | ✅ PASS（72 项端到端用例全通过） |
| **后端鉴权：普通管理员调超管接口 → 403** | ✅ PASS |
| **后端鉴权：小程序用户跨用户读取 → 403** | ✅ PASS |
| **令牌篡改 / 过期 / 退出后重放 → 401** | ✅ PASS |

详见 `docs/TEST_REPORT.md`。

---

## 八、待外部条件验证

以下项目无法在纯本地环境独立完成，需提供相应条件：

1. **微信小程序真机/开发者工具编译运行**：需微信开发者工具（GUI）与微信平台权限；
2. **正式微信 AppID**：当前 `project.config.json` 的 `appid` 为原作者占位值，需替换为你的 AppID；
3. **讯飞语音**（`/url` 接口）：需 `XFYUN_API_KEY` / `XFYUN_API_SECRET`；
4. **榛子云短信**（忘记密码）：需 `ZHENZI_SMS_APP_ID` / `ZHENZI_SMS_APP_SECRET`。

---

## 九、认证与鉴权（Phase Security-1）

> 详细设计见 `docs/SECURITY_FIX_REPORT.md`，接口级权限见 `docs/API_PERMISSION_MATRIX.md`，测试证据见 `docs/SECURITY_TEST_REPORT.md`。

- **统一认证**：Spring Security 6 + 服务端签发的 **JWT（HS256，默认 30 分钟）**，Vue 管理端与微信小程序共用同一套令牌。
- **角色模型**：`adminType='1'` → `ROLE_SUPER_ADMIN`（同时持有 `ROLE_ADMIN`）；`adminType='0'` → `ROLE_ADMIN`；小程序用户 → `ROLE_WECHAT_USER`。
- **默认拒绝**：79 个接口中仅 5 个允许匿名（登录、注册、找回密码），未在 `SecurityConfig` 登记的路径一律 403。
- **401 / 403 语义**：未登录或令牌无效 → 401；已登录但角色不足 → 403，均为 JSON 响应。
- **防越权**：身份只从令牌取；请求体里的 `account` / `phone` 只用于一致性校验，不一致直接 403。
- **退出登录**：调用 `/web_logout` / `/wechat_logout` 吊销当前令牌 jti；修改密码会吊销该账号此前所有令牌。
- **凭证传递**：只用 `Authorization: Bearer`，不使用 Cookie/Session，因此 CSRF 不适用（若改为 Cookie 必须同时开启 CSRF）。

已知边界（不夸大）：

- 吊销表保存在**单实例内存**，重启或多实例部署后失效，此时令牌仅受 30 分钟有效期约束；
- 未实现登录失败次数限制（暴力破解防护）；
- 全链路需部署 HTTPS 后才可对公网开放。

---

## 十、已知限制

- 密码已改为 **BCrypt 哈希**存储（`web_user` / `wechat_users` 均适用），字段已扩容至 `varchar(100)`；接口响应通过 `@JsonIgnore` 不回传密码。
- 管理员**初始凭据不再允许弱口令**：`AdminInitializer` 要求 `INIT_ADMIN_PASSWORD` 至少 8 位且含字母和数字，否则不创建账号（fail-closed）。**既有 `admin` 账号仍使用早期口令，请在「个人信息」页改成强口令。**
- 管理员删除受 `log` 表外键 `ON DELETE RESTRICT` 约束：已产生操作日志的管理员无法删除（接口返回 `false`），属数据完整性保护。
- 找回密码依赖榛子云短信；未配置时服务端 **fail-closed**（不签发验证码），该功能不可用。
- HanLP 使用内置词典（标准分词），未使用感知机大模型（portable 版不含）。
- `fuzzyWords.txt` 模糊词表未提供（问答流程会跳过模糊词替换，不影响主链路）。

---

## 十一、Git 与团队协作

- 分支策略：在 `feature/modernization` 分支提交变更，**不直接覆盖 main，不强制推送**；
- 构建产物与敏感文件已通过 `.gitignore` 排除（`target/`、`dist/`、`node_modules/`、`.env`、日志、备份等）；
- 提交前请确认 `git status` 中不含 `.env`、真实密码或第三方密钥。

```bash
git init
git checkout -b feature/modernization
git add .
git commit -m "chore: 现代化升级 SmartQA（Spring Boot 3.5 + Vue3 + Neo4j 5.26）并修复登录/问答链路"
# 推送到远端（如有）
git remote add origin <你的仓库地址>
git push -u origin feature/modernization
```
