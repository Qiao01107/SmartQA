# HanLP 数据目录说明

本项目使用 **`hanlp-portable-1.7.2`（内置词典版）**，词典数据已打包在 jar 的 `data/` 目录中，
通过 `hanlp.properties` 里的 `IOAdapter=ResourceIOAdapter` 从 classpath 直接读取。

## 当前默认方案（无需额外下载）

- **无需**在本目录放置任何词典文件即可运行，中文分词（`HanLP.segment`）开箱即用。
- 已通过实际问答链路验证（HanLP 分词 → Neo4j 图谱查询 → 答案生成）。

## 如需自定义外部词典（可选，非必需）

仅当需要扩充自定义词典时才需要下载完整数据包：

1. 从 <https://github.com/hankcs/HanLP/releases> 下载 `data-for-1.7.5.zip`（HanLP 1.7.x 数据包）。
2. 解压后把 `data/` 目录放到 `backend/hanlp-data/` 下。
3. 修改 `backend/src/main/resources/hanlp.properties`：
   - 取消注释 `root=./hanlp-data`
   - 将 `IOAdapter` 改回 `com.hankcs.hanlp.corpus.io.FileIOAdapter`

> 数据包来源必须是 HanLP 官方 release（GitHub），请勿使用来源不明的数据文件。

## fuzzyWords.txt（问答模糊词表）

`WeChatReplyService` 按空格拆分每行，把前面的词替换为最后一个词，例如：

```text
计算机 电脑
电脑 计算机
```

- 文件使用 UTF-8 编码。
- 若缺失：记录警告，跳过模糊词替换，问答流程仍可继续。
- 也可通过环境变量 `HANLP_FUZZY_WORDS` 指定路径。
