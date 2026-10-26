# SmartQA 现代化升级报告（阶段 C：前端升级）

> 生成时间：2026-10-26  
> 工作目录：`D:\School\3\up\软工\SmartQA`  
> 本报告覆盖阶段 C（Vue 前端升级），阶段 A/B 结论见 `MODERNIZATION_PLAN.md` 及后端升级记录。

---

## 一、升级前后技术栈

| 维度       | 升级前                   | 升级后                                           |
| -------- | --------------------- | --------------------------------------------- |
| 前端框架     | Vue 2.6.10            | **Vue 3.5.44**                                |
| 构建工具     | Vue CLI 3 + Webpack 4 | **Vite 7.3.7**                                |
| UI 组件库   | Element UI 2.11.0     | **Element Plus 2.14.7**                       |
| 路由       | vue-router 3.0.3      | **vue-router 4.6.4**                          |
| 状态管理     | vuex 3.6.2            | **vuex 4.1.0**                                |
| 国际化      | vue-i18n 8.10.0       | **vue-i18n 9.14.5**                           |
| HTTP 客户端 | axios 0.18.1          | **axios 1.20.0**                              |
| 图谱渲染     | vis-network 9.1.0     | **vis-network 9.1.13**（保持 9.x，避免 10.x 破坏性变更）  |
| 图片裁剪     | vue-cropperjs 3.0.0   | **vue-cropperjs 3.0.0**（3.x 已支持 Vue 3）        |
| 查询字符串    | qs 6.9.3              | **qs 6.16.1**                                 |
| Vite 插件  | —                     | **@vitejs/plugin-vue 6.0.10**（peer 支持 vite 7） |

**Node / npm 实际版本**（真实验证）：

- Node.js **v24.14.0**（`D:\CodeTools\nodejs\node.exe`）
- npm **11.9.0**（`D:\CodeTools\nodejs\npm.cmd`）

> 注意：bash 默认 PATH 里的 node 是 managed 版 v22.22.2 / npm 10.9.7，**并非**任务要求的 Node 24。  
> 本次迁移全程使用系统目录 `D:\CodeTools\nodejs\` 下的 **Node 24.14.0 + npm 11.9.0** 执行安装与构建，未用 Node 22 冒充 Node 24。

---

## 二、实际采用的准确依赖版本

`package.json` 依赖清单（已通过 `npm ls --depth=0` 实测落定）：

```
@vitejs/plugin-vue  6.0.10   (devDependency)
axios               1.20.0
element-plus        2.14.7
qs                  6.16.1
vis-network         9.1.13
vue                 3.5.44
vue-cropperjs       3.0.0
vue-i18n            9.14.5
vue-router          4.6.4
vuex                4.1.0
vite                7.3.7    (devDependency)
```

锁文件：`package-lock.json`（lockfileVersion 3，共 107 个包）。

**移除的死依赖**（原 package.json 声明但源码未引用）：  
`babel-polyfill`、`echarts`、`mavon-editor`、`vue-quill-editor`、`vue-schart`、`vuedraggable`、`neovis.js`、`@vue/cli-plugin-babel`、`@vue/cli-service`、`vue-template-compiler`。

---

## 三、修改文件清单

### 新增文件

| 文件                             | 说明                                              |
| ------------------------------ | ----------------------------------------------- |
| `web/index.html`               | Vite 入口 HTML（从 public/ 移到根目录，注入 `/src/main.js`） |
| `web/vite.config.js`           | Vite 配置（端口 8080、/api 代理到 127.0.0.1:8081、@ 别名）   |
| `docs/MODERNIZATION_REPORT.md` | 本报告                                             |

### 重写文件

| 文件                                        | 迁移内容                                                                                                            |
| ----------------------------------------- | --------------------------------------------------------------------------------------------------------------- |
| `web/package.json`                        | 依赖升级、scripts 改为 vite                                                                                            |
| `web/src/main.js`                         | `new Vue()` → `createApp()`；`Vue.use()` → `app.use()`；`Vue.prototype.$axios` → `app.config.globalProperties`    |
| `web/src/router/index.js`                 | `new Router({ mode: 'history' })` → `createRouter({ history: createWebHistory() })`；`*` 通配 → `/:pathMatch(.*)*` |
| `web/src/store.js`                        | `new Vuex.Store()` → `createStore()`                                                                            |
| `web/src/components/common/bus.js`        | `new Vue()` EventBus → 自定义轻量 Bus 类（保持 `$on/$emit/$off` 接口）                                                      |
| `web/src/components/common/directives.js` | `bind` 钩子 → `mounted` 钩子；`Vue.directive` → 导出对象 + `app.directive`                                               |

### 编辑文件（Vue 组件迁移）

| 文件                              | 迁移点                                                                                 |
| ------------------------------- | ----------------------------------------------------------------------------------- |
| `common/GraphView.vue`          | `beforeDestroy` → `beforeUnmount`                                                   |
| `common/Header.vue`             | `slot="dropdown"` → `#dropdown`；`require()` 头像 → `new URL(..., import.meta.url)`    |
| `common/Sidebar.vue`            | `el-submenu` → `el-sub-menu`；`slot="title"` → `#title`                              |
| `common/Tags.vue`               | `slot="dropdown"` → `#dropdown`；`route.matched[1].components.default.name` 防御性处理    |
| `page/Login.vue`                | `slot="prepend"` → `#prepend`；`.native` 移除；`el-radio label` → `value`               |
| `page/Dashboard.vue`            | `slot="header"` → `#header`；`slot-scope` → `#default`；14 处 `require()` → `new URL`  |
| `page/Neo4j.vue`                | `slot="prepend"` → `#prepend`；`.native` 移除；`el-option` 修正为 `:value/:label`          |
| `page/EntityForm.vue`           | `.native`、`slot="prepend"`、`slot-scope`、3 处 `:visible.sync`、2 处 `slot="footer"`     |
| `page/RelationForm.vue`         | 同上 + 4 处 `:visible.sync`、2 处 `slot="footer"`                                        |
| `page/UserManage.vue`           | `.native`、2 处 `slot-scope`、`:visible.sync`、`slot="footer"`                          |
| `page/AdminManage.vue`          | 2 处 `slot-scope`、2 处 `:visible.sync`、2 处 `slot="footer"`、`el-radio label` → `value` |
| `page/QuestionForm.vue`         | `.native`、2 处 `slot-scope`、2 处 `:visible.sync`、2 处 `slot="footer"`                  |
| `page/QuestionTemplateForm.vue` | `.native`、`slot="prepend"`、`slot-scope`、2 处 `:visible.sync`、2 处 `slot="footer"`     |
| `page/FeedbackForm.vue`         | 2 处 `slot-scope`、2 处 `:visible.sync`、`slot="footer"`、`el-radio label` → `value`     |
| `page/Logs.vue`                 | `.native`、`slot-scope`                                                              |
| `page/UserInformation.vue`      | `el-radio label` → `value`                                                          |
| `page/Upload.vue`               | `slot="tip"`、`:visible.sync`、`slot="footer"`、`require()` → `new URL`                |

### 删除文件

| 文件                      | 说明                        |
| ----------------------- | ------------------------- |
| `web/vue.config.js`     | 旧代理配置（迁移到 vite.config.js） |
| `web/babel.config.js`   | Vite 不再需要                 |
| `web/postcss.config.js` | Vite 内置 PostCSS           |
| `web/.browserslistrc`   | Vite 不再需要                 |
| `web/public/index.html` | 移到根目录                     |

### 保留未改（无需迁移）

- `web/src/api/index.js`、`web/src/api/global_data.js`、`web/src/utils/request.js`：纯 axios 封装，跨框架兼容
- `web/src/utils/graphData.js`：纯逻辑，无 Vue 依赖
- `web/src/utils/neovis.js`、`neovis-without-dependencies.js`：遗留 vendor 库，未被引用，不参与构建
- `web/src/components/page/I18n.vue`：未注册到路由的遗留演示页，不被 import，不影响构建
- `web/src/assets/css/*`：CSS 不受 Vue 版本影响

---

## 四、迁移难点与处理

### 1. Vue 2 → Vue 3 语法迁移

| 旧语法                   | 新语法                  | 影响文件                                           |
| --------------------- | -------------------- | ---------------------------------------------- |
| `:visible.sync="x"`   | `v-model="x"`        | el-dialog，8 个组件                                |
| `@keyup.enter.native` | `@keyup.enter`       | 9 个组件                                          |
| `slot="xxx"`          | `<template #xxx>`    | 14 个组件                                         |
| `slot-scope="scope"`  | `#default="scope"`   | 9 个组件                                          |
| `beforeDestroy`       | `beforeUnmount`      | GraphView                                      |
| `new Vue()` EventBus  | 自定义 Bus 类            | bus.js                                         |
| `el-submenu`          | `el-sub-menu`        | Sidebar                                        |
| `el-radio label="0"`  | `el-radio value="0"` | Login/AdminManage/FeedbackForm/UserInformation |

### 2. 图标体系迁移（关键兼容层）

Element Plus 2.x **移除了 Element UI 的字体图标体系**。项目使用两类图标：

- **`el-icon-lx-*`（5 个）**：来自 alicdn 外部字体 `//at.alicdn.com/t/font_830376_qzecyukz0s.css`，由 index.html 引入，**保持不变**。
- **`el-icon-*`（约 25 个 Element UI 内置图标）**：通过保留的 `web/src/assets/css/theme-green/index.css`（内含完整 `@font-face` + `el-icon-*` 的 content 映射，字体文件 `theme-green/fonts/element-icons.ttf/woff`）作为**兼容层**，在 main.js 中引入，使旧类名继续渲染图标，**无需逐个改写为 Element Plus 的 `<el-icon><Xxx/></el-icon>` 组件**。

### 3. `require()` 静态资源 → Vite `new URL`

Vue CLI 的 `require('../../assets/img/N.jpg')` 在 Vite 下不可用，统一替换为 `new URL('../../assets/img/N.jpg', import.meta.url).href`（Header、Dashboard 的 14 处、Upload）。

### 4. 依赖兼容冲突修复

- `@vitejs/plugin-vue@5.2.4` 的 peer 是 `vite ^5||^6`，与 Vite 7 冲突 → 升级为 `@vitejs/plugin-vue@6.0.10`（peer 支持 `^7.0.0`）。
- `vue-i18n@9` 无 default export → 改用命名导入 `import { createI18n } from 'vue-i18n'`。

### 5. vis-network 在 Vite 下的兼容性

`GraphView.vue` 使用 `import { Network } from 'vis-network/standalone'`，在 Vite 7 下**成功构建**（打包进 graphData chunk，625KB）。无需额外配置。

---

## 五、构建与验证结果（真实验证）

| 验证项                | 结果                  | 证据                                                         |
| ------------------ | ------------------- | ---------------------------------------------------------- |
| Node 版本            | ✅ v24.14.0          | `node -v` 输出 v24.14.0                                      |
| npm 版本             | ✅ 11.9.0            | `npm -v` 输出 11.9.0                                         |
| 依赖安装               | ✅ 107 packages      | `npm install` added 107 packages                           |
| 锁文件                | ✅ lockfileVersion 3 | package-lock.json 生成                                       |
| 生产构建               | ✅ 成功                | `✓ 1774 modules transformed`、`✓ built in 6.50s`            |
| 开发服务器              | ✅ 启动                | `VITE v7.3.7 ready in 1023 ms`、`http://127.0.0.1:8080/`    |
| 首页访问               | ✅ 200               | `curl` 返回 200，HTML 含 vite 注入                               |
| /api 代理            | ✅ 转发成功              | POST /api/count_entity 返回后端 500（MyBatis 连 MySQL 失败），证明代理生效 |
| main.js/App.vue 编译 | ✅ 200               | Vite 正确编译无语法错误                                             |

**构建产物**：`web/dist/` 包含 index.html、所有页面 chunk（403/404/Login/Dashboard/EntityForm/RelationForm/Neo4j 等）、element-icons 字体、全部图片资源。

**唯一警告**：`index.js`（1.2MB）和 `graphData.js`（625KB）chunk 超过 500KB，属 Element Plus 全量引入 + vis-network 的正常现象，不影响功能，可后续通过 `manualChunks` 或按需引入优化。

---

## 六、未解决问题

### 1. `npm ci` 被环境安全策略拦截

`npm ci` 需要先清空 node_modules（约 200+ 文件），触发 WorkBuddy 环境的 `safe-delete` 批量删除保护，报错：

```
npm error [safe-delete][SAFE_DELETE_BULK_CONFIRM_REQUIRED] {"count":201,...}
```

- **性质**：环境级安全保护，非代码/依赖问题。
- **影响**：`npm ci` 严格模式未能在沙箱内完成。但 `npm install`（安装结果等价）已成功完成，锁文件已生成，构建与运行均验证通过。
- **解决**：在真实终端（非沙箱）或关闭该保护后执行 `npm ci` 即可。团队成员本地执行不受此限制。

### 2. `vue-i18n@9.14.5` 已停止维护（deprecation 警告）

npm 提示 v9/v10 已停止维护，建议迁移 v11。项目实际仅未注册的 I18n.vue 演示页使用 i18n，功能无影响。可后续升 v11 消除警告。

### 3. 真实数据库联调未做

前端构建与代理已验证，但真实 MySQL/Neo4j 读写（登录、实体/关系查询、图谱数据）属于阶段 E 联调，需 `DB_PASSWORD`/`NEO4J_PASSWORD` 环境变量。代理转发已证明链路通，数据读写待阶段 E。

---

## 七、回退方式

完整备份目录：`D:\School\3\up\软工\SmartQA-backup-pre-modernization-20261026-163240`

回退前端到升级前状态：

```bash
# 删除当前 web 目录
rm -rf "D:/School/3/up/软工/SmartQA/web"
# 从备份恢复
cp -r "D:/School/3/up/软工/SmartQA-backup-pre-modernization-20261026-163240/web" "D:/School/3/up/软工/SmartQA/web"
# 重新安装旧依赖（如需回退到 Vue 2 可运行状态）
cd "D:/School/3/up/软工/SmartQA/web" && npm install
```

> 备份已核实：473 个文件，关键文件哈希与源一致，无构建产物残留。

---

## 八、结论

| 结论项                     | 结果                                |
| ----------------------- | --------------------------------- |
| 是否原生适配 Node.js 24.14.0？ | ✅ 是（全程使用 Node 24 + npm 11 验证）     |
| Vue 3 + Vite 7 是否构建成功？  | ✅ 是（`npm run build` 成功）           |
| Element Plus 是否正常工作？    | ✅ 是（组件 + 图标兼容层）                   |
| 开发服务器是否启动？              | ✅ 是（8080 端口，1023ms 就绪）            |
| /api 代理是否正确转发到 8081？    | ✅ 是（路径重写 `/api`→空，实测返回后端响应）       |
| 是否保留原有页面/菜单/业务操作？       | ✅ 是（23 个组件全部迁移，路由/菜单结构不变）         |
| 是否浏览器直连 Neo4j / 前端保存密码？ | ✅ 否（0 命中，图谱走后端 API + vis-network） |
| 是否引入原作者地址/旧凭据？          | ✅ 否（0 命中）                         |

**阶段 C 前端升级已完成并通过构建与运行验证。** 建议进入阶段 D（Neo4j 5.26 迁移）和阶段 E（联调回归）。
