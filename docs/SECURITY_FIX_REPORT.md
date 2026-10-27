# SmartQA Phase Security-1：后端鉴权与权限控制修复报告

> 分支：`feature/modernization`
> 修复前基线：提交 `427162c`，回退标签 **`pre-security-1`**（`git checkout pre-security-1` 可完整回到改造前状态）
> 范围：只做认证 / 授权专项修复，不重构业务逻辑、不改数据库表结构、不升级其它依赖。
> 结论口径：本报告严格区分 **PASS / FAIL / BLOCKED / NOT TESTED**。所有 PASS 均有真实 HTTP 执行记录（见 `SECURITY_TEST_REPORT.md`），未把「编译通过 / 前端隐藏菜单」写成「鉴权通过」。

---

## 一、修复前的真实缺陷（逐条核实）

| 编号 | 缺陷 | 实测证据（改造前） | 严重级别 |
| --- | --- | --- | --- |
| 1 | 18 个 Controller 无任何拦截器 / 过滤器 / `WebMvcConfigurer` | 全仓库搜索无 `WebMvcConfigurer`、无 `HandlerInterceptor` | 严重 |
| 2 | 未登录即可读取管理员列表 | `POST /get_web_user` 无凭证返回全部管理员姓名、账号、手机号 | 严重 |
| 3 | 权限只靠前端 `v-if` 隐藏菜单 | `Sidebar.vue` 里 `item.index != 'AdminManage' || adminType == '1'`，后端无对应校验 | 严重 |
| 4 | 前端 token 是伪造值 | `Login.vue`：`store.commit('setToken', 'session-' + Date.now())` | 严重 |
| 5 | 小程序接口未认证且可跨用户读取 | `WeChatChattingController` 直接使用请求体 `phone` 查数据 | 严重 |
| 6 | 初始凭据过弱 | `AdminInitializer` 默认 `admin`/`admin` | 高 |
| 7 | 前端路由守卫被当作权限边界 | `router.beforeEach` 只判断 `token == undefined` | 高 |

### 本次审计额外发现并一并修复的高危问题

| 编号 | 问题 | 说明 |
| --- | --- | --- |
| A | **`/change_password` 无需登录即可修改任意账号密码** | 原实现只接收 `account + newPassword`，没有任何身份校验。**这等于任何人可以重置超级管理员密码，整套权限体系形同虚设。** 已改为：必须登录 + 只能改自己 + 改完吊销旧令牌。 |
| B | **`/forget_password` 把短信验证码明文返回给浏览器** | 原实现 `return templateParams[0]`，验证码由前端比对。任何知道「账号 + 手机号」的人都能拿到验证码。已改为服务端生成、服务端校验、响应中永不返回。 |
| C | **`/insist_remove_entity` 用请求体里的 `account` 去库里查 `adminType`** | 普通管理员只要在请求体里填超管账号，就能绕过「关系数 > 5 不允许强制删除」的限制。已改为使用令牌中的角色声明 `isSuperAdmin()`。 |
| D | 操作日志的操作人取自请求体 `account` / `operatorAccount` | 可以伪造他人身份写审计日志。已全部改为取自令牌。 |
| E | `/import_data` 允许传入任意服务器文件路径 | 属于「任意文件读取 + 批量写库」能力，已收紧为**仅超级管理员**（仍保留路径参数，见「残留风险」）。 |
| F | 遗留测试接口 `/test` | 会触发 `neo4jService.allRelation()`，已整体删除 `Test.java`。 |
| G | `get_feedback` 映射写成 `value = "get_feedback"`（无前导斜杠） | 依赖 Spring 容错，已随权限矩阵统一登记为 `/get_feedback`。 |

---

## 二、方案选型：为什么用 JWT Bearer 而不是 Session/Cookie

| 维度 | JWT Bearer（采用） | Session + Cookie |
| --- | --- | --- |
| 小程序兼容 | `wx.request` 直接加一个请求头即可 | 小程序没有浏览器 Cookie 语义，需自行模拟，容易出错 |
| 服务端状态 | 无状态，天然适配前后端分离 | 需要 Session 存储与粘性会话 |
| CSRF | 不适用（浏览器不会自动附带 `Authorization` 头） | 必须引入 CSRF Token，且要处理 SameSite |
| 退出登录 | 需要额外的吊销表（本项目已实现进程内 jti 吊销） | 天然支持（删 Session 即可） |

**结论**：本项目同时要服务 Vue 管理端与微信小程序，JWT 在两端一致、实现成本最低；代价是「退出即时失效」需要额外机制，本项目用 `TokenRevocationService` 补齐，并在下文如实说明它的边界。

### 2.1 CSRF 的处置（不是"为了过测试而关闭"）

- 认证凭证**只**通过 `Authorization: Bearer <jwt>` 传递，服务端 `SessionCreationPolicy.STATELESS`，不签发、不读取任何 Cookie。
- 浏览器不会在跨站请求中自动添加自定义头，因此**不存在**基于 Cookie 自动携带的 CSRF 攻击面，`csrf().disable()` 是与该凭证传递方式匹配的配置。
- 若将来改为 Cookie 承载令牌，**必须**同时开启 CSRF 并配置 `SameSite=Lax/Strict`——这一点已写进 `SecurityConfig` 类注释，防止后人误改。

### 2.2 退出登录的真实语义（不夸大）

| 动作 | 服务端效果 | 边界 |
| --- | --- | --- |
| 前端删除 localStorage 中的 token | **无任何效果**，令牌在有效期内仍可用 | 这就是"删前端 token 不等于下线" |
| 调用 `POST /web_logout`（或 `/wechat_logout`） | 把当前令牌的 `jti` 写入吊销表，**该令牌立即 401** | 吊销表在**单实例内存**中；重启或多实例部署时，该记录丢失，令牌会恢复到"仅靠有效期约束"的状态 |
| 修改密码（`/change_password`、`/wechat_change_password`、`/change_self_info` 带密码） | 记录 `身份 → 吊销时间点`，该账号**此前签发的所有令牌**立即失效 | 同上，进程重启后失效记录丢失 |
| 令牌自然到期 | 默认 **30 分钟**（`JWT_TTL_MINUTES`，上限 120） | 本项目**未实现刷新令牌**，到期需重新登录 |

---

## 三、认证链路（实现细节）

```
① 登录
   POST /web_login  {account, password, type}
     └─ WebLoginService.login()  → BCrypt matches()
     └─ Roles.fromAdminType(adminType)  → ROLE_SUPER_ADMIN / ROLE_ADMIN
     └─ JwtService.newPrincipal() + issue()  → HS256 签名
   200 {success:true, token, tokenType:"Bearer", expiresIn, role, user}   ← user 不含 password
   401 {success:false, code:"INVALID_CREDENTIALS", message:"账号或密码错误"}

② 携带
   前端 axios 拦截器：Authorization: Bearer <token>
   小程序 request 封装：Authorization: Bearer <token>

③ 校验（JwtAuthenticationFilter，每个请求）
   解析 → 验签（HS256）→ 验 issuer → 验 exp（允许 30s 时钟偏移）
        → 验 jti 是否被吊销 → 验 iat 是否早于"改密吊销时间点"
   ├─ 通过：SecurityContext 放入 AuthPrincipal，权限 = 令牌中的 role claim
   └─ 失败：不抛异常、不写 SecurityContext → 由授权规则统一返回 401

④ 授权（SecurityConfig）
   超管专属 / 管理员 / 小程序用户 / 共享已认证 / 匿名 —— 见 API_PERMISSION_MATRIX.md
   未登记路径 → denyAll()

⑤ 统一错误
   未认证 → 401 {code:"UNAUTHORIZED|TOKEN_EXPIRED|TOKEN_REVOKED|TOKEN_INVALID"}
   无权限 → 403 {code:"FORBIDDEN", message:"当前角色无权访问该接口"}
```

关键实现点：

- **密钥不硬编码**：`JWT_SECRET` 只从环境变量读取；不足 32 字节直接**启动失败**（fail-closed）；完全未配置时生成一次性随机密钥并打 WARN（重启即全部失效），绝不回落成固定默认值。
- **角色不可篡改**：角色来自签名后的 `role` claim，请求体里的 `adminType` 不参与任何授权判断。
- **身份不从请求体取**：所有"本人"操作（改资料、改密码、聊天、收藏、反馈、个人信息）都用 `AuthContext.identity()`；请求体里的 `account`/`phone` 只用于一致性校验，不一致直接 403（`AuthContext.requireSelf()`）。

---

## 四、角色模型

| 数据库字段 | 角色 | 说明 |
| --- | --- | --- |
| `web_user.adminType = '1'` | `ROLE_SUPER_ADMIN` **+** `ROLE_ADMIN` | 超管同时持有普通管理员权限，"普通管理员能做的超管也能做" |
| `web_user.adminType = '0'` | `ROLE_ADMIN` | 不能触达任何超管专属接口 |
| `wechat_users`（任意） | `ROLE_WECHAT_USER` | 只能访问小程序私有接口，且仅限自己的数据 |
| 非法 `adminType` | 拒绝签发令牌（403 `INVALID_ROLE`） | 防止脏数据导致权限不确定 |

---

## 五、逐项修复清单

### 5.1 后端新增文件

| 文件 | 作用 |
| --- | --- |
| `security/SecurityConfig.java` | `SecurityFilterChain`、密码编码器、CORS、默认拒绝策略（本项目权限的唯一事实来源） |
| `security/JwtService.java` | 签发/校验 JWT，密钥来源与长度校验 |
| `security/JwtAuthenticationFilter.java` | Bearer 令牌认证过滤器 |
| `security/AuthPrincipal.java` | 认证主体（不含任何密码字段） |
| `security/AuthContext.java` | 控制器获取当前登录者的唯一入口，含 `requireSelf()` 防越权 |
| `security/Roles.java` | `adminType → ROLE_*` 唯一映射 |
| `security/TokenRevocationService.java` | jti 吊销表 + 按身份批量吊销 |
| `security/RestAuthenticationEntryPoint.java` | 401 JSON |
| `security/RestAccessDeniedHandler.java` | 403 JSON |
| `security/PasswordPolicy.java` | 管理员密码强度策略 |
| `service/PasswordResetService.java` | 服务端验证码流程（5 分钟、5 次、一次性） |
| `tools/Payload.java` | 请求体取值工具（消除历史 `map.get().toString()` 的 NPE → 500） |
| `src/test/java/.../security/ApiSecurityIntegrationTest.java` | 9 个真实 HTTP 鉴权集成测试 |

### 5.2 后端修改文件

| 文件 | 改动 |
| --- | --- |
| `pom.xml` | 新增 `spring-boot-starter-security`、`jjwt 0.12.6`（11 个 jar，版本由 Boot BOM 管理） |
| `application.yml` | `server.error.include-message: always`；排除 `UserDetailsServiceAutoConfiguration`（去掉无用的随机默认口令） |
| `config/PasswordEncoderUtil.java` | 改为注入统一的 `PasswordEncoder` Bean（BCrypt 强度 10） |
| `config/AdminInitializer.java` | 去掉 `admin/admin` 默认值；密码不满足策略时**只报 ERROR、不创建**（fail-closed） |
| `controller/WebLoginController.java` | 重写：签发 JWT、401 明确响应、`/web_logout`、`/reset_password`、`/change_password` 收紧为本人 |
| `service/WebLoginService.java` | 移除短信逻辑（迁到 `PasswordResetService`），新增 `findByAccount` |
| `controller/WeChatLoginController.java` | 重写：登录签发 JWT、注册/改密结构化响应、`/wechat_logout` |
| `service/WeChatLoginService.java` | 新增 `findByPhone` |
| `controller/AdminManageController.java` | 操作人取自令牌；`change_self_info` 只能改自己；改密后吊销旧令牌 |
| `service/AdminManageService.java` | 新建走密码策略；禁止删自己 / 删或降级最后一个超管；外键失败返回 false |
| `service/EntityManageService.java` | `insistRemoveEntity(boolean superAdmin, int id)`：角色判断改用令牌 |
| `controller/EntityManageController.java` | 操作人 / 角色取自令牌；参数空值防护 |
| `controller/WeChatChattingController.java`<br>`WeChatCollectibleController.java`<br>`WeChatFeedbackController.java`<br>`WeChatInformationController.java` | 手机号一律 `AuthContext.requireSelf(map.get("phone"))`，改 phone 越权直接 403 |
| `controller/HomePageController.java`<br>`QuestionManageController.java`<br>`RelationManageController.java`<br>`TemplateManageController.java`<br>`FeedbackManageController.java`<br>`WeChatManageController.java` | 操作日志的 `account` 统一改为 `AuthContext.identity()` |
| `controller/Test.java` | **删除**（遗留测试接口） |
| `dao/AdminManageMapper.java` + `mapper/AdminManageMapper.xml` | 新增 `countSuperAdmin` |

### 5.3 前端修改文件

| 文件 | 改动 |
| --- | --- |
| `src/utils/http.js`（新增） | 统一 axios 实例：自动加 `Authorization`、401 清登录态并跳登录、403 提示"无权访问"、登录接口 401 不跳转 |
| `src/main.js` | `$axios` 换成上述实例；路由守卫加角色判断（仅体验层） |
| `src/store.js` | token/userInfo 持久化到 localStorage；`setUserinfomation` 强制剔除 `password`；`clearLogin` 同时清存储 |
| `src/components/page/Login.vue` | 删除 `'session-' + Date.now()` 假 token，改存后端 `body.token`；401 显示后端 message |
| `src/components/common/Header.vue` | 退出登录先调 `/api/web_logout` 吊销令牌，再清本地 |
| `src/components/common/Sidebar.vue` | 菜单改为按 `roles` 配置过滤（并注明这只是体验优化，不是安全边界） |
| `src/router/index.js` | `/AdminManage` 加 `meta.roles: ['1']` |
| `src/components/page/ForgetPassword.vue` | 重写为「发送验证码 → 服务端校验 → 重置」，不再前端比对验证码 |
| `src/components/page/UserInformation.vue` | 处理 `success / passwordChanged`；改密后强制重新登录；不把密码写入 store |
| `src/components/page/AdminManage.vue` | 删除请求体 `operatorAccount`；删除失败与新增失败给出具体原因；密码输入加策略提示 |
| `src/utils/request.js`、`src/api/global_data.js` | **删除**（死代码，后者保存假 token，易误导） |

### 5.4 小程序修改文件

| 文件 | 改动 |
| --- | --- |
| `utils/request.js`（新增） | 统一封装：注入 Bearer 令牌、401 清理并回登录页、403 弹窗提示；`logout()` 调后端吊销 |
| `pages/login/login.js` | 适配 `{success, code, token, user}` 新响应；令牌落本地存储；登录页进入时清理旧登录态 |
| `pages/enroll/enroll.js` | 适配 `{success, message}` 注册响应 |
| `pages/changepw/changepw.js` | 适配结构化响应；改密后回登录页（后端已吊销旧令牌） |
| `pages/user/user.js` | 退出登录改为调用 `/wechat_logout` 后再清理本地 |
| `pages/home/home.js`、`collection`、`history`、`feedback` | 29 处 `wx.request(` → 统一 `request(`（业务参数与回调结构不变） |

---

## 六、安全语义与已知边界（必须如实告知）

| 项 | 现状 | 影响 / 建议 |
| --- | --- | --- |
| 令牌吊销表所在位置 | 进程内存（`ConcurrentHashMap`） | 单实例有效；**重启或多实例部署后吊销记录丢失**，未过期令牌重新可用。生产环境应改 Redis 或不透明令牌 |
| 令牌有效期 | 30 分钟，无刷新令牌 | 到期需重新登录；这是刻意用短有效期压缩泄露窗口 |
| 登录失败次数限制 | **未实现** | 存在在线暴力破解风险。建议后续加：按账号/IP 计数 + 锁定（可用 Bucket4j 或 Redis） |
| 密码强度策略 | 仅作用于管理员（≥8 位、含字母数字、弱口令黑名单） | 小程序用户仍是"≥6 位"的既有规则，未在本次擅自提高 |
| 管理员删除 | `log` 表外键 `ON DELETE RESTRICT`，有日志的管理员无法删除，返回 `false` | 数据完整性优先，未做级联删除。如需删除应先归档日志（人工确认后执行） |
| 找回密码 | 短信未配置时 **fail-closed**（不签发验证码） | 当前环境未配置榛子云短信 → 该功能不可用，用户在报告中标记为 BLOCKED |
| `/import_data` | 仅超管，但仍接受服务器文件路径 | 仍属高权限能力，建议后续限定到固定导入目录（`app.import.dir`） |
| CORS | 仅放行 `127.0.0.1:8080` / `localhost:8080`，`allowCredentials=false` | 生产需通过 `CORS_ALLOWED_ORIGINS` 指定前端域名 |
| 密码哈希 | BCrypt（强度 10），`@JsonIgnore` 保证不回传 | 已实测登录响应与用户列表均无 `password` 字段 |

---

## 七、回退方式（三级）

1. **整体回退**：`git checkout pre-security-1`（或用 `git reset --hard pre-security-1`，注意会丢弃本分支后续提交）。
2. **按文件回退**：本报告第 5 节的表格即完整改动清单，可逐文件 `git checkout 427162c -- <path>`。
3. **运行时降级**：不配置 `JWT_SECRET` 也能启动（生成一次性随机密钥 + WARN），但**不要**用这种方式跳过鉴权；如需临时关闭鉴权必须改代码，本项目**不提供**"关闭鉴权"的开关。

数据库层零改动：本轮**没有**任何表结构变更、没有新表、没有删改任何业务数据（仅新增 1 个 SQL 查询 `countSuperAdmin`）。

---

## 八、结论（对应交付问题）

| 问题 | 回答 |
| --- | --- |
| 未登录是否仍能直接调用敏感接口？ | **不能**。79 个接口中 5 个匿名入口（登录/注册/找回密码），其余全部需要令牌；实测未登录访问 `/get_web_user`、`/add_web_user`、`/add_entity`、`/disable_user`、`/change_password` 等均返回 401。 |
| 普通管理员能否绕过菜单调用超级管理员接口？ | **不能**。菜单只是体验优化，后端 `hasRole("SUPER_ADMIN")` 硬拦截；实测 9 个越权用例全部 403。 |
| 微信用户能否访问其他用户的数据？ | **不能**。身份取自令牌，请求体 phone 不一致直接 403；实测聊天记录 / 个人信息 / 收藏 / 反馈 / 清空聊天记录 / 修改密码 **6 类**跨用户用例全部 403（测试报告 F6–F11）。 |
| 真实认证凭证是否已取代假 token？ | **是**。前端 `'session-' + Date.now()` 已删除，现为后端 HS256 签名的 JWT（三段式，实测长度 263 字符）；小程序同样使用该令牌。 |
| Web 和小程序原有业务是否正常？ | **基本正常**。业务回归 11 项中 10 项 PASS；`/apart` 因本机 Docker/Neo4j 未启动返回 500（环境问题，非鉴权回归，已标 BLOCKED）。小程序 GUI 未在微信开发者工具中跑通（标 BLOCKED，附人工验收步骤）。 |
| 是否达到课程项目基本安全交付标准？ | **达到「可演示 + 不可公网」的水平**：认证、授权、越权、令牌校验、密码存储均已落地并有实测证据；但**登录限流、吊销表持久化、HTTPS、Neo4j 依赖验证**未完成，**不建议直接部署到公网**。 |
