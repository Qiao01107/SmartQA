# SmartQA 接口权限矩阵（Phase Security-1）

> 生成时间：2026-10-27
> 事实来源：`backend/src/main/java/com/example/springboot/security/SecurityConfig.java`（唯一授权入口）
> 统计：**79 个业务接口**——匿名 5、超管专属 7、管理员（含超管）52、小程序用户 13、共享已认证 2。
> 授权原则：**默认拒绝**。未在下方登记的新接口会被 `anyRequest().denyAll()` 拦截为 403，不会"默认放行"。

## 图例

| 列 | 含义 |
| --- | --- |
| 匿名 | ✅ 允许未登录访问；❌ 必须携带 `Authorization: Bearer <jwt>` |
| 允许角色 | `SUPER`=`ROLE_SUPER_ADMIN`；`ADMIN`=`ROLE_ADMIN`（超管同时持有 ADMIN）；`WECHAT`=`ROLE_WECHAT_USER` |
| 私有数据 | ✅ 涉及个人数据（必须校验归属）；— 不涉及 |
| 需管理员 | ✅ 仅后台管理员可调用 |
| 前端调用 | `web:` 管理端页面 / `mp:` 小程序页面；`—` 表示当前无前端调用（遗留接口） |

---

## 一、匿名可访问（5）

| 方法 | 路径 | 模块 | 匿名 | 允许角色 | 私有数据 | 需管理员 | 前端调用 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/web_login` | 管理端登录 | ✅ | 任意 | — | — | `web: Login.vue` | 成功签发 JWT；失败 401（明确 JSON） |
| POST | `/wechat_login` | 小程序登录 | ✅ | 任意 | — | — | `mp: pages/login` | 成功签发 JWT；禁用账号 403 |
| POST | `/wechat_register` | 小程序注册 | ✅ | 任意 | — | — | `mp: pages/enroll` | 手机号重复返回 `success:false` |
| POST | `/forget_password` | 找回密码 | ✅ | 任意 | ✅ | — | `web: ForgetPassword.vue` | 验证码**只在服务端保存**；短信未配置时 fail-closed |
| POST | `/reset_password` | 找回密码 | ✅ | 任意 | ✅ | — | `web: ForgetPassword.vue` | 校验「账号+手机号+验证码」，5 分钟 / 5 次 / 一次性 |

> 另有 Spring Boot 内置 `/error` 允许匿名访问，用于把异常转成 JSON，不承载业务逻辑。

---

## 二、超级管理员专属（7）

普通管理员直接发 HTTP 请求也会得到 **403**（实测见 `SECURITY_TEST_REPORT.md` C1–C9）。

| 方法 | 路径 | 模块 | 匿名 | 允许角色 | 私有数据 | 需管理员 | 前端调用 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/get_web_user` | 管理员管理 | ❌ | SUPER | ✅ | ✅ | `web: AdminManage.vue` | 分页列出全部管理员 |
| POST | `/count_web_user` | 管理员管理 | ❌ | SUPER | ✅ | ✅ | `web: AdminManage.vue` | 管理员总数 |
| POST | `/screen_web_user` | 管理员管理 | ❌ | SUPER | ✅ | ✅ | `web: AdminManage.vue` | 按 adminType 筛选 |
| POST | `/add_web_user` | 管理员管理 | ❌ | SUPER | ✅ | ✅ | `web: AdminManage.vue` | 受密码策略约束（弱口令返回 false） |
| POST | `/remove_web_user` | 管理员管理 | ❌ | SUPER | ✅ | ✅ | `web: AdminManage.vue` | 禁止删自己 / 最后一个超管 / 有日志者 |
| POST | `/change_web_user` | 管理员管理 | ❌ | SUPER | ✅ | ✅ | `web: AdminManage.vue` | 禁止降级最后一个超管 |
| POST | `/import_data` | 数据导入 | ❌ | SUPER | — | ✅ | — | 按服务器路径批量导入；**高权限能力**，见残留风险 |

---

## 三、管理员可用（含超管，52）

### 3.1 知识图谱·实体（8）

| 方法 | 路径 | 模块 | 匿名 | 允许角色 | 私有数据 | 需管理员 | 前端调用 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/add_entity` | 实体管理 | ❌ | ADMIN | — | ✅ | `web: EntityForm.vue` | 操作人取自令牌 |
| POST | `/remove_entity` | 实体管理 | ❌ | ADMIN | — | ✅ | `web: EntityForm.vue` | 有关系时拒绝并返回关系数 |
| POST | `/insist_remove_entity` | 实体管理 | ❌ | ADMIN | — | ✅ | `web: EntityForm.vue` | 强制删除；>5 条关系时仅 SUPER 可执行（角色取自令牌） |
| POST | `/change_entity` | 实体管理 | ❌ | ADMIN | — | ✅ | `web: EntityForm.vue` | |
| POST | `/search_entity` | 实体管理 | ❌ | ADMIN | — | ✅ | `web: EntityForm.vue` | |
| POST | `/count_entity` | 实体管理 | ❌ | ADMIN | — | ✅ | `web: Dashboard.vue / EntityForm.vue` | |
| POST | `/get_entity` | 实体管理 | ❌ | ADMIN | — | ✅ | `web: EntityForm.vue / utils/graphData.js` | |
| POST | `/get_entity_label` | 实体管理 | ❌ | ADMIN | — | ✅ | `web: RelationForm.vue` | |

### 3.2 知识图谱·关系（7）

| 方法 | 路径 | 模块 | 匿名 | 允许角色 | 私有数据 | 需管理员 | 前端调用 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/add_relation` | 关系管理 | ❌ | ADMIN | — | ✅ | `web: RelationForm.vue` | |
| POST | `/remove_relation` | 关系管理 | ❌ | ADMIN | — | ✅ | `web: RelationForm.vue` | |
| POST | `/change_relation` | 关系管理 | ❌ | ADMIN | — | ✅ | `web: RelationForm.vue` | |
| POST | `/search_relation` | 关系管理 | ❌ | ADMIN | — | ✅ | `web: RelationForm.vue` | |
| POST | `/count_relation` | 关系管理 | ❌ | ADMIN | — | ✅ | `web: Dashboard.vue / RelationForm.vue` | |
| POST | `/get_relation` | 关系管理 | ❌ | ADMIN | — | ✅ | `web: RelationForm.vue / graphData.js` | |
| POST | `/get_relation_label` | 关系管理 | ❌ | ADMIN | — | ✅ | `web: RelationForm.vue` | |

### 3.3 问答·问题（10）

| 方法 | 路径 | 模块 | 匿名 | 允许角色 | 私有数据 | 需管理员 | 前端调用 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/add_question` | 问题管理 | ❌ | ADMIN | — | ✅ | `web: QuestionForm.vue` | |
| POST | `/remove_question` | 问题管理 | ❌ | ADMIN | — | ✅ | `web: QuestionForm.vue` | |
| POST | `/insist_remove_question` | 问题管理 | ❌ | ADMIN | — | ✅ | `web: QuestionForm.vue` | |
| POST | `/change_question` | 问题管理 | ❌ | ADMIN | — | ✅ | `web: QuestionForm.vue` | |
| POST | `/insist_change_question` | 问题管理 | ❌ | ADMIN | — | ✅ | `web: QuestionForm.vue` | |
| POST | `/change_answer` | 问题管理 | ❌ | ADMIN | — | ✅ | `web: QuestionForm.vue` | |
| POST | `/search_question` | 问题管理 | ❌ | ADMIN | — | ✅ | `web: QuestionForm.vue` | |
| POST | `/screen_question` | 问题管理 | ❌ | ADMIN | — | ✅ | `web: QuestionForm.vue` | 按状态筛选 |
| POST | `/count_question` | 问题管理 | ❌ | ADMIN | — | ✅ | `web: QuestionForm.vue` | |
| POST | `/get_question` | 问题管理 | ❌ | ADMIN | — | ✅ | `web: QuestionForm.vue` | |

### 3.4 问答·问题模板（6）

| 方法 | 路径 | 模块 | 匿名 | 允许角色 | 私有数据 | 需管理员 | 前端调用 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/add_template` | 模板管理 | ❌ | ADMIN | — | ✅ | `web: QuestionTemplateForm.vue` | |
| POST | `/remove_template` | 模板管理 | ❌ | ADMIN | — | ✅ | `web: QuestionTemplateForm.vue` | |
| POST | `/change_template` | 模板管理 | ❌ | ADMIN | — | ✅ | `web: QuestionTemplateForm.vue` | |
| POST | `/search_template` | 模板管理 | ❌ | ADMIN | — | ✅ | `web: QuestionTemplateForm.vue` | |
| POST | `/count_template` | 模板管理 | ❌ | ADMIN | — | ✅ | `web: QuestionTemplateForm.vue` | |
| POST | `/get_template` | 模板管理 | ❌ | ADMIN | — | ✅ | `web: QuestionTemplateForm.vue` | |

### 3.5 用户反馈管理（6）

| 方法 | 路径 | 模块 | 匿名 | 允许角色 | 私有数据 | 需管理员 | 前端调用 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/get_feedback` | 反馈管理 | ❌ | ADMIN | ✅ | ✅ | `web: FeedbackForm.vue` | |
| POST | `/screen_feedback` | 反馈管理 | ❌ | ADMIN | ✅ | ✅ | `web: FeedbackForm.vue` | 按处理状态筛选 |
| POST | `/search_feedback` | 反馈管理 | ❌ | ADMIN | ✅ | ✅ | `web: FeedbackForm.vue` | |
| POST | `/count_feedback` | 反馈管理 | ❌ | ADMIN | ✅ | ✅ | `web: Dashboard.vue / FeedbackForm.vue` | |
| POST | `/change_feedback` | 反馈管理 | ❌ | ADMIN | ✅ | ✅ | `web: FeedbackForm.vue` | 标记已处理 |
| POST | `/get_info` | 反馈管理 | ❌ | ADMIN | ✅ | ✅ | `web: FeedbackForm.vue` | 按手机号查小程序用户资料 |

### 3.6 操作日志（3）

| 方法 | 路径 | 模块 | 匿名 | 允许角色 | 私有数据 | 需管理员 | 前端调用 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/search_log` | 日志 | ❌ | ADMIN | ✅ | ✅ | `web: Logs.vue` | |
| POST | `/count_log` | 日志 | ❌ | ADMIN | ✅ | ✅ | `web: Logs.vue` | |
| POST | `/get_log` | 日志 | ❌ | ADMIN | ✅ | ✅ | `web: Logs.vue` | |

### 3.7 小程序用户管理（6）

| 方法 | 路径 | 模块 | 匿名 | 允许角色 | 私有数据 | 需管理员 | 前端调用 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/get_wechat_user` | 小程序用户 | ❌ | ADMIN | ✅ | ✅ | `web: UserManage.vue` | |
| POST | `/count_wechat_user` | 小程序用户 | ❌ | ADMIN | ✅ | ✅ | `web: UserManage.vue / Dashboard.vue` | |
| POST | `/search_user` | 小程序用户 | ❌ | ADMIN | ✅ | ✅ | `web: UserManage.vue` | |
| POST | `/screen_user` | 小程序用户 | ❌ | ADMIN | ✅ | ✅ | `web: UserManage.vue` | 按启用状态筛选 |
| POST | `/disable_user` | 小程序用户 | ❌ | ADMIN | ✅ | ✅ | `web: UserManage.vue` | 禁用后该用户登录返回 403 |
| POST | `/able_user` | 小程序用户 | ❌ | ADMIN | ✅ | ✅ | `web: UserManage.vue` | 解除禁用 |

### 3.8 管理员个人中心（6）

| 方法 | 路径 | 模块 | 匿名 | 允许角色 | 私有数据 | 需管理员 | 前端调用 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/get_admin_info` | 个人中心 | ❌ | ADMIN | ✅ | ✅ | — | 账号取自令牌，传别人账号 → 403 |
| POST | `/last_login` | 个人中心 | ❌ | ADMIN | ✅ | ✅ | `web: Dashboard.vue` | 同上 |
| POST | `/change_head` | 个人中心 | ❌ | ADMIN | ✅ | ✅ | `web: Dashboard.vue` | 同上 |
| POST | `/change_self_info` | 个人中心 | ❌ | ADMIN | ✅ | ✅ | `web: UserInformation.vue` | 只能改自己；带密码则改密并**吊销所有旧令牌** |
| POST | `/change_password` | 个人中心 | ❌ | ADMIN | ✅ | ✅ | — | 只能改自己；弱口令拒绝；改完吊销所有旧令牌 |
| POST | `/web_logout` | 退出登录 | ❌ | ADMIN | — | — | `web: Header.vue` | 吊销当前令牌 jti |

---

## 四、微信小程序用户专属（13）

全部要求 `ROLE_WECHAT_USER`；且**手机号一律取自令牌**，请求体中的 `phone` 只用于一致性校验，不一致直接 403。管理员令牌访问这些接口同样 403（角色不匹配）。

| 方法 | 路径 | 模块 | 匿名 | 允许角色 | 私有数据 | 需管理员 | 前端调用 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/wechat_chatting` | 聊天记录 | ❌ | WECHAT | ✅ | — | `mp: pages/history` | 只能读自己的 |
| POST | `/add_chatting` | 聊天记录 | ❌ | WECHAT | ✅ | — | `mp: pages/home` | 只能写自己的 |
| POST | `/clearHistory` | 聊天记录 | ❌ | WECHAT | ✅ | — | `mp: pages/history` | 只能清自己的 |
| POST | `/collectible` | 收藏 | ❌ | WECHAT | ✅ | — | `mp: pages/collection` | 只能看自己的 |
| POST | `/get_collectible` | 收藏 | ❌ | WECHAT | ✅ | — | `mp: pages/home` | 判断某问答是否已收藏 |
| POST | `/add_collectible` | 收藏 | ❌ | WECHAT | ✅ | — | `mp: pages/home` | |
| POST | `/remove_collectible` | 收藏 | ❌ | WECHAT | ✅ | — | `mp: pages/home` | |
| POST | `/wechat_feedback` | 反馈 | ❌ | WECHAT | ✅ | — | `mp: pages/feedback` | 只能提交自己的 |
| POST | `/wechat_feedbackView` | 反馈 | ❌ | WECHAT | ✅ | — | `mp: pages/feedback` | 只能看自己的 |
| POST | `/wechat_information` | 个人信息 | ❌ | WECHAT | ✅ | — | — | 只能看自己的；响应不含密码 |
| POST | `/wechat_change_password` | 改密 | ❌ | WECHAT | ✅ | — | `mp: pages/changepw` | 校验旧密码；改完吊销所有旧令牌 |
| POST | `/apart` | 智能问答 | ❌ | WECHAT | — | — | `mp: pages/home / history` | 依赖 Neo4j；未启动时 500（环境问题） |
| POST | `/wechat_logout` | 退出登录 | ❌ | WECHAT | — | — | `mp: pages/user` | 吊销当前令牌 jti |

---

## 五、共享已认证（2）

两种身份都可调用（管理员看后台热搜榜、小程序看首页热搜）。

| 方法 | 路径 | 模块 | 匿名 | 允许角色 | 私有数据 | 需管理员 | 前端调用 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/hotSearch` | 热搜 | ❌ | ADMIN / SUPER / WECHAT | — | — | `web: Dashboard.vue`、`mp: pages/home` | 只读热门问题 |
| GET | `/url` | 讯飞语音鉴权 | ❌ | ADMIN / SUPER / WECHAT | — | — | `mp: pages/home` | 返回讯飞 WebSocket 鉴权 URL；未配置密钥时 503 |

---

## 六、已删除 / 不再暴露的接口

| 路径 | 处置 | 原因 |
| --- | --- | --- |
| `POST /test` | **删除** `Test.java` | 遗留测试接口，会直接触发图库全量查询，无任何业务价值 |
| `POST /change_password`（旧语义） | **收口**为"已登录 + 只能改自己" | 旧实现可匿名改任意账号密码（最严重缺陷） |
| `POST /forget_password`（旧语义） | **改造**为服务端验证码，响应不含验证码 | 旧实现把验证码明文返回浏览器 |

---

## 七、权限判定的三层结构

```
第 1 层（接口级，强制）  SecurityConfig：路径 → 角色，未登记 = denyAll
第 2 层（数据级，强制）  AuthContext.requireSelf()：请求体声明的身份必须等于令牌身份，否则 403
第 3 层（业务级，兜底）  Service：禁止删自己 / 最后一个超管 / 有日志者；密码策略；一次性验证码
────────────────────────────────────────────────────────
前端 v-if / 路由守卫：仅体验层，不构成安全边界（后端不依赖它）
```

> 第 2 层是本轮消除 IDOR（越权访问对象）的关键：即使攻击者拿到合法令牌，也只能操作自己的数据。
