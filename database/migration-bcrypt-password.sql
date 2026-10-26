-- =============================================================
-- SmartQA 密码字段扩容迁移（BCrypt 兼容）
-- 目标：将 password 字段从 varchar(20) 扩容为 varchar(100)
--       以容纳 BCrypt 哈希（固定 60 字符）。
-- 说明：可重复执行（幂等），不会覆盖或修改已有数据。
--       执行前建议备份数据库。
-- 执行方式：mysql -h127.0.0.1 -uroot -p smartques < 本文件
-- =============================================================

-- 管理员表 web_user：password varchar(20) -> varchar(100)
ALTER TABLE `web_user`
    MODIFY COLUMN `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL;

-- 微信用户表 wechat_users：password varchar(20) -> varchar(100)
ALTER TABLE `wechat_users`
    MODIFY COLUMN `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL;

-- 迁移后确认（仅提示，不阻断）
SELECT 'web_user' AS tbl, CHARACTER_MAXIMUM_LENGTH AS pwd_len
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'smartques' AND TABLE_NAME = 'web_user' AND COLUMN_NAME = 'password'
UNION ALL
SELECT 'wechat_users', CHARACTER_MAXIMUM_LENGTH
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'smartques' AND TABLE_NAME = 'wechat_users' AND COLUMN_NAME = 'password';
