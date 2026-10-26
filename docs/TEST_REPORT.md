# SmartQA 完整测试报告

> 生成时间：2026-10-26
> 说明：本报告所有结论均来自真实执行记录（命令 + 实际返回结果），未将「编译成功 / 静态检查通过」误写为「联调成功」。

---

## 一、测试环境

| 项 | 值 |
| --- | --- |
| Java | 21.0.11（Oracle） |
| Maven | 3.9.16 |
| Spring Boot | 3.5.16 |
| MySQL | 8.0（127.0.0.1:3306，库 smartques） |
| Neo4j | 5.26.0（Docker 容器 smartqa-neo4j，bolt 7687） |
| Node.js | 24.14.0 |
| npm | 11.9.0 |
| OS | Windows 11 |

---

## 二、测试结果汇总

### 后端

| # | 测试项 | 命令/操作 | 预期 | 实际 | 结果 |
| --- | --- | --- | --- | --- | --- |
| 1 | 干净编译 | `mvn clean compile` | 成功 | EXIT 0 | ✅ PASS |
| 2 | 打包 | `mvn clean package -DskipTests` | 成功 | 44.9MB jar | ✅ PASS |
| 3 | 启动 + MySQL 连接 | `java -jar ... --server.port=8081` | Tomcat 8081 + HikariPool | `Started in 2.7s` + `HikariPool Start completed` | ✅ PASS |
| 4 | Neo4j 5.26 连接 | `NEO4J_URI=bolt://127.0.0.1:7687` | 建立 Driver | 日志 `[Neo4j] 使用...bolt://127.0.0.1:7687` | ✅ PASS |
| 5 | 初始超级管理员创建 | 首次启动（表空） | 创建 admin/adminType=1 | `[AdminInit] 已创建...account=admin` | ✅ PASS |

### 登录与权限

| # | 测试项 | 请求 | 预期 | 实际 | 结果 |
| --- | --- | --- | --- | --- | --- |
| 6 | 超级管理员登录 | `POST /web_login {admin/admin/type:1}` | success:true | `{"success":true,"user":{...adminType:"1"}}` | ✅ PASS |
| 7 | 错误密码登录 | `POST /web_login {admin/wrong/type:1}` | success:false | `{"success":false,"message":"账号或密码错误"}` | ✅ PASS |
| 8 | 类型不匹配登录 | `POST /web_login {admin/admin/type:0}` | success:false | `{"success":false}` | ✅ PASS |
| 9 | 密码不回传 | 登录响应体 | 不含 password | 无 password 字段（@JsonIgnore） | ✅ PASS |
| 10 | 创建普通管理员 | `POST /add_web_user {test01/type:0}` | true | `true` | ✅ PASS |
| 11 | 普通管理员登录 | `POST /web_login {test01/type:0}` | adminType:0 | `adminType:"0"` | ✅ PASS |
| 12 | 角色筛选 | `POST /screen_web_user {adminType:1}` | 仅超级 | 仅返回 admin | ✅ PASS |

### Neo4j 知识图谱

| # | 测试项 | 命令/操作 | 预期 | 实际 | 结果 |
| --- | --- | --- | --- | --- | --- |
| 13 | 实体新增 | `POST /add_entity {Person:张三}` | 写入 Neo4j+MySQL | bid 从 1 起，双端一致 | ✅ PASS |
| 14 | 实体查询 | `POST /get_entity` | 返回实体列表 | id=1,2,3 与 bid 一致 | ✅ PASS |
| 15 | 关系新增 | `POST /add_relation {张三-职称->教授}` | 写入 | 双端一致 | ✅ PASS |
| 16 | 关系查询 | `POST /get_relation` | 返回关系 | 含 startEntityId/endEntityId | ✅ PASS |
| 17 | 关系删除 | `POST /remove_relation` | 删除 | 成功 | ✅ PASS |

### 智能问答

| # | 测试项 | 请求 | 预期 | 实际 | 结果 |
| --- | --- | --- | --- | --- | --- |
| 18 | 图谱问答（关系） | `POST /apart {张三的职称}` | 返回答案 | `张三的职称是教授` | ✅ PASS |
| 19 | 图谱问答（带疑问词） | `POST /apart {张三的职称是什么}` | 返回答案 | `张三的职称是教授` | ✅ PASS |
| 20 | 图谱问答（链式） | `POST /apart {张三的所属}` | 返回答案 | `张三的所属是计算机学院` | ✅ PASS |
| 21 | 模板问答 | `POST /apart {计算机由什么组成}` | 返回答案 | `计算机由硬件系统和软件系统组成` | ✅ PASS |
| 22 | 模板问答 | `POST /apart {什么是软件工程}` | 返回答案 | `软件工程是一门研究用工程化方法...` | ✅ PASS |
| 23 | 无答案 | `POST /apart {今天天气怎么样}` | 暂无解答 | `抱歉，暂无解答` | ✅ PASS |

### 微信用户端

| # | 测试项 | 请求 | 预期 | 实际 | 结果 |
| --- | --- | --- | --- | --- | --- |
| 24 | 注册 | `POST /wechat_register` | true | `true` | ✅ PASS |
| 25 | 登录 | `POST /wechat_login` | 0（正常用户） | `0` | ✅ PASS |
| 26 | 重复注册 | `POST /wechat_register` | false | `false` | ✅ PASS |
| 27 | 错误密码登录 | `POST /wechat_login` | 2（失败） | `2` | ✅ PASS |

### 前端（Web 管理后台）

| # | 测试项 | 命令/操作 | 预期 | 实际 | 结果 |
| --- | --- | --- | --- | --- | --- |
| 28 | 生产构建 | `npm run build` | 成功 | ✓ built in 7.09s（1774 模块） | ✅ PASS |
| 29 | dev server | `npm run dev` | 8080 监听 | HTTP 200 | ✅ PASS |
| 30 | /api 代理 | `POST /api/web_login`（经 8080） | 转发到 8081 | 成功返回登录结果 | ✅ PASS |

### 小程序

| # | 测试项 | 命令/操作 | 预期 | 实际 | 结果 |
| --- | --- | --- | --- | --- | --- |
| 31 | JS 语法检查 | `node --check`（全部 11 页） | 通过 | 全部通过 | ✅ PASS |
| 32 | API 基地址 | `utils/config.js` | 指向本地 | `127.0.0.1:8081` | ✅ PASS |
| 33 | app.json 完整性 | 检查 | 11 页 + tabBar | 完整 | ✅ PASS |
| 34 | 开发者工具编译运行 | 需 GUI 环境 | — | **未执行** | ⏸ 待外部条件 |

### 安全扫描

| # | 测试项 | 结果 |
| --- | --- | --- |
| 35 | 旧远程地址扫描 | ✅ 无原作者 IP/域名残留 |
| 36 | 明文密码/密钥扫描 | ✅ 无硬编码（均走环境变量） |
| 37 | .gitignore 覆盖 | ✅ .env/target/dist/node_modules/backup 已排除 |

---

## 三、失败项与修复记录

| 问题 | 根因 | 修复 | 状态 |
| --- | --- | --- | --- |
| 登录失败返回 HTTP 200 空 body | `WebLoginController` 返回 null | 改为统一 `{success,user/message}` 结构 | ✅ 已修复 |
| 登录响应泄露明文密码 | `WebUsers.password` 序列化 | 加 `@JsonIgnore` | ✅ 已修复 |
| 前端硬编码假 token | `Login.vue` `setToken('asasadad')` | 改为 `session-<时间戳>` | ✅ 已修复 |
| store 预置假 adminType | `store.js` 默认 `admin_type:'1'` | 清空为默认值 | ✅ 已修复 |
| Sidebar 权限不响应式 | `data()` 快照 userInfo | 改为 computed | ✅ 已修复 |
| Neo4j `id(n)` 弃用 + id 从 0 起致 MySQL 主键错位 | Neo4j 5.x 弃用 id() | 改用节点属性 `bid` 业务自增 | ✅ 已修复 |
| Cypher `{param}` 旧语法 + 拼接缺空格 | 迁移遗留 | 改 `$param` 并补空格 | ✅ 已修复 |
| HanLP 词典缺失 | `root=./hanlp-data` 指向空目录 | portable 版内置词典，启用 ResourceIOAdapter + `HanLP.segment` | ✅ 已修复 |

---

## 四、待外部条件验证清单

| 项目 | 需要条件 | 由谁提供 |
| --- | --- | --- |
| 小程序开发者工具编译/真机运行 | 微信开发者工具 + 平台权限 | 用户 |
| 正式微信 AppID | 自己的 AppID（当前为原作者占位值） | 用户 |
| 讯飞语音（可选） | XFYUN_API_KEY / XFYUN_API_SECRET | 用户 |
| 榛子云短信（可选） | ZHENZI_SMS_APP_ID / SECRET | 用户 |

---

## 五、结论

- **已真实通过**：后端编译/启动/数据库连接、Neo4j 图谱增删查、管理员登录与双角色权限、图谱问答 + 模板问答完整链路、微信用户注册/登录、前端构建与代理、小程序代码静态检查。
- **未通过/未验证**：小程序 GUI 编译运行（需微信开发者工具）、讯飞语音、榛子云短信（需外部密钥）。
- **需要用户提供**：正式 AppID、讯飞/短信密钥（如需启用）。
