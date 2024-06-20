/*
 Navicat Premium Data Transfer

 Source Server         : book
 Source Server Type    : MySQL
 Source Server Version : 80037
 Source Host           : localhost:3306
 Source Schema         : librarydb

 Target Server Type    : MySQL
 Target Server Version : 80037
 File Encoding         : 65001

 Date: 20/06/2024 11:06:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for books
-- ----------------------------
DROP TABLE IF EXISTS `books`;
CREATE TABLE `books`  (
  `book_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `author` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `available` tinyint(1) NULL DEFAULT 1,
  `deleted` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`book_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of books
-- ----------------------------
INSERT INTO `books` VALUES (1, '图书大全', '王卓', 1, 0);
INSERT INTO `books` VALUES (2, '小说大全', '钟火花', 1, 0);
INSERT INTO `books` VALUES (3, '影视大全', '纪耀', 1, 0);
INSERT INTO `books` VALUES (4, '学习资料', '翁锋', 1, 0);
INSERT INTO `books` VALUES (5, '程序设计', '王欣', 1, 0);
INSERT INTO `books` VALUES (6, '故事大全', '王欣', 1, 0);
INSERT INTO `books` VALUES (7, '影', '345', 1, 0);
INSERT INTO `books` VALUES (8, '火', '345', 1, 0);
INSERT INTO `books` VALUES (9, '1', '123', 1, 0);

-- ----------------------------
-- Table structure for borrowedbooks
-- ----------------------------
DROP TABLE IF EXISTS `borrowedbooks`;
CREATE TABLE `borrowedbooks`  (
  `borrow_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NULL DEFAULT NULL,
  `book_id` int NULL DEFAULT NULL,
  `borrow_date` date NULL DEFAULT NULL,
  `return_date` date NULL DEFAULT NULL,
  PRIMARY KEY (`borrow_id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `book_id`(`book_id` ASC) USING BTREE,
  CONSTRAINT `borrowedbooks_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `borrowedbooks_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of borrowedbooks
-- ----------------------------
INSERT INTO `borrowedbooks` VALUES (1, 2, 1, '2024-06-19', '2024-06-20');
INSERT INTO `borrowedbooks` VALUES (2, 3, 5, '2024-06-20', '2024-06-20');
INSERT INTO `borrowedbooks` VALUES (3, 3, 9, '2024-06-20', '2024-06-20');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` enum('admin','user') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'user',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', 'adminpass', 'admin');
INSERT INTO `users` VALUES (2, 'user', '123456', 'user');
INSERT INTO `users` VALUES (3, 'wxz', 'wxz1234', 'user');

SET FOREIGN_KEY_CHECKS = 1;
