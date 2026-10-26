/*
 Navicat Premium Data Transfer

 Source Server         : XXX
 Source Server Type    : MySQL
 Source Server Version : 80024
 Source Host           : 47.113.226.5:3306
 Source Schema         : smartques

 Target Server Type    : MySQL
 Target Server Version : 80024
 File Encoding         : 65001

 Date: 05/01/2022 16:32:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chatting
-- ----------------------------
DROP TABLE IF EXISTS `chatting`;
CREATE TABLE `chatting`  (
  `phone` varchar(13) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `speaker` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `speakDate` datetime NOT NULL,
  INDEX `FK_CHATTING_REFERENCE_WECHAT_U`(`phone`) USING BTREE,
  CONSTRAINT `FK_CHATTING_REFERENCE_WECHAT_U` FOREIGN KEY (`phone`) REFERENCES `wechat_users` (`phone`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for entity
-- ----------------------------
DROP TABLE IF EXISTS `entity`;
CREATE TABLE `entity`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `label` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11008 CHARACTER SET = utf32 COLLATE = utf32_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for entity_relation
-- ----------------------------
DROP TABLE IF EXISTS `entity_relation`;
CREATE TABLE `entity_relation`  (
  `startEntity` int NOT NULL,
  `endEntity` int NOT NULL,
  `label` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `relation` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  INDEX `起始结点id`(`startEntity`) USING BTREE,
  INDEX `结束结点id`(`endEntity`) USING BTREE,
  CONSTRAINT `结束结点id` FOREIGN KEY (`endEntity`) REFERENCES `entity` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `起始结点id` FOREIGN KEY (`startEntity`) REFERENCES `entity` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for feedback_users
-- ----------------------------
DROP TABLE IF EXISTS `feedback_users`;
CREATE TABLE `feedback_users`  (
  `phone` varchar(13) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `opinion` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `question` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `feedbackTime` datetime NOT NULL,
  `evaluation` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  INDEX `FK_FEEDBACK_REFERENCE_WECHAT_U`(`phone`) USING BTREE,
  CONSTRAINT `FK_FEEDBACK_REFERENCE_WECHAT_U` FOREIGN KEY (`phone`) REFERENCES `wechat_users` (`phone`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for log
-- ----------------------------
DROP TABLE IF EXISTS `log`;
CREATE TABLE `log`  (
  `account` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `operation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `operationTime` datetime NULL DEFAULT NULL,
  `usedInterface` varchar(35) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `operationStatus` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  INDEX `FK_LOG_REFERENCE_WEB_USER`(`account`) USING BTREE,
  CONSTRAINT `FK_LOG_REFERENCE_WEB_USER` FOREIGN KEY (`account`) REFERENCES `web_user` (`account`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for question
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `question` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `answer` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `questionStatus` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 46 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for question_template
-- ----------------------------
DROP TABLE IF EXISTS `question_template`;
CREATE TABLE `question_template`  (
  `id` int NULL DEFAULT NULL,
  `questionTemplate` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `preciseQuestion` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  INDEX `问题id`(`id`) USING BTREE,
  CONSTRAINT `问题id` FOREIGN KEY (`id`) REFERENCES `question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for search_time
-- ----------------------------
DROP TABLE IF EXISTS `search_time`;
CREATE TABLE `search_time`  (
  `id` int NULL DEFAULT NULL,
  `question` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `reply` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `searchTime` datetime NOT NULL,
  INDEX `FK_SEARCH_T_REFERENCE_QUESTION`(`question`) USING BTREE,
  INDEX `问题的id`(`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_collectible
-- ----------------------------
DROP TABLE IF EXISTS `user_collectible`;
CREATE TABLE `user_collectible`  (
  `phone` varchar(13) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `question` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `reply` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `collectDate` datetime NULL DEFAULT NULL,
  INDEX `FK_USER_COL_REFERENCE_WECHAT_U`(`phone`) USING BTREE,
  CONSTRAINT `FK_USER_COL_REFERENCE_WECHAT_U` FOREIGN KEY (`phone`) REFERENCES `wechat_users` (`phone`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for web_user
-- ----------------------------
DROP TABLE IF EXISTS `web_user`;
CREATE TABLE `web_user`  (
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `account` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone` varchar(13) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `head` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `registerTime` datetime NOT NULL,
  `adminType` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`account`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for wechat_users
-- ----------------------------
DROP TABLE IF EXISTS `wechat_users`;
CREATE TABLE `wechat_users`  (
  `weChatName` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone` varchar(13) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `userStatus` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `registerTime` datetime NOT NULL,
  `profilePicture` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cause` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`phone`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
