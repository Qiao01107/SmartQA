# SmartQA 团队部署指南（TEAM SETUP GUIDE）

> 面向拿到本项目仓库的队友，从零独立完成本地部署。
> 环境基线：JDK 21、Maven 3.9.16、Node.js 24.14.0、npm 11.9.0、MySQL 8、Neo4j 5.26（Docker）。

---

## 一、环境准备

### 1.1 必备工具

| 工具 | 版本 | 验证命令 |
| --- | --- | --- |
| JDK | 21 | `java -version` |
| Maven | 3.9.16 | `mvn -version` |
| Node.js | 24.14.0 | `node -v` |
| npm | 11.9.0 | `npm -v` |
| MySQL | 8.0 | `mysql --version` |
| Docker | 任意新版 | `docker --version` |

### 1.2 拉取代码

```bash
git clone <仓库地址> smartqa
cd smartqa
git checkout feature/modernization
```

---

## 二、数据库准备

### 2.1 MySQL 建库建表

```bash
# 创建数据库并导入表结构（11 张表 + 6 个外键）
mysql -h127.0.0.1 -uroot -p -e "CREATE DATABASE IF NOT EXISTS smartques DEFAULT CHARSET utf8mb4;"
mysql -h127.0.0.1 -uroot -p smartques < database/数据库.sql
```

### 2.2 BCrypt 密码字段迁移（幂等，可重复执行）

```bash
mysql -h127.0.0.1 -uroot -p smartques < database/migration-bcrypt-password.sql
```

> 作用：把 `web_user.password`、`wechat_users.password` 从 `varchar(20)` 扩容到 `varchar(100)`，容纳 60 字符的 BCrypt 哈希。重复执行无副作用。

### 2.3 （可选）导入开发测试数据

```bash
# 问答模板测试数据（question / question_template）
mysql -h127.0.0.1 -uroot -p smartques < database/dev-test-data.sql
```

---

## 三、Neo4j 5.26（Docker）

### 3.1 使用 Docker Compose（推荐）

在项目根目录创建 `docker-compose.yml`：

```yaml
services:
  neo4j:
    image: neo4j:5.26.0
    container_name: smartqa-neo4j
    ports:
      - "127.0.0.1:7474:7474"   # HTTP/Bolt
      - "127.0.0.1:7687:7687"   # Bolt
    environment:
      - NEO4J_AUTH=neo4j/你的密码
    volumes:
      - smartqa-neo4j-data-5:/data
    restart: unless-stopped

volumes:
  smartqa-neo4j-data-5:
```

启动：

```bash
docker compose up -d
```

### 3.2 手动 docker run（等价）

```bash
docker run -d --name smartqa-neo4j \
  -p 127.0.0.1:7474:7474 -p 127.0.0.1:7687:7687 \
  -e NEO4J_AUTH=neo4j/你的密码 \
  -v smartqa-neo4j-data-5:/data \
  neo4j:5.26.0
```

### 3.3 国内网络拉取镜像（可选）

```bash
docker pull docker.m.daocloud.io/neo4j:5.26.0
docker tag docker.m.daocloud.io/neo4j:5.26.0 neo4j:5.26.0
```

### 3.4 验证连接

```bash
curl -u neo4j:你的密码 -H "Content-Type: application/json" \
  -X POST http://127.0.0.1:7474/db/neo4j/tx/commit \
  -d '{"statements":[{"statement":"RETURN 1 AS x"}]}'
```

### 3.5 初始化示例图谱（可选，用于问答链路测试）

通过 Neo4j Browser（http://127.0.0.1:7474）执行 `database/dev-test-graph.cypher`，或用 cypher-shell。

---

## 四、环境变量配置

### 4.1 创建 .env

```bash
cp .env.example .env
```

编辑 `.env`，填入真实值：

```ini
DB_USER=root
DB_PASSWORD=你的MySQL密码
NEO4J_URI=bolt://127.0.0.1:7687
NEO4J_USER=neo4j
NEO4J_PASSWORD=你的Neo4j密码
INIT_ADMIN_ACCOUNT=admin
INIT_ADMIN_PASSWORD=你的初始管理员密码
INIT_ADMIN_NAME=超级管理员
INIT_ADMIN_PHONE=13800000000
```

> ⚠️ `.env` 已被 `.gitignore` 排除，**禁止提交**；`.env.example` 是模板，可提交。

---

## 五、后端启动

### 5.1 打包

```bash
cd backend
mvn clean package -DskipTests
```

### 5.2 启动

**方式一（Windows，推荐）**：双击项目根目录 `start-backend.bat`（自动加载 `.env`，运行 `target/*.jar`，端口 8081）。

**方式二（手动）**：

```bash
# Linux/macOS
cd backend
export $(grep -v '^#' ../.env | xargs)
java -jar target/springboot-0.0.1-SNAPSHOT.jar --server.port=8081

# Windows PowerShell
cd backend
Get-Content ..\.env | ForEach-Object { if ($_ -match '^([^#].*?)=(.*)$') { Set-Item "env:$($matches[1])" $matches[2] } }
java -jar target/springboot-0.0.1-SNAPSHOT.jar --server.port=8081
```

> 注意：若环境存在 `SERVER__PORT` 变量（部分 IDE/工具会注入），需先清除：`set SERVER__PORT=`（Windows）或 `unset SERVER__PORT`（Linux）。

### 5.3 验证启动

```bash
curl -X POST http://127.0.0.1:8081/web_login \
  -H "Content-Type: application/json" \
  -d '{"account":"admin","password":"你的密码","type":"1"}'
# 预期：{"success":true,"user":{...}}
```

---

## 六、前端启动

```bash
cd web
npm ci        # 或 npm install
npm run dev   # 开发模式，/api 代理到 127.0.0.1:8081，端口 8080
```

生产构建：

```bash
npm run build   # 产物在 web/dist/
```

访问 http://127.0.0.1:8080/login 用管理员账号登录。

---

## 七、HanLP 数据说明

本项目使用 **`hanlp-portable-1.7.2`（内置词典）**，**无需额外下载数据**。

- 配置文件 `backend/src/main/resources/hanlp.properties` 已设置 `IOAdapter=ResourceIOAdapter`，从 jar 内置资源读取词典。
- 如需**自定义外部词典**（data-for-1.7.5 完整包），才需下载并放置，来源：<https://github.com/hankcs/HanLP/releases>（`data-for-1.7.5.zip`）。
- `backend/hanlp-data/README.md` 为旧版说明（描述的是外部数据包方案），**当前默认方案是内置词典，不强制要求该目录**。

**验证方法**：启动后端后请求：

```bash
curl -X POST http://127.0.0.1:8081/apart \
  -H "Content-Type: application/json" \
  -d '{"text":"张三的职称"}'
# 若返回答案（而非 HanLP 初始化错误），则分词正常。
```

---

## 八、管理员初始化与改密

### 8.1 首次初始化

- 首次启动时，若 `web_user` 表为空，`AdminInitializer` 会用 `INIT_ADMIN_ACCOUNT`/`INIT_ADMIN_PASSWORD` **自动创建**初始超级管理员（adminType='1'，密码 BCrypt 哈希）。
- **幂等**：重复启动不会覆盖已有账号、不会重置密码（日志会输出「跳过」）。

### 8.2 修改密码

- 超级管理员登录后，在「管理员管理」页新增管理员。
- 修改自己密码：登录后进入「个人信息」页。
- 忘记密码：需配置榛子云短信（`ZHENZI_SMS_*`），未配置时返回「短信服务未配置」提示。

---

## 九、微信小程序

1. 用微信开发者工具导入 `miniprogram/` 目录。
2. 替换 `project.config.json` 中的 `appid`（当前为原作者占位值 `wxf13673eb599b2e89`）。
3. API 基地址在 `miniprogram/utils/config.js` 配置：
   - 开发者工具：`127.0.0.1:8081`
   - 真机调试：改为电脑局域网 IP（如 `192.168.1.100:8081`）
   - 发布：配置 HTTPS 合法域名后填 `PRODUCTION_BASE_URL`
4. 后端需已启动（8081）。

---

## 十、已知安全限制（重要）

- 🔴 **后端接口未实施身份认证与权限校验**：所有管理接口（增删改、管理员列表）无需凭证即可直接调用；前端 token 为本地会话标记，无服务端校验。
- 本项目定位为**课程开发演示**，**不可部署到公网/生产环境**。若需生产使用，必须补全后端会话鉴权（如 Spring Security + JWT）与服务端角色校验。

---

## 十一、Git 协作

- 分支：`feature/modernization`（不要直接改 main）。
- 提交前：`git status` 确认无 `.env`、`target/`、`dist/`、`node_modules/`、数据库备份混入。
- 推送：`git push -u origin feature/modernization`，然后在 GitHub 发起 Pull Request 供队友审核。
- **不强制推送、不覆盖 main、不自动合并。**
