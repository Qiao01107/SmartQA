# HanLP 数据目录（占位说明，不含词典数据）

本目录用于存放 HanLP 运行所需的数据。项目**没有**附带完整的 `data-for-1.7.5` 数据包，也没有伪造任何词典文件；请从合法的 HanLP 数据来源自行准备。

## 目录要求

将 HanLP 1.7.x 完整数据包的内容放到本目录，使运行时存在：

```text
backend/hanlp-data/
├── data/
│   ├── dictionary/
│   │   ├── CoreNatureDictionary.txt
│   │   ├── CoreNatureDictionary.ngram.txt
│   │   ├── CoreNatureDictionary.tr.txt
│   │   ├── custom/
│   │   │   └── CustomDictionary.txt
│   │   ├── person/
│   │   │   └── nrf.txt
│   │   └── ...
│   └── model/
│       └── ...
├── 现代汉语补充词库.txt        （可选，按 hanlp.properties 的 CustomDictionaryPath）
├── 全国地名大全.txt            （可选）
├── 人名词典.txt                （可选）
├── 机构名词典.txt              （可选）
├── My.txt                      （可选）
├── 上海地名.txt                （可选）
└── fuzzyWords.txt              （本项目问答模糊词表，见下）
```

`hanlp.properties` 默认：

```properties
root=./hanlp-data
```

如果从其他工作目录启动，或数据放在别处，请设置环境变量 `HANLP_ROOT`（HanLP 1.7.x 原生读取该系统属性/环境变量），不要修改成某台电脑的绝对路径。

## fuzzyWords.txt 格式

`WeChatReplyService` 按空格拆分每一行，把前面的词替换为最后一个词，例如：

```text
计算机 电脑
电脑 计算机
```

即“源词1 源词2 ... 目标词”。文件使用 UTF-8 编码。

也可以通过环境变量 `HANLP_FUZZY_WORDS` 指定该文件的绝对或相对路径。

## 缺失时的行为

- `fuzzyWords.txt` 缺失：记录明确警告，跳过模糊词替换，问答流程仍可继续（在 HanLP 词典完整的前提下）。
- HanLP 核心词典缺失：`NLPTokenizer.segment()` 会失败，`/apart` 返回明确的“HanLP 分词不可用”错误；不会伪造分词结果。此时需要补齐上述 `data/` 数据。
