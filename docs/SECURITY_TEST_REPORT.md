# SmartQA 鉴权安全测试报告（Phase Security-1）

> 执行时间：2026-10-27 09:57 ～ 10:2x（GMT+8）
> 被测版本：`feature/modernization` 分支本轮改动（基线 `427162c`，回退标签 `pre-security-1`）
> 执行人：自动化脚本 + 人工核对
> 结论口径：**PASS / FAIL / BLOCKED / NOT TESTED**。所有 PASS 均为真实 HTTP 请求与真实进程日志的结果，**没有**把「编译成功」「前端隐藏菜单」写成「鉴权通过」。

---

## 一、测试环境

| 项 | 值 |
| --- | --- |
| 操作系统 | Windows 11（win32） |
| JDK | Java 21.0.11 LTS |
| Maven | 3.9.16 |
| Node / npm | 22.22.2（npm 11.x） |
| MySQL | 8.0.46，`127.0.0.1:3306`，库 `smartques` ✅ 运行中 |
| Neo4j | 5.26（Docker 容器 `smartqa-neo4j`） ❌ **本会话未启动**（Docker Desktop 守护进程未运行，`com.docker.service` 需管理员权限，未提权） |
| 后端 | Spring Boot 3.5.16，`http://127.0.0.1:8081` ✅ 运行中 |
| 前端 dev server | Vite 7，`http://127.0.0.1:8080` ✅ 运行中，`/api` 代理到 8081 |

> Neo4j 未启动直接导致 3 个依赖图库的接口（`/apart`、实体/关系增删改查）**无法验证 200 分支**，全部标记为 **BLOCKED**，未做任何"看起来应该没问题"的推断。

---

## 二、Maven 测试（自动化，可重复执行）

命令（凭证全部来自环境变量，仓库内不含真实密码）：

```bash
# 需先启动后端；测试账号通过环境变量注入
export SMARTQA_TEST_SUPER_ACCOUNT=... SMARTQA_TEST_SUPER_PASSWORD=...
export SMARTQA_TEST_ADMIN_ACCOUNT=... SMARTQA_TEST_ADMIN_PASSWORD=...
export SMARTQA_TEST_WECHAT_PHONE=...  SMARTQA_TEST_WECHAT_PASSWORD=...
mvn test
```

实际输出（摘要）：

```
[INFO] Running com.example.springboot.ExampleApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.springboot.security.ApiSecurityIntegrationTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] Results:
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| # | 用例 | 断言 | 结果 |
| --- | --- | --- | --- |
| 1 | 未登录访问管理员列表 | HTTP 401 + `code=UNAUTHORIZED` | ✅ PASS |
| 2 | 未登录尝试创建管理员 | HTTP 401 | ✅ PASS |
| 3 | 错误密码登录 | HTTP 401 + `code=INVALID_CREDENTIALS` | ✅ PASS |
| 4 | BCrypt 登录成功 | HTTP 200，token 为三段式 JWT，响应无 `password` | ✅ PASS |
| 5 | 超管访问管理员列表 | HTTP 200 | ✅ PASS |
| 6 | 普通管理员调超管接口 | `/get_web_user`、`/add_web_user` → 403；`/count_wechat_user` → 200 | ✅ PASS |
| 7 | 小程序用户访问管理员接口 | 403 | ✅ PASS |
| 8 | 小程序用户读他人私有数据 | 聊天/收藏/反馈/个人信息 4 项全部 403 | ✅ PASS |
| 9 | 篡改 / 伪造令牌 | 401 | ✅ PASS |

> 说明：`ApiSecurityIntegrationTest` 若检测不到后端或未注入测试账号，会以 JUnit `assumeTrue` **skip 并打印原因**，不会伪装成通过；本次运行 **0 skipped**。

---

## 三、端到端鉴权验证（72 项，全部 PASS）

脚本按 HTTP 真实请求逐条断言，`total 72 / pass 72 / fail 0`。

### A. 未认证访问（13 项）

| ID | 用例 | 期望 | 实际 | 结果 |
| --- | --- | --- | --- | --- |
| A1 | 未登录访问管理员列表 `/get_web_user` | 401 | 401 | PASS |
| A2 | 未登录创建管理员 `/add_web_user` | 401 | 401 | PASS |
| A3 | 未登录删除管理员 `/remove_web_user` | 401 | 401 | PASS |
| A4 | 未登录读取小程序用户列表 `/get_wechat_user` | 401 | 401 | PASS |
| A5 | 未登录读取聊天记录 `/wechat_chatting` | 401 | 401 | PASS |
| A6 | 未登录调用 `/apart` 问答 | 401 | 401 | PASS |
| A7 | 未登录调用讯飞鉴权 `/url` | 401 | 401 | PASS |
| A8 | 已移除的 `/test` 接口不可用 | 401 或 404 | 401 | PASS |
| A9 | 未登录新增实体 `/add_entity` | 401 | 401 | PASS |
| A10 | 未登录新增关系 `/add_relation` | 401 | 401 | PASS |
| A11 | 未登录新增问题 `/add_question` | 401 | 401 | PASS |
| A12 | 未登录禁用小程序用户 `/disable_user` | 401 | 401 | PASS |
| A13 | 未登录调用 `/change_password` 改密码 | 401 | 401 | PASS |

### B. 登录与会话（6 项）

| ID | 用例 | 期望 | 实际 | 结果 |
| --- | --- | --- | --- | --- |
| B1 | 超管 BCrypt 登录 | 200 | 200 | PASS |
| B2 | 登录响应不含 `password` 字段 | 无该字段 | 无该字段 | PASS |
| B3 | 返回真实 JWT（三段式） | 3 段 | 3 段 | PASS |
| B4 | 错误密码登录被拒绝 | 401 | 401 | PASS |
| B5 | 普通管理员登录 | 200 | 200 | PASS |
| B6 | 普通管理员误选超管身份登录被拒绝 | 401 | 401 | PASS |

### C. 普通管理员越权（9 项，全部 403）

| ID | 用例 | 期望 | 实际 | 结果 |
| --- | --- | --- | --- | --- |
| C1 | 读取管理员列表 | 403 | 403 | PASS |
| C2 | 创建管理员 | 403 | 403 | PASS |
| C3 | 删除管理员 | 403 | 403 | PASS |
| C4 | 修改管理员角色（把自己改成超管） | 403 | 403 | PASS |
| C5 | 按角色筛选管理员 | 403 | 403 | PASS |
| C6 | 统计管理员数量 | 403 | 403 | PASS |
| C7 | 调用数据导入 `/import_data` | 403 | 403 | PASS |
| C8 | 修改他人资料（body 传 `admin`） | 403 | 403 | PASS |
| C9 | 通过 `/change_password` 改超管密码 | 403 | 403 | PASS |

### D. 普通管理员正常业务（12 项，全部 200）

统计/分页读取：小程序用户数、小程序用户、日志、问题、问题模板、反馈、热搜、本人信息 —— 12 项全部 200，**证明收紧鉴权没有破坏正常业务**。

### E. 超级管理员业务与防自锁（10 项）

| ID | 用例 | 期望 | 实际 | 结果 |
| --- | --- | --- | --- | --- |
| E1 | 读取管理员列表 | 200 | 200 | PASS |
| E2 | 统计管理员数量 | 200 | 200 | PASS |
| E3 | 读取小程序用户列表 | 200 | 200 | PASS |
| E4 | 用弱口令（`admin`）创建管理员被拒 | false | false | PASS |
| E5 | 用强口令创建管理员 | true | true | PASS |
| E6 | 重复账号创建被拒 | false | false | PASS |
| E7 | 删除刚创建的管理员 | true | true | PASS |
| E8 | 超管删除自己 | false | false | PASS |
| E9 | 删除不存在账号 | false | false | PASS |
| E10 | 用弱口令改自己密码被策略拒绝 | false | false | PASS |

### F. 小程序用户权限（16 项）

| ID | 用例 | 期望 | 实际 | 结果 |
| --- | --- | --- | --- | --- |
| F1 | 注册 / 登录获取令牌 | 200 | 200 | PASS |
| F2 | 访问管理员列表 | 403 | 403 | PASS |
| F3 | 访问管理员创建 | 403 | 403 | PASS |
| F4 | 访问日志接口 | 403 | 403 | PASS |
| F5 | 访问**本人**聊天记录 | 200 | 200 | PASS |
| F6 | 访问**他人**聊天记录 | 403 | 403 | PASS |
| F7 | 访问**他人**个人信息 | 403 | 403 | PASS |
| F8 | 访问**他人**收藏 | 403 | 403 | PASS |
| F9 | 查看**他人**反馈 | 403 | 403 | PASS |
| F10 | 清空**他人**聊天记录 | 403 | 403 | PASS |
| F11 | 修改**他人**密码 | 403 | 403 | PASS |
| F12 | 访问热搜（共享接口） | 200 | 200 | PASS |
| F13 | 管理员令牌访问小程序私有接口 | 403 | 403 | PASS |
| F14 | 小程序用户访问实体管理接口 | 403 | 403 | PASS |
| F15 | 小程序用户新增实体 | 403 | 403 | PASS |
| F16 | 小程序用户禁用其他用户 | 403 | 403 | PASS |

### G. 令牌完整性（6 项）

| ID | 用例 | 期望 | 实际 | 结果 |
| --- | --- | --- | --- | --- |
| G1 | 无 `Bearer` 前缀的裸令牌 | 401 | 401 | PASS |
| G2 | 篡改签名 | 401 | 401 | PASS |
| G3 | 随意构造的字符串 | 401 | 401 | PASS |
| G4 | **已过期**令牌（用真实密钥签名、`exp` 设为 2 小时前） | 401 + `TOKEN_EXPIRED` | 401 + `TOKEN_EXPIRED` | PASS |
| G5 | 退出登录 | 200 | 200 | PASS |
| G6 | 退出后**原令牌立即失效** | 401 + `TOKEN_REVOKED` | 401 + `TOKEN_REVOKED` | PASS |

> G4 用真实 `JWT_SECRET` 自签一枚过期令牌，验证的是"过期校验生效"，而不是"随便一个坏串被拒"。

---

## 四、业务回归（11 项：10 PASS / 1 BLOCKED）

| ID | 用例 | 实际 | 结果 |
| --- | --- | --- | --- |
| W0 | 小程序用户登录获取令牌 | 200 | ✅ PASS |
| W1 | 图文问答 `/apart` 返回答案 | **500**（`Unable to connect to 127.0.0.1:7687`） | ⚠️ **BLOCKED**（Neo4j 未启动，非鉴权回归） |
| W2 | 写入本人聊天记录 | 200 `true` | ✅ PASS |
| W3 | 读取本人聊天记录含刚才内容 | 200，内容匹配 | ✅ PASS |
| W4 | 清空本人历史记录生效 | 200 / 200 | ✅ PASS |
| W5 | 新增并读取本人收藏 | 200 / 200 | ✅ PASS |
| W6 | 取消收藏生效 | 200 / 200 | ✅ PASS |
| W7 | 提交本人反馈 | 200 `true` | ✅ PASS |
| W8 | 查看本人反馈列表 | 200 | ✅ PASS |
| W9 | 热搜列表可读 | 200（数组） | ✅ PASS |
| W10 | 读取本人资料不含密码 | 200，无 `password` | ✅ PASS |

`/apart` 的 500 是**环境依赖**问题：该接口需要 `MATCH (n {name: $name})` 查图库。改造前该接口同样依赖 Neo4j。鉴权层面它已通过 A6（未登录 401）与 F 组（角色校验）验证，**不能**据此认为"问答功能被改坏了"，也不能据此认为"问答功能已验证通过"。

---

## 五、构建验证

| 项 | 命令 | 结果 |
| --- | --- | --- |
| 后端编译 | `mvn -o compile` | ✅ BUILD SUCCESS |
| 后端打包 | `mvn -o package -DskipTests` | ✅ 生成 `target/springboot-0.0.1-SNAPSHOT.jar`（约 49.5 MB） |
| 后端测试 | `mvn test` | ✅ 10 tests / 0 failures / 0 skipped |
| 前端生产构建 | `npm run build` | ✅ built in 9.95s，产物含 `ForgetPassword`、`AdminManage` 等分包 |
| 前端 dev + 代理 | 通过 `http://127.0.0.1:8080/api/*` 请求 | ✅ 登录 200 返回 263 字符 JWT；未认证 `/api/get_web_user` → 401；带令牌 → 200 |

---

## 六、BLOCKED / NOT TESTED（不假装通过）

| 项 | 状态 | 原因 | 复现方式 |
| --- | --- | --- | --- |
| `/apart` 问答返回答案 | BLOCKED | 本机 Docker 守护进程未运行，Neo4j 5.26 容器未启动 | 启动 Docker Desktop → `docker compose up -d` → 重跑 `business_check.py` |
| 实体 / 关系增删改查（200 分支） | BLOCKED | 同上（`add_entity` 等会调用 `Neo4jService`） | 同上；鉴权的 401/403 已由 A9/A10/F14/F15 覆盖 |
| `/import_data` 成功路径 | NOT TESTED | 会批量写入 MySQL + Neo4j，属于破坏性操作，本轮不做 | 人工确认后自行验证 |
| 微信小程序 GUI 全流程 | BLOCKED | 需微信开发者工具、正式 AppID、合法域名 | 见第七节人工清单 |
| 找回密码端到端（真发短信） | BLOCKED | 未配置榛子云 `ZHENZI_SMS_APP_ID/SECRET`，实现为 fail-closed | 配置短信后验证 |
| 登录失败次数限制 | NOT TESTED | **功能未实现**（见残留风险） | — |
| 多实例 / 重启后的令牌吊销 | NOT TESTED | 需多实例或重启验证，本轮未做 | 重启后端后旧令牌会恢复可用（已知边界） |

---

## 七、小程序人工验收清单（必须人工确认，未自动执行）

前置：启动后端（8081）；微信开发者工具导入 `miniprogram/`；详情 →「不校验合法域名」勾选；AppID 暂用测试号。

| # | 步骤 | 预期 | 结果 |
| --- | --- | --- | --- |
| M1 | 打开小程序，进入登录页 | 页面正常，无报错 | ☐ |
| M2 | 用不存在的手机号登录 | 弹窗"手机号或密码错误" | ☐ |
| M3 | 用正确账号登录 | 弹窗用户指南 → 进入首页 | ☐ |
| M4 | 检查 Storage | 存在 `smartqa_token`、`smartqa_user`，`smartqa_token` 为三段式字符串 | ☐ |
| M5 | 提问（文本） | 返回答案（需 Neo4j 已启动） | ☐ |
| M6 | 收藏 / 取消收藏，进入"我的收藏" | 列表与操作一致 | ☐ |
| M7 | 进入"历史记录"，清空 | 列表清空 | ☐ |
| M8 | 提交反馈 → 查看反馈 | 能看到自己刚提交的反馈 | ☐ |
| M9 | 个人中心 → 修改密码（旧密码错误） | 提示"旧密码错误" | ☐ |
| M10 | 修改密码（正确旧密码 + 新密码 ≥6 位） | 提示成功 → **回到登录页**（旧令牌已吊销） | ☐ |
| M11 | 用新密码重新登录 | 成功 | ☐ |
| M12 | 个人中心 → 退出登录 | 回到登录页；此时用抓包工具重放旧请求 → 401 | ☐ |
| M13 | 抓包工具（如 Charles）手动把请求体 `phone` 改成他人手机号，调 `/collectible` | 返回 403 + "禁止访问或修改其他用户的数据" | ☐ |
| M14 | 用小程序令牌手动调 `POST /get_web_user` | 403 | ☐ |

---

## 八、本轮对数据库的读写记录（可核验）

| 操作 | 对象 | 数量 | 说明 |
| --- | --- | --- | --- |
| 新增 → 删除 | `web_user.sec_test_admin` | +1 / −1 | 测试"创建管理员 + 弱口令拒绝 + 删除"，测试后立即删除，**净变化 0** |
| 新增 → 删除 | `log`（`sec_test_admin` 的登录日志） | +1 / −1 | 因 `log` 外键 `RESTRICT` 阻塞删除，先删该行日志再删账号，均为本轮测试产物 |
| 新增 → 删除 | `wechat_users`（测试手机号） | +1 / −1 | 小程序越权测试账号，测试后删除 |
| 新增 → 删除 | `feedback_users`（测试手机号） | +1 / −1 | 业务回归 W7/W8 产生，测试后删除 |
| 新增 → 删除 | `chatting`、`user_collectible` | 0 / 0 | W4/W6 已自清理，净变化 0 |
| 仅追加 | `log`（正常登录/操作审计） | 净增若干行 | 审计日志性质，保留不删 |

清理后实测：`web_user=2`（`admin` 超管、`test01` 普通管理员，**均为改造前既有账号，未删除、未改密码**）、`wechat_users=1`（既有账号）、`feedback_users=0`、`chatting=0`、`user_collectible=0`。

**未执行**：任何 `TRUNCATE` / `DROP` / 批量 `DELETE`；未修改 MySQL 密码；未触碰 Neo4j 数据卷；未删除现有管理员或微信用户。

---

## 九、残留风险（按优先级）

| 优先级 | 风险 | 建议 |
| --- | --- | --- |
| 高 | 无登录失败次数限制，可在线暴力破解 | 增加账号/IP 维度限流与锁定 |
| 高 | 令牌吊销表在单 JVM 内存，重启/多实例失效 | 引入 Redis 或改用不透明令牌（opaque token） |
| 中 | 全链路 HTTP 明文，令牌可被中间人截获 | 部署时强制 HTTPS（小程序正式版本来就要求 HTTPS） |
| 中 | `/import_data` 仍接受服务器文件路径 | 限定到固定导入目录并做后缀/大小校验 |
| 中 | 现有 `admin` 账号口令仍是初始弱口令 | 上线前用「个人信息」页改成强口令（本轮**未**擅自修改用户既有口令） |
| 低 | 管理员删除受 `log` 外键限制 | 如需删除，先归档日志（人工确认） |
| 低 | 找回密码依赖未配置的短信服务 | 配好榛子云后验证 |
| 低 | 前端 dev server 依赖 `/api` 代理 | 生产部署需 Nginx 反代或同域部署 |
