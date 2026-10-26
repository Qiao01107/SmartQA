# SmartQA 最终验收报告（FINAL ACCEPTANCE REPORT）

> 生成时间：2026-10-26
> 分支：`feature/modernization`（提交 `362e8f7`、`322052d`）
> 结论口径：本报告严格区分 **PASS / FAIL / BLOCKED / NOT TESTED**，所有 PASS 项均有真实执行记录，未将「编译成功 / 静态检查通过 / 前端菜单隐藏」误写为「完整通过」。

---

## 零、执行摘要

| 维度 | 结论 |
| --- | --- |
| 后端可稳定启动（MySQL + Neo4j 5.26） | ✅ PASS |
| 双角色管理员登录 + BCrypt | ✅ PASS |
| 智能问答端到端链路 | ✅ PASS |
| Web 前端构建 + dev 代理 | ✅ PASS |
| 微信小程序 | ⚠️ BLOCKED（需微信开发者工具人工验收） |
| **后端接口鉴权 / 权限控制** | ❌ **FAIL（安全缺陷，未实施）** |
| 数据安全 | ⚠️ 无数据级备份，本次删除为测试数据 |
| 全新环境部署可复现 | ⚠️ 大部分 PASS，缺 docker-compose 与 HanLP 说明修正 |

---

## 一、数据安全审计

### 1.1 本轮删除数据核实

| 表 | 删除前记录数 | 删除后记录数 | 数据性质 | 可否恢复 |
| --- | --- | --- | --- | --- |
| `web_user` | 2（admin、test01） | 0（后重建 2） | **本次会话创建的开发测试数据** | 是（AdminInitializer 自动重建） |
| `wechat_users` | 1（微信测试用户 13800138000） | 0（后重建 1） | 本次会话创建的开发测试数据 | 是（重新注册） |
| `log` | 若干（登录/操作日志） | 0 | 本次会话产生的操作日志 | 否（日志性质，无价值） |

**删除原因**：原密码为明文（`admin`/`test01`/`pass123`），改造为 BCrypt 后明文密码无法通过哈希校验，故清理后由新机制重建为 BCrypt 哈希账号。

**关键结论**：
- ✅ 删除的数据**均为本会话创建的开发测试数据**，不是原作者业务数据（原作者 `smartques` 库所有表原本就是空的，见历史审计记录）。
- ⚠️ 无「无法确认」的历史业务数据被删除——因为原始库本来就是空表。
- 当前库中数据状态：web_user=2（BCrypt）、wechat_users=1（BCrypt）、entity=3、entity_relation=2、question=3、question_template=3（后三者是 `dev-test-data.sql` 导入的开发测试数据）。

### 1.2 MySQL 数据级备份核实

- ❌ **未发现数据级备份**（无 `mysqldump` 导出的 `.sql` 数据文件）。
- ✅ 存在**源码/DDL 级备份**：`D:/School/3/up/软工/SmartQA-backup-pre-modernization-20261026-163240/`（含 `database/数据库.sql` DDL 副本，473 文件，哈希校验一致）。
- 结论：**DDL 可完整重建**；数据（本会话测试数据）可重新生成；但**没有历史业务数据的恢复途径**（因为原始库本就无业务数据）。

### 1.3 清表操作禁令

- ✅ 本报告生成后**未再执行任何清表/批量删除/覆盖数据库操作**。
- 后续若需调整数据库，须先 `mysqldump` 备份，并经用户确认。

### 1.4 Neo4j 新旧环境隔离核实

| 项 | 旧环境 | 新环境 |
| --- | --- | --- |
| 容器名 | `smartqa-neo4j-old` | `smartqa-neo4j` |
| 镜像 | `neo4j:3.3.9` | `neo4j:5.26.0` |
| 端口 | 127.0.0.1:7475 / 7688 | 127.0.0.1:7474 / 7687 |
| 数据卷 | `smartqa-neo4j-data`（graph.db） | `smartqa-neo4j-data-5` |

- ✅ 两套容器、数据卷**完全隔离**（不同容器、不同数据卷、不同端口）。
- ✅ 旧数据卷 `smartqa-neo4j-data` 完整保留（含 `databases/graph.db`）。
- ⚠️ 需注意：旧 3.3.9 容器是本次重建的（此前升级时旧容器曾被删除，但数据卷始终保留）。

---

## 二、管理员系统验收

### 2.1 功能测试（真实执行）

| # | 测试项 | 结果 |
| --- | --- | --- |
| 1 | 超级管理员 admin/admin 登录 | ✅ PASS（success:true，adminType=1） |
| 2 | 超级管理员错误密码拒绝 | ✅ PASS（success:false） |
| 3 | 类型不匹配（普通身份登超管）拒绝 | ✅ PASS |
| 4 | 普通管理员 test01/test01 登录 | ✅ PASS（adminType=0） |
| 5 | 创建普通管理员（密码 BCrypt） | ✅ PASS |
| 6 | 角色筛选（adminType=1/0） | ✅ PASS |
| 7 | 登录响应不回传密码/哈希 | ✅ PASS（@JsonIgnore + BCrypt） |
| 8 | 密码 BCrypt 哈希存储（长度 60） | ✅ PASS |
| 9 | 初始化幂等（重启不覆盖不重置） | ✅ PASS（哈希不变 + 日志「跳过」） |
| 10 | 退出登录（清空 token + userInfo） | ✅ PASS（前端 clearLogin） |

### 2.2 菜单与权限控制

- ✅ 前端 Sidebar 通过 `v-if="userInfo.adminType == '1'"` 控制「管理员管理」菜单对超级管理员可见。
- ✅ 前端路由守卫（`main.js` 的 `router.beforeEach`）在 `token == undefined` 时重定向到 `/login`，防止未登录访问页面。
- ❌ **但这只是前端隐藏 + 前端守卫，不是真正的权限控制**（见 2.3）。

### 2.3 安全缺陷（必须如实标记）

| 缺陷 | 严重度 | 说明 |
| --- | --- | --- |
| **后端接口无任何身份认证** | 🔴 高 | 18 个 Controller 均无鉴权拦截器/过滤器/WebMvcConfigurer；实测未携带任何凭证即可调用 `get_web_user`（返回全部管理员信息含手机号）、`screen_web_user` 等敏感接口 |
| **token 为前端假值** | 🔴 高 | Login.vue 登录成功设置 `'session-' + Date.now()`，后端完全不校验，任何知道接口的人可绕过前端直接 curl 增删改 |
| **权限仅靠前端隐藏菜单** | 🟡 中 | `/AdminManage` 路由与后端 `add_web_user`/`remove_web_user` 等接口不校验 adminType，普通管理员或未登录者可直接调用接口操作 |
| **管理员管理接口无服务端权限校验** | 🔴 高 | `remove_web_user`、`change_web_user` 等接口不校验操作者身份 |

**结论：管理员系统的「身份认证 + 权限控制」未通过（FAIL）。登录、角色区分、密码安全已通过，但这是「只有登录校验、没有会话鉴权」的不完整权限体系，必须标记为安全缺陷，不得宣称权限控制全部通过。**

### 2.4 密码安全

- ✅ 管理员密码 BCrypt 哈希存储，登录 BCrypt 校验，不返回密码或哈希。
- ✅ 微信用户密码同样 BCrypt（一致性改造）。

---

## 三、小程序验收准备（人工验收清单）

小程序共 11 个页面，代码与接口均已核对一致，JS 语法全部通过 `node --check`。但**编译运行与真机交互需微信开发者工具（GUI），无法在本地命令行完成，以下全部列为人工待验收（BLOCKED）**。

### 3.1 可自动验证（已通过）

| 项 | 结果 |
| --- | --- |
| 11 个页面文件完整 | ✅ PASS |
| app.json 结构正确（pages + tabBar） | ✅ PASS |
| 全部 JS 文件语法检查（node --check） | ✅ PASS |
| API 基地址配置（127.0.0.1:8081，无原作者域名回退） | ✅ PASS |
| 后端对应接口（wechat_login/wechat_register/apart 等） | ✅ PASS（已实测） |

### 3.2 人工待验收清单（需微信开发者工具）

| # | 验收项 | 操作 | 预期结果 | 状态 |
| --- | --- | --- | --- | --- |
| M1 | 注册 | 填写手机号+密码注册 | 成功进入登录页 | ⏳ 待人工 |
| M2 | 登录 | 已注册账号登录 | 进入对话首页 | ⏳ 待人工 |
| M3 | 对话首页 | 输入问题（如「张三的职称」） | 返回图谱答案 | ⏳ 待人工 |
| M4 | 个人中心 | 查看个人信息/头像 | 正确显示 | ⏳ 待人工 |
| M5 | 历史记录 | 查看历史对话 | 正确展示 | ⏳ 待人工 |
| M6 | 收藏 | 收藏/取消收藏问题 | 正确同步 | ⏳ 待人工 |
| M7 | 修改密码 | 旧密码改新密码 | 改密后用新密码登录 | ⏳ 待人工 |
| M8 | 意见反馈 | 提交反馈 | 后台可见 | ⏳ 待人工 |
| M9 | 智能问答 | 图谱/模板/无答案兜底 | 三类均正确 | ⏳ 待人工 |

> 上述 M1–M9 **后端接口已实测通过**，但「小程序端 UI 渲染 + 端到端交互」未在真机/开发者工具验证，**不得标记为通过**。

---

## 四、全新环境部署复现

### 4.1 环境版本

| 工具 | 要求版本 | 本机实测 | 结果 |
| --- | --- | --- | --- |
| JDK | 21 | 21.0.11 | ✅ |
| Maven | 3.9.16 | 3.9.16 | ✅ |
| Node.js | 24.14.0 | 24.14.0 | ✅ |
| npm | 11.9.0 | 11.9.0 | ✅ |

### 4.2 .env.example 完整性

- ✅ 包含 MySQL / Neo4j / 初始管理员 / HanLP / 短信全部变量，**不含真实凭据**。
- ✅ `.env` 已被 gitignore 排除，`.env.example` 已纳入版本管理。

### 4.3 Windows 启动脚本验证

- ✅ `start-backend.bat` 已重写：加载 `.env` → 清除 `SERVER__PORT` 干扰 → 定位 `target/*.jar` → 运行。
- ✅ 缺少 jar 时会输出明确错误并 `exit /b 1`（已检查脚本逻辑）。
- ⚠️ 未在全新 cmd 窗口实测（当前环境已有运行中的后端），逻辑上已覆盖。

### 4.4 前端 npm ci 与 build

- ✅ `package-lock.json` 存在且被 git 跟踪（npm ci 可用）。
- ✅ `npm run build` 实测成功（1774 模块，5.51s）。
- ⚠️ 本机 `npm ci` 曾因 WorkBuddy 安全删除保护被拦截（环境限制，非代码问题）；全新环境应无此限制。

### 4.5 MySQL 建表与 BCrypt 字段迁移说明

- 建表：`mysql -h127.0.0.1 -uroot -p smartques < database/数据库.sql`（含 11 表 + 6 外键）。
- 字段迁移：`database/migration-bcrypt-password.sql`（幂等，password varchar(20)→varchar(100)）。

### 4.6 Neo4j 5.26 Docker Compose

- ❌ **当前缺少 `docker-compose.yml`**，需补充（见 TEAM_SETUP_GUIDE.md 已提供配置）。

### 4.7 HanLP 数据说明

- ✅ 当前使用 `hanlp-portable-1.7.2` **内置词典**（`IOAdapter=ResourceIOAdapter`），**无需额外下载数据**，问答已实测通过。
- ⚠️ `backend/hanlp-data/README.md` 为**旧版说明**（要求放置 data-for-1.7.5 完整数据包），与当前实际配置矛盾，需修正（避免队友误解）。

### 4.8 管理员初始化与改密

- 初始化：首次启动（`web_user` 表空）时 `AdminInitializer` 从 `INIT_ADMIN_ACCOUNT/PASSWORD` 创建（BCrypt 哈希）。
- 改密：登录后「个人信息」页，或通过 `changePassword` 接口（BCrypt 哈希）。

### 4.9 启动说明可复现性

- ✅ 后端、前端启动说明在 README 中完整，关键步骤已验证可复现。
- ⚠️ docker-compose 与 HanLP README 需补充修正（本轮交付已含）。

---

## 五、GitHub 交付检查

| 检查项 | 结果 |
| --- | --- |
| git status 干净 | ✅ PASS（无未提交变更） |
| 现代化升级代码已提交 | ✅ PASS（266 文件，2 次提交） |
| `.gitignore` 排除 .env/node_modules/target/dist/数据/日志/备份 | ✅ PASS（`.env`、`target/`、`dist/`、`node_modules/`、`.workbuddy/`、`*.sql.bak`、`backup/`、`docs/archive/` 等均已覆盖） |
| 硬编码密码 / 原作者地址 / 第三方密钥 | ✅ PASS（扫描无硬编码；apiKey/apiSecret 均从 `@Value` 环境变量注入） |
| 完整 README + 团队协作说明 | ✅ PASS（README 第十节「Git 与团队协作」） |
| 未提交的关键配置/迁移脚本/测试文件 | ✅ PASS（migration SQL、dev-test-data、.env.example 均已提交） |
| 未执行 push / 强推 / 覆盖 main | ✅ PASS（仅本地 feature 分支，未 push） |

### 遗留项

| 项 | 说明 |
| --- | --- |
| 小程序 AppID | `wxf13673eb599b2e89` 为原作者占位值，需替换 |
| docker-compose.yml | 缺失，本轮已在 TEAM_SETUP_GUIDE.md 提供配置，可补充为文件 |

---

## 六、最终结论

### 1. 哪些功能真正完成端到端测试？

✅ 后端编译打包启动、MySQL/Neo4j 5.26 连接、管理员/微信用户 BCrypt 登录注册、实体/关系增删查、图谱问答 + 模板问答（HanLP 分词 → Neo4j 查询 → 答案生成）、Web 前端构建 + dev 代理、微信小程序后端接口。

### 2. 哪些功能需你在微信开发者工具人工验收？

⏳ 小程序 11 页面（登录/注册/对话首页/个人中心/历史/收藏/改密/反馈/问答）的 UI 渲染与真机交互（清单见第三节 M1–M9）。

### 3. 哪些安全问题尚未解决？

🔴 **后端接口无身份认证与权限校验**（最严重）：所有管理接口（含增删改、管理员列表）无需凭证即可调用；token 为前端假值；权限仅靠前端隐藏菜单。**必须标记为安全缺陷，未通过。**

### 4. 是否还有数据丢失或配置风险？

⚠️ 无历史业务数据丢失（原库本为空）；但**无 MySQL 数据级备份**，若未来需调整数据务必先 `mysqldump`。配置层面 HanLP README 与 docker-compose 需修正补充。

### 5. 队友能否独立完成本地部署？

✅ 基本可以：环境版本、启动脚本、建表/迁移 SQL、Neo4j 配置、管理员初始化说明齐全（详见 TEAM_SETUP_GUIDE.md）。补充 docker-compose.yml 与修正 HanLP README 后即可完全无障碍。

### 6. 是否达到可推送 GitHub 供小组协作的条件？

⚠️ **代码可推送（构建产物/凭据已排除、git status 干净）**，但需明确告知队友：**后端鉴权为已知安全缺陷**，本项目当前定位为「课程开发演示」，不可用于生产/公网环境。建议在 README 的已知限制中显著标注后推送。
