/*
 Navicat Premium Data Transfer

 Source Server         : ST
 Source Server Type    : MySQL
 Source Server Version : 50623
 Source Host           : 192.168.81.60:3306
 Source Schema         : fabricmanager_191

 Target Server Type    : MySQL
 Target Server Version : 50623
 File Encoding         : 65001

 Date: 25/04/2019 17:59:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for certificates
-- ----------------------------
DROP TABLE IF EXISTS `certificates`;
CREATE TABLE `certificates`  (
  `serial_number` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `authority_key_identifier` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `cert_pem` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `key_pem` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `ca_user_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `server_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `not_before` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `not_after` timestamp(0) NOT NULL DEFAULT '0000-00-00 00:00:00',
  `state` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`serial_number`, `authority_key_identifier`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for channel_config_task
-- ----------------------------
DROP TABLE IF EXISTS `channel_config_task`;
CREATE TABLE `channel_config_task`  (
  `request_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `channel_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `requester` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `content` blob NOT NULL,
  `description` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `channel_config_version` bigint(255) NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `request_time` datetime(0) NULL DEFAULT NULL,
  `request_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `expected_endorsement` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `reject` tinyint(1) NULL DEFAULT NULL,
  `reason` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `response_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`request_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for channel_config_task_response
-- ----------------------------
DROP TABLE IF EXISTS `channel_config_task_response`;
CREATE TABLE `channel_config_task_response`  (
  `request_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `responder` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `reject` tinyint(1) NOT NULL,
  `reason` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `signature` blob NULL,
  `response_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`request_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for container
-- ----------------------------
DROP TABLE IF EXISTS `container`;
CREATE TABLE `container`  (
  `host_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `container_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `container_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `image` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `tag` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `working_dir` varchar(4096) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `network_mode` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `volumes` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `exposed_port` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`host_name`, `container_id`) USING BTREE,
  CONSTRAINT `container_ibfk_1` FOREIGN KEY (`host_name`) REFERENCES `host` (`host_name`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for couchdb_node
-- ----------------------------
DROP TABLE IF EXISTS `couchdb_node`;
CREATE TABLE `couchdb_node`  (
  `couchdb_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `creator` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `host_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `container_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `peer_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `port` int(255) NULL DEFAULT NULL,
  `exposed_port` int(255) NULL DEFAULT NULL,
  PRIMARY KEY (`couchdb_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for fabric_ca_server
-- ----------------------------
DROP TABLE IF EXISTS `fabric_ca_server`;
CREATE TABLE `fabric_ca_server`  (
  `server_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `creator` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'creator',
  `host_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `container_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `port` int(255) NOT NULL,
  `exposed_port` int(255) NULL DEFAULT NULL,
  `home` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `parent_server` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `affiliations` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `tls_enable` tinyint(1) NOT NULL,
  `tls_ca` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `tls_server_cert` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `tls_server_key` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`server_name`) USING BTREE,
  INDEX `user_id`(`creator`) USING BTREE,
  CONSTRAINT `fabric_ca_server_ibfk_1` FOREIGN KEY (`creator`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for fabric_ca_user
-- ----------------------------
DROP TABLE IF EXISTS `fabric_ca_user`;
CREATE TABLE `fabric_ca_user`  (
  `user_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `server_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `secret` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `creator` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `owner` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `user_type` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `Identity_type` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `affiliation` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `attributes` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `state` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `max_enrollments` int(11) NULL DEFAULT NULL,
  `roles` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `tls_enable` tinyint(1) NOT NULL,
  `tls_cert` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `tls_key` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`user_id`, `server_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for host
-- ----------------------------
DROP TABLE IF EXISTS `host`;
CREATE TABLE `host`  (
  `host_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `protocol` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `ip` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `port` int(255) NOT NULL,
  `tls_enable` tinyint(1) NOT NULL,
  `cert_path` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`host_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for orderer_node
-- ----------------------------
DROP TABLE IF EXISTS `orderer_node`;
CREATE TABLE `orderer_node`  (
  `orderer_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `server_port` int(8) NOT NULL,
  `ca_server_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `ca_orderer_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `creator` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `host_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `container_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `system_chain` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`orderer_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for org_ref_channel
-- ----------------------------
DROP TABLE IF EXISTS `org_ref_channel`;
CREATE TABLE `org_ref_channel`  (
  `channel_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `org_msp` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `anchor_peers` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`channel_name`, `org_msp`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for organization
-- ----------------------------
DROP TABLE IF EXISTS `organization`;
CREATE TABLE `organization`  (
  `org_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `org_msp_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `org_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `tls_enable` tinyint(1) NOT NULL,
  `tls_ca_server` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `tls_ca_cert` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `tls_ca_key` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `orderer_ip` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `orderer_port` int(255) NULL DEFAULT NULL,
  `orderer_tls_cert` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`org_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for peer_node
-- ----------------------------
DROP TABLE IF EXISTS `peer_node`;
CREATE TABLE `peer_node`  (
  `peer_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `server_port` int(8) NOT NULL,
  `ca_server_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `ca_peer_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `creator` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `host_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `container_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `couchdb_enable` tinyint(1) NOT NULL,
  `couchdb_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`peer_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for peer_ref_channel
-- ----------------------------
DROP TABLE IF EXISTS `peer_ref_channel`;
CREATE TABLE `peer_ref_channel`  (
  `channel_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `peer_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`channel_name`, `peer_name`) USING BTREE,
  INDEX `channel_name`(`channel_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for register_code
-- ----------------------------
DROP TABLE IF EXISTS `register_code`;
CREATE TABLE `register_code`  (
  `code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `owner` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `generate_date` bigint(20) NOT NULL,
  PRIMARY KEY (`code`) USING BTREE,
  INDEX `register_user_owner_user_id`(`owner`) USING BTREE,
  CONSTRAINT `register_user_owner_user_id` FOREIGN KEY (`owner`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for remark
-- ----------------------------
DROP TABLE IF EXISTS `remark`;
CREATE TABLE `remark`  (
  `parent_user_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `user_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `remarks` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`parent_user_id`, `user_id`) USING BTREE,
  INDEX `remarks_user_user_id`(`user_id`) USING BTREE,
  CONSTRAINT `remarks_user_parent_user_id` FOREIGN KEY (`parent_user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `remarks_user_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for secret
-- ----------------------------
DROP TABLE IF EXISTS `secret`;
CREATE TABLE `secret`  (
  `user_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `question1` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `answer1` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `question2` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `answer2` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `question3` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `answer3` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`user_id`) USING BTREE,
  CONSTRAINT `secret_user_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `user_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `password` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `identity` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `affiliation` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `token` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `register_date` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

SET FOREIGN_KEY_CHECKS = 1;
