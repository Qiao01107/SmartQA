# 双端运行审计报告（DUAL_CLIENT_AUDIT）

> 日期：2026-10-26
> 范围：只读审计，未修改任何业务代码、数据库记录、Neo4j 容器或数据卷。
> 对象：Web 管理端（`web/`）+ 微信小程序用户端（`miniprogram/`）

---

## 0. 结论速览

1. **管理端登录**：`web_login` 成功返回 `WebUsers` JSON；**失败返回 `null`（HTTP 200 + 空响应体）**。这解释了“HTTP 200 但 Response 为空”——即账号密码不匹配或 `web_user` 表为空。
2. **管理员分级**：普通管理员（`adminType='0'`）与超级管理员（`adminType='1'`）共用同一张表 `web_user`、同一个接口 `web_login`，仅通过 `type` 参数匹配 `adminType` 字段区分；前端侧“管理员管理”菜单仅超级管理员可见。
3. **小程序用户端**：登录/注册走**不同接口、不同表**——`wechat_login`/`wechat_register`，读写 `wechat_users` 表（与管理员 `web_user` 完全独立）。
4. **当前运行状态**：审计时后端（8081）与前端（8080）均已停止；仅 MySQL（3306）与 Neo4j（7687）在运行。之前观察到的“200 空响应”与“502 拒绝连接”分别是“登录失败返回 null”与“后端已停止”两种状态。
5. **小程序 API 基地址**：`utils/config.js` 开发者工具默认 `http://127.0.0.1:8081`，与后端 Controller 根路径接口匹配；但 `project.config.json` 的 `urlCheck: true` 会在开发时拦截 HTTP 明文请求，需关闭“校验合法域名”。

---

## 1. Web 管理端：路由、登录与管理员分级

### 1.1 路由表（`web/src/router/index.js`）

| 路径 | 组件 | 标题 |
| --- | --- | --- |
| `/` → `/Dashboard` | Dashboard | 首页 |
| `/UserManage` | UserManage | 用户管理（微信用户） |
| `/AdminManage` | AdminManage | 管理员管理 |
| `/404` `/403` | 404 / 403 | 错误页 |
| `/neo4j` | Neo4j | 数据库图形界面 |
| `/entityForm` | EntityForm | 实体表管理 |
| `/relationForm` | RelationForm | 关系表管理 |
| `/questionForm` | QuestionForm | 问题表管理 |
| `/questionTemplateForm` | QuestionTemplateForm | 问题模板表管理 |
| `/feedbackForm` | FeedbackForm | 用户反馈管理 |
| `/userInformation` | UserInformation | 个人信息修改 |
| `/logs` | Logs | 后台管理系统日志 |
| `/login` | Login | 登录 |
| `/forgetPassword` | ForgetPassword | 忘记密码 |

### 1.2 登录接口与密码验证

- 接口：`POST /web_login`（`WebLoginController.java`），参数 `account`、`password`、`type`（char，`'0'`/`'1'`）。
- 验证：`WebLoginService.login()` → `AdminManageMapper.findAllWebUser()` 遍历 `web_user` 表，**明文比对** `account`、`password`、`type == adminType`。
- 存储：`web_user` 表（`account` 主键、`password` varchar(20) 明文、`adminType` char(1)）。

### 1.3 管理员类型判断（前端）

`Sidebar.vue` 中“管理员管理”菜单的显示条件：
```js
v-if="item.index != 'AdminManage' || (item.index == 'AdminManage' && userInfo.adminType == '1')"
```
- `adminType == '1'`（超级管理员）→ 可见“管理员管理”菜单。
- `adminType == '0'`（普通管理员）→ 不可见该菜单，其余菜单均可访问。

> **关键**：`userInfo.adminType` 来自登录返回的 `WebUsers` 对象的 `adminType` 字段（`store.setUserinfomation(response.data)`）。但 `store.js` 里 `userInfo` 的默认 `admin_type` 是 `'1'`（字段名是 `admin_type`，与后端返回的 `adminType` 字段名**不一致**，见第 6 节）。

### 1.4 登录守卫

`main.js` 的 `router.beforeEach`：`token` 为 `undefined` 且不在 `/login`、`/forgetPassword` 时强制跳 `/login`。登录成功时 `store.commit('setToken', 'asasadad')`（硬编码假 token，仅用于放行守卫）。

---

## 2. 微信小程序：页面、接口与后端对应

### 2.1 页面清单（`app.json` + 页面源码）

| 页面 | 路径 | 用途 | 后端接口 |
| --- | --- | --- | --- |
| 启动页 | `pages/load/load` | 微信授权、跳登录 | 无（仅 `wx.login`/`wx.getUserInfo`） |
| 登录 | `pages/login/login` | 手机号+密码登录 | `POST /wechat_login` |
| 注册 | `pages/enroll/enroll` | 手机号+密码注册 | `POST /wechat_register` |
| 对话首页 | `pages/home/home`（tabBar） | 智能问答、热搜、语音 | `POST /apart`、`/hotSearch`、`/add_chatting`、`/get_collectible`、`/add_collectible`、`/remove_collectible`、`GET /url`（讯飞语音） |
| 个人中心 | `pages/user/user`（tabBar） | 入口：历史/收藏/改密/反馈 | 无（页面跳转） |
| 历史记录 | `pages/history/history` | 查看/清空聊天历史 | `POST /wechat_chatting`、`/clearHistory`、`/apart` |
| 简单问答 | `pages/simplequestion/simplequestion` | 展示单条问答 | 无（接收参数） |
| 我的收藏 | `pages/collection/collection` | 查看收藏 | `POST /collectible` |
| 修改密码 | `pages/changepw/changepw` | 修改密码 | `POST /wechat_change_password` |
| 意见反馈 | `pages/feedback/feedback` | 提交/查看反馈 | `POST /wechat_feedback`、`/wechat_feedbackView` |
| 日志页 | `pages/logs/logs` | 本地日志展示 | 无（本地 storage） |

### 2.2 小程序 → 后端接口对照（含返回语义）

| 接口 | Controller | 数据表 | 返回 |
| --- | --- | --- | --- |
| `POST /wechat_login` | WeChatLoginController | `wechat_users` | `int`：0=成功，1=已禁用，2=账号或密码错误 |
| `POST /wechat_register` | WeChatLoginController | `wechat_users` | `boolean`：true 成功 / false 手机号已存在 |
| `POST /wechat_change_password` | WeChatLoginController | `wechat_users` | `boolean` |
| `POST /wechat_information` | WeChatInformationController | `wechat_users` | `List<WeChatUsers>` |
| `POST /apart` | WeChatReplyController | Neo4j + `search_time` | `String`（答案） |
| `POST /hotSearch` | WeChatReplyController | `search_time` | `List`（热搜） |
| `POST /add_chatting` | WeChatChattingController | `chatting` | — |
| `POST /wechat_chatting` | WeChatChattingController | `chatting` | `List`（聊天历史） |
| `POST /clearHistory` | WeChatChattingController | `chatting` | — |
| `POST /collectible` | WeChatCollectibleController | `user_collectible` | `List`（收藏列表） |
| `POST /get_collectible` | WeChatCollectibleController | `user_collectible` | `boolean` |
| `POST /add_collectible` / `/remove_collectible` | WeChatCollectibleController | `user_collectible` | `boolean` |
| `POST /wechat_feedback` | WeChatFeedbackController | `feedback_users` | `boolean` |
| `POST /wechat_feedbackView` | WeChatFeedbackController | `feedback_users` | `List` |
| `GET /url` | XunFeiController | 无（讯飞鉴权） | `String` |

### 2.3 小程序 API 基地址（`utils/config.js`）

- `DEVTOOLS_BASE_URL = 'http://127.0.0.1:8081'`（开发者工具用）
- `DEVICE_BASE_URL = 'http://192.168.1.100:8081'`（真机调试，需改成电脑局域网 IP）
- `PRODUCTION_BASE_URL = ''`（发布，待配置 HTTPS 域名）
- 解析逻辑：`release`→生产域名；`trial`/`develop`+非 devtools 平台→DEVICE；devtools→DEVTOOLS。

**结论**：开发者工具下 API 基地址正确指向本地后端 `http://127.0.0.1:8081`，且接口路径（`/wechat_login` 等）均为后端 Controller 根路径（无 `/api` 前缀），**匹配无误**。

---

## 3. 登录接口“HTTP 200 但 Response 为空”根因

### 3.1 现象与机制

- `WebLoginController.login()` 返回类型为 `WebUsers`（非包装对象）：
  ```java
  public WebUsers login(...) {
      ...
      WebUsers webUser = webLoginService.login(account, password, type);
      if (webUser != null) { logManageService.recordOperation(...); }
      return webUser;   // 失败时为 null
  }
  ```
- 登录失败（账号不存在 / 密码错误 / `web_user` 表为空）时，`webUser == null`，Spring 将 `null` 序列化为**空响应体**，但 HTTP 状态码仍为 **200**。
- 前端 `Login.vue` 的判定 `if (response.data)`：空响应体 → `response.data` 为空 → 走“登录失败”分支。

### 3.2 三种失败状态对照

| 状态 | HTTP 码 | 响应体 | 根因 |
| --- | --- | --- | --- |
| 后端连不上 MySQL | 500 | 错误 JSON（`CannotGetJdbcConnectionException`） | `DB_PASSWORD` 未注入 |
| 登录失败（查不到账号） | **200** | **空** | 账号/密码不匹配，或 `web_user` 表空 |
| 登录成功 | 200 | `WebUsers` JSON | 正常 |

> 因此“HTTP 200 但空”**不是接口异常，而是登录失败的正常业务返回**。结合 `web_user` 表无种子数据，当前登录失败的最可能原因是“查不到账号”（在能连上 MySQL 的前提下）。

### 3.3 用户端与管理端登录的差异

| 维度 | 管理端 | 小程序用户端 |
| --- | --- | --- |
| 登录接口 | `POST /web_login` | `POST /wechat_login` |
| 注册接口 | 无（由超级管理员在管理端 `add_web_user` 添加） | `POST /wechat_register` |
| 数据表 | `web_user` | `wechat_users` |
| 密码验证 | 明文比对 | 明文比对 |
| 登录标识 | `account` + `type`（adminType） | `phone` + `password` |
| 返回 | `WebUsers` 对象（失败 null→空 body） | `int`（0/1/2） |

**两套账号体系完全独立**：管理员的 `account` 与用户的 `phone` 互不相通，存储表不同，接口不同。

---

## 4. 微信开发者工具导入与运行步骤

1. **前置**：确认后端已启动（8081）且能连上 MySQL；`DB_PASSWORD`/`NEO4J_PASSWORD` 已设置。
2. 打开微信开发者工具 → “导入项目”。
3. 目录选择：`D:\School\3\up\软工\SmartQA\miniprogram`。
4. AppID：使用 `project.config.json` 里的 `appid`（`wxe1dea064ccd2a8aa`）或点击“测试号”。
5. 关闭域名校验：右上角「详情」→「本地设置」→ 勾选 **“不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书”**（否则 `urlCheck:true` 会拦截 `http://127.0.0.1:8081`）。
6. 确认 `utils/config.js` 的 `DEVTOOLS_BASE_URL` 为 `http://127.0.0.1:8081`（已正确）。
7. 编译运行，从启动页 → 登录/注册。
8. 真机调试：将 `DEVICE_BASE_URL` 改为电脑局域网 IP，且手机与电脑同一网络、后端监听非仅本机（或用内网穿透）。

---

## 5. 运行小程序所需的数据、AppID、网络与域名条件

### 5.1 数据库数据

| 数据 | 表/库 | 是否必需 | 现状 |
| --- | --- | --- | --- |
| 微信用户账号 | `wechat_users` | 必需（登录/注册） | 无种子数据，需注册或导入 |
| 管理员账号 | `web_user` | 仅管理端 | 无种子数据 |
| 问答内容 | `question` / `question_template` | 影响 `/apart` 问答效果 | 无种子数据 |
| 知识图谱实体/关系 | Neo4j | `/apart` 模糊匹配依赖 | 未确认（容器 3.3.9） |
| 聊天/收藏/反馈 | `chatting`/`user_collectible`/`feedback_users` | 运行时产生 | 空表（正常） |
| HanLP 词典数据 | `backend/hanlp-data/` | `/apart` 分词必需 | **缺失**（仅 README） |

> `/apart` 问答链路依赖 HanLP（分词）+ Neo4j（实体/关系）。HanLP 数据缺失会导致问答失败（这是既有阻断点，与升级无关）。

### 5.2 AppID

- `project.config.json` 中 `appid = "wxe1dea064ccd2a8aa"`（原作者账号的 AppID）。
- **本地运行需换成自己的 AppID**（或使用“测试号”）。原 AppID 属于原作者，本地无法正常使用其云能力/登录鉴权。

### 5.3 网络与域名条件

- 开发者工具：关闭“校验合法域名”后可用 `http://127.0.0.1:8081`（明文 HTTP）。
- 真机：需 `http://局域网IP:8081`，且手机与电脑同一网络；微信真机默认要求 HTTPS，开发阶段需在开发者工具“详情”中关闭校验，或使用合法 HTTPS 域名。
- 发布：需在微信公众平台配置 **HTTPS 合法 request 域名**，并把 `PRODUCTION_BASE_URL` 填为该域名（当前为空）。

---

## 6. 发现的问题与未解决项

1. **字段名不一致（前端潜在 bug）**：后端 `WebUsers.adminType`（驼峰）序列化为 JSON 字段 `adminType`；而 `store.js` 默认 `userInfo.admin_type`（下划线）。`Sidebar.vue` 用的是 `userInfo.adminType`，登录成功后会用后端返回覆盖为 `adminType`，故登录后菜单判断正常；但**未登录时的默认值字段名错位**（`admin_type` vs `adminType`），仅影响初始化展示，不影响登录流程。
2. **无管理员/用户种子数据**：`database/数据库.sql` 仅建表无 INSERT，导致管理端登录必然失败、用户端需先注册。
3. **`web_user.password` / `wechat_users.password` 明文存储+明文比对**，且登录接口无鉴权、返回整个对象（含密码字段），存在安全与信息泄露隐患。
4. **`Test.java` Controller** 暴露 `/test` 接口并调用 `neo4jService.allRelation()`，属遗留测试代码，建议后续清理。
5. **HanLP 数据缺失**：`/apart` 问答链路阻断，需补 `hanlp-data/` 词典与 `fuzzyWords.txt`。
6. **前端假 token**：`store.commit('setToken', 'asasadad')` 硬编码，路由守卫形同虚设（仅依赖 `token !== undefined`）。
7. **当前后端/前端均已停止**：审计时 8081、8080 均无监听，需重新启动才能继续联调。

---

## 7. 审计时运行状态快照

| 组件 | 端口 | 状态 |
| --- | --- | --- |
| 后端 Spring Boot | 8081 | ❌ 未运行（审计时） |
| 前端 Vite | 8080 | ❌ 未运行 |
| MySQL | 3306 | ✅ 运行（PID 7932） |
| Neo4j | 7687 | ✅ 运行（PID 22484） |

---

## 8. 证据清单

| 证据 | 位置 |
| --- | --- |
| 管理端路由表 | `web/src/router/index.js` |
| 管理端登录接口 | `backend/.../controller/WebLoginController.java:25-36` |
| 管理员分级判断 | `web/src/components/common/Sidebar.vue:35` |
| 登录守卫 | `web/src/main.js:41-47` |
| 管理员明文比对 | `backend/.../service/WebLoginService.java:35-44` |
| 小程序页面注册 | `miniprogram/app.json` |
| 小程序 API 基地址 | `miniprogram/utils/config.js` |
| 小程序登录/注册 | `miniprogram/pages/login/login.js`、`enroll/enroll.js` |
| 微信登录接口/返回码 | `backend/.../controller/WeChatLoginController.java`、`service/WeChatLoginService.java:18-28` |
| 微信用户表 | `database/数据库.sql:156-168`、`mapper/WeChatLoginMapper.xml` |
| 问答接口 | `backend/.../controller/WeChatReplyController.java:22-35` |
| AppID | `miniprogram/project.config.json:51` |

---

## 9. 下一步（待确认）

1. 是否重启后端（需 `DB_PASSWORD`/`NEO4J_PASSWORD`）并继续联调？
2. 是否初始化管理员账号与微信用户种子数据？（账号/密码由你指定）
3. 是否补齐 HanLP 数据以打通 `/apart` 问答链路？
4. AppID 使用你自己的还是测试号？

本轮未修改任何代码、数据库或容器，等待确认。
