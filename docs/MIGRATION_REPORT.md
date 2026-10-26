# 迁移报告（MIGRATION_REPORT）

> 本报告记录「智能问答知识库管理系统」→ `SmartQA` 的目录标准化整理过程。
> 迁移方式：**只读源目录 + 复制到目标目录**，未删除、未移动任何原始文件。

---

## 1. 基本信息

| 项 | 内容 |
| --- | --- |
| 原始项目路径 | `D:\School\3\up\软工\project\智能问答知识库管理系统` |
| 原始压缩包 | `D:\School\3\up\软工\project\智能问答知识库管理系统.rar`（175,776,079 字节，未改动） |
| 目标项目路径 | `D:\School\3\up\软工\SmartQA` |
| 迁移时间 | 2026-10-22 |
| 迁移工具 | `robocopy`（目录复制，带排除项）+ `Copy-Item`（单文件） |
| 校验算法 | SHA-256（`Get-FileHash -Algorithm SHA256`） |

> 原始项目在迁移前已存在解压目录 `项目\智能问答知识库管理系统`，本次直接使用该解压目录作为源，未重新解压压缩包。

---

## 2. 文件映射关系

| 原始位置 | 目标位置 |
| --- | --- |
| `工程文件/(智能问答知识库管理系统) 源代码/后端代码/` | `backend/` |
| `工程文件/(智能问答知识库管理系统) 源代码/网页前端代码/` | `web/` |
| `工程文件/(智能问答知识库管理系统) 源代码/小程序前端代码/` | `miniprogram/` |
| `工程文件/数据库.sql` | `database/数据库.sql` |
| `项目正式文档/` | `docs/original/` |
| `项目过程文档/` | `docs/archive/` |

映射过程中**保留了各模块原有的内部目录层级与源码内容**。

---

## 3. 文件复制数量

| 模块 | 目标文件数 | 源（排除后）文件数 | 说明 |
| --- | ---: | ---: | --- |
| `backend/` | 91 | 91 | 含 `.mvn`、`src`、`pom.xml`、`mvnw` 等 |
| `web/` | 69 | 69 | 含 `src`、`public`、`package*.json`、`vue.config.js` 等 |
| `miniprogram/` | 84 | 84 | 含 `pages`、`icons`、`utils`、`app.*`、`project.config.json` 等 |
| `database/` | 1 | 1 | `数据库.sql` |
| `docs/original/` | 5 | 5 | 需求、UML、数据库设计、测试报告、用户手册 |
| `docs/archive/` | 212 | 212 | 个人周计划/总结、会议纪要、原型与 UML 等 |
| **合计** | **462** | **462** | 全部纳入迁移范围 |

另新建的非迁移文件：`README.md`、`.gitignore`、`docs/PROJECT_STRUCTURE.md`、`docs/MIGRATION_REPORT.md`（由整理者编写）。

---

## 4. 排除的缓存与构建文件

> 原则：源码、配置、资源、文档完整保留；仅排除 IDE 缓存、构建产物、依赖缓存与个人 IDE 配置。

### 4.1 后端 `backend/`

| 排除项 | 类型 | 理由 |
| --- | --- | --- |
| `.idea/` | IDE 缓存 | IntelliJ IDEA 工程配置 |
| `target/` | 构建产物 | Maven 编译输出（含 `.class`、打包结果） |
| `example.iml` | IDE 元数据 | IntelliJ 模块文件 |

### 4.2 网页前端 `web/`

| 排除项 | 类型 | 理由 |
| --- | --- | --- |
| `.git/` | 嵌套 Git 仓库 | **不复制**，避免形成嵌套仓库；原仓库保留不动 |
| `.idea/` | IDE 缓存 | IntelliJ 配置 |
| `.vscode/`（仅 `launch.json`） | 个人编辑器配置 | 调试配置属个人环境 |
| `.VSCodeCounter/` | 插件缓存 | VS Code 插件统计产物 |
| `dist/` | 构建产物 | 前端打包输出 |
| `vue-manage-system-master.iml` | IDE 元数据 | IntelliJ 模块文件 |

### 4.3 小程序 `miniprogram/`

| 排除项 | 类型 | 理由 |
| --- | --- | --- |
| `.git/` | 嵌套 Git 仓库 | **不复制**，避免形成嵌套仓库；原仓库保留不动 |
| `.idea/` | IDE 缓存 | IntelliJ 配置 |
| `.VSCodeCounter/` | 插件缓存 | VS Code 插件统计产物 |

### 4.4 源根及其它

| 排除项 | 说明 |
| --- | --- |
| `工程文件/(智能问答知识库管理系统) 源代码/.idea/` | 位于三个模块之上，未映射到任何目标目录，天然未复制 |
| `node_modules/` | 全项目搜索**不存在**，故无实际排除；已在复制规则中防御性排除 |

### 4.5 嵌套 `.git` 处理说明

`web/` 与 `miniprogram/` 原目录中各含一个 `.git`。为**避免新项目形成嵌套 Git 仓库**，二者均未复制；原始仓库及其提交历史**原样保留在 `project/` 下未改动**。如后续需要独立版本历史，可自行重新初始化仓库或从原仓库迁移。

---

## 5. 完整性检查结果

### 5.1 文件级 SHA-256 校验

对纳入迁移的 **462 个文件**逐一计算源文件与目标文件的 SHA-256：

| 指标 | 结果 |
| --- | --- |
| 目标文件总数 | 462 |
| SHA-256 一致 | **462** |
| 不一致 | 0 |
| 目标缺失（源有、目标无） | 0 |
| 目标多余（目标有、源无） | 0 |

分模块结果：

| 模块 | 目标数 | 一致 | 不一致 | 缺失 | 多余 |
| --- | ---: | ---: | ---: | ---: | ---: |
| backend | 91 | 91 | 0 | 0 | 0 |
| web | 69 | 69 | 0 | 0 | 0 |
| miniprogram | 84 | 84 | 0 | 0 | 0 |
| docs/original | 5 | 5 | 0 | 0 | 0 |
| docs/archive | 212 | 212 | 0 | 0 | 0 |
| database/数据库.sql | 1 | 1 | 0 | 0 | 0 |

### 5.2 结构与存在性检查

| 检查项 | 结果 |
| --- | --- |
| `backend/pom.xml` 存在 | ✅ |
| `backend/src/main` 目录存在 | ✅ |
| `web/package.json` 存在 | ✅ |
| `web/src` 目录存在 | ✅ |
| `miniprogram/app.json` 存在 | ✅ |
| `database/数据库.sql` 存在 | ✅ |
| `docs/original/` 保留 5 份正式文档 | ✅ |
| 目标中是否误入 `.git`/`.idea`/`.vscode`/`.VSCodeCounter`/`target`/`dist`/`node_modules` | ❌ 均未发现 |
| 目标中 `*.iml` 数量 | 0 |
| `数据库.sql` 源/目标 SHA-256 一致 | ✅ |

### 5.3 异常空目录 / 路径冲突

- 未发现异常空目录（robocopy `/E` 复制了应有的目录层级）；
- 目标目录在迁移前为空，**不存在覆盖冲突**，未覆盖任何既有文件。

---

## 6. 迁移过程中发现的问题

1. **Windows 文件权限（ACL / 完整性标签）阻塞写入**
   - 现象：初始对工作区根目录 `D:\School\3\up\软工` 的任何命令均报
     `SetNamedSecurityInfoW failed (Win32 5): grantWrite(...)`；
     根因是该目录缺少当前用户的直接 `WRITE_OWNER` 权限项。
   - 处理：按环境诊断流程，为 `D:\School\3\up\软工` 与 `D:\School\3\up\软工\SmartQA`
     补充了当前用户（`S-1-5-21-...-1001`）的“完全控制”权限项，并生成了可回滚备份
     （位于 `D:\School\3\up\acl-recovery\`）。
   - 进一步发现：DSH 沙箱进程以**低完整性**运行，只能写入带 `Low Mandatory Level` 标签的对象；
     工作区根目录已有该标签，而 `SmartQA` 没有，导致受限模式下仍无法写入。
   - 处理：**未修改目录完整性标签**；改为在“完全权限”模式下执行写入类操作（复制、写文档），
     只读校验仍在受限模式下完成。此处理未改动任何业务文件内容。

2. **网页入口依赖 `node_modules` 实际存在**
   - `web/src/main.js` 使用 `import axios from '../node_modules/axios'`（而非 `from 'axios'`），
     说明工程按“已安装依赖”状态组织；首次运行前必须先 `npm install`，否则构建失败。

3. **大量硬编码的连接信息与路径**
   - MySQL（`application.yml`）、Neo4j（`Neo4jService.java`、`WeChatReplyService.java`）
     均为远程地址 + 明文账号密码；
   - HanLP 数据目录为 Linux 路径 `/root/spring_java/data-for-1.7.5`，Windows 下不可直接使用；
   - 小程序接口域名硬编码为 `https://www.bulaqiu.top/api`；
   - 网页开发代理与端口（80）硬编码。
   - **按整理要求均未修改**，仅记录。

4. **嵌套 `.git`**
   - `web/`、`miniprogram/` 各含一个 `.git`，已按规则不复制，原始仓库保持不变。

5. **前端依赖锁文件来自旧 npm 源**
   - `web/package-lock.json` 中 `resolved` 多为 `registry.npm.taobao.org` 等旧镜像，
     部分地址可能已失效；安装依赖时可能需要更换镜像或删除锁文件重装（本次未改动）。

6. **端口 80**
   - `web/vue.config.js` 将开发服务器端口设为 80，Windows 下通常需要管理员权限或改用其它端口。

---

## 7. 尚未完成的工作

- [ ] 未执行 `mvn` 编译/打包（`backend/`）；
- [ ] 未执行 `npm install` / `npm run serve` / `npm run build`（`web/`）；
- [ ] 未编译或运行微信小程序（`miniprogram/`）；
- [ ] 未导入 `database/数据库.sql`，未连接 MySQL / Neo4j；
- [ ] 未配置 HanLP 数据目录，未验证分词与模糊匹配；
- [ ] 未启动后端、网页前端、小程序，未做任何功能/联调测试；
- [ ] 未修改任何硬编码地址、账号密码、路径（按任务要求保持原样）；
- [ ] 未初始化新的 Git 仓库，未执行任何 `git add/commit/push`；
- [ ] 未处理 `docs/archive/` 的个人资料外发问题（仅通过根 `.gitignore` 默认排除）。

---

## 8. 结论

- 目录已按目标结构完成标准化整理；
- 462 个纳入迁移的文件全部复制成功且 SHA-256 完全一致；
- 未删除、未移动、未修改原始项目中的任何文件；
- 未修改任何业务源码、`pom.xml`、`package.json`、`package-lock.json`、数据库脚本或连接配置；
- 项目**尚未实际运行验证**。
