-- ============================================================
-- SmartQA 开发测试数据（MySQL）
-- 仅用于本地开发与联调测试，请勿导入生产环境。
-- 该脚本可重复执行（使用 INSERT IGNORE 或先清空测试标识数据）。
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- 1. 问答数据（question）
--    questionStatus = '1' 表示启用，'0' 表示停用
-- ------------------------------------------------------------
INSERT INTO question (id, question, answer, questionStatus) VALUES
  (1, '计算机由什么组成', '计算机由硬件系统和软件系统组成', '1'),
  (2, '什么是软件工程', '软件工程是一门研究用工程化方法构建和维护有效、实用和高质量软件的学科', '1'),
  (3, '数据结构有哪些', '常见数据结构包括数组、链表、栈、队列、树和图', '1')
ON DUPLICATE KEY UPDATE answer = VALUES(answer), questionStatus = VALUES(questionStatus);

-- ------------------------------------------------------------
-- 2. 问题模板数据（question_template）
--    questionTemplate 格式：词_权重#词_权重#...
--    权重累加 >= 0.6 时命中对应 question 的答案
-- ------------------------------------------------------------
INSERT INTO question_template (id, questionTemplate, preciseQuestion) VALUES
  (1, '计算机_0.4#组成_0.3#什么_0.1', '计算机由什么组成'),
  (2, '软件_0.4#工程_0.4', '什么是软件工程'),
  (3, '数据结构_0.5#哪些_0.2', '数据结构有哪些')
ON DUPLICATE KEY UPDATE questionTemplate = VALUES(questionTemplate), preciseQuestion = VALUES(preciseQuestion);

SET FOREIGN_KEY_CHECKS = 1;
