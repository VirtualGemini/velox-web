-- Business IDs are stored as BIGINT; velox.id.snowflake.enabled=true uses Snowflake and false uses database sequences.
-- MySQL dump 10.13  Distrib 9.6.0, for macos15.7 (arm64)
--
-- Host: 127.0.0.1    Database: velox
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Database: `velox`
--

DROP DATABASE IF EXISTS `velox`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE DATABASE IF NOT EXISTS `velox` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Use the database `velox`
--

USE `velox`;

--
-- Character set and collation for the database
--

SET NAMES utf8mb4;

--
-- Table structure for table `sys_id_sequence`
--

DROP TABLE IF EXISTS `sys_id_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_id_sequence` (
  `business_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Business Type',
  `current_value` bigint NOT NULL DEFAULT '0' COMMENT 'Current Allocated Sequence',
  `create_time` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated Time',
  PRIMARY KEY (`business_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Business ID Sequence Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_id_sequence`
--

/*!40000 ALTER TABLE `sys_id_sequence` DISABLE KEYS */;
INSERT INTO `sys_id_sequence` (`business_type`, `current_value`) VALUES
  ('default',0),
  ('sys_account',0),
  ('sys_role',0),
  ('sys_menu',0),
  ('sys_profile',0),
  ('sys_account_role',0),
  ('sys_account_session',0),
  ('sys_account_security',0),
  ('sys_role_menu_permission',0),
  ('sys_file_config',0),
  ('sys_file',0),
  ('sys_file_content',0);
/*!40000 ALTER TABLE `sys_id_sequence` ENABLE KEYS */;

--
-- Table structure for table `sys_file_config`
--

DROP TABLE IF EXISTS `sys_file_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_file_config` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Config Name',
  `storage` int NOT NULL COMMENT 'Storage Type (1-Database 10-Local Disk 11-FTP 12-SFTP 20-S3 Cloud Storage)',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Remark',
  `master` tinyint DEFAULT '0' COMMENT 'Is Master Config (0-No 1-Yes)',
  `config` json DEFAULT NULL COMMENT 'Storage Config (JSON)',
  `enabled` tinyint DEFAULT '1' COMMENT 'Enabled Status (0-Disabled 1-Enabled)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  PRIMARY KEY (`id`),
  KEY `idx_sys_file_config_storage` (`storage`),
  KEY `idx_sys_file_config_master` (`master`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='File Config Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_file_config`
--

/*!40000 ALTER TABLE `sys_file_config` DISABLE KEYS */;
INSERT INTO `sys_file_config` VALUES
  ('1900000000000000090','Database Storage',1,'Database-stored files',0,'{\"domain\": \"http://127.0.0.1:3006\"}',1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000040','Local Storage',10,'Local disk storage',1,'{\"domain\": \"http://127.0.0.1:3006\", \"basePath\": \"/tmp/uploads\"}',1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000041','S3 Cloud Storage',20,'S3/OSS cloud storage (currently tested only with Alibaba Cloud; in theory all S3-compatible providers are supported. If you encounter issues, please submit an issue to help us improve and optimize.)',0,'{\"bucket\": \"bucket\", \"endpoint\": \"oss-cn-beijing.aliyuncs.com\", \"accessKey\": \"Your Access Key\", \"accessSecret\": \"Your Access Secret\", \"enablePublicAccess\": true, \"enablePathStyleAccess\": false}',1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000011','FTP Storage',11,'FTP file storage example',0,'{\"host\": \"127.0.0.1\", \"port\": 21, \"username\": \"ftp-user\", \"password\": \"ftp-password\", \"mode\": \"Passive\", \"basePath\": \"/uploads\", \"domain\": \"http://127.0.0.1:3006\"}',0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000072','SFTP Storage',12,'SFTP file storage example',0,'{\"host\": \"127.0.0.1\", \"port\": 22, \"username\": \"sftp-user\", \"password\": \"sftp-password\", \"basePath\": \"/uploads\", \"domain\": \"http://127.0.0.1:3006\"}',0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_file_config` ENABLE KEYS */;

--
-- Table structure for table `sys_file`
--

DROP TABLE IF EXISTS `sys_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_file` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `config_id` bigint NOT NULL COMMENT 'File Config ID',
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'File Name',
  `path` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'File Path',
  `url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'File URL',
  `type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'File Type',
  `size` bigint DEFAULT '0' COMMENT 'File Size (Bytes)',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Upload Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  PRIMARY KEY (`id`),
  KEY `idx_sys_file_config_id` (`config_id`),
  KEY `idx_sys_file_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='File Main Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_file_content`
--

DROP TABLE IF EXISTS `sys_file_content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_file_content` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `config_id` bigint NOT NULL COMMENT 'File Config ID',
  `path` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'File Path',
  `content` mediumblob NOT NULL COMMENT 'File Content (Binary)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  PRIMARY KEY (`id`),
  KEY `idx_sys_file_content_config_id` (`config_id`),
  KEY `idx_sys_file_content_path` (`path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='File Content Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `role_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Role Name',
  `role_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Role Code',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Role Description',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT 'Role Type: 0-System Role, 1-Custom Role',
  `role_level` int NOT NULL DEFAULT '0' COMMENT 'Role Level, higher value means higher privileges',
  `enabled` tinyint DEFAULT '1' COMMENT 'Enabled Status (0-Disabled 1-Enabled)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  `active_role_code` varchar(50) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((case when (`deleted` = 0) then `role_code` else NULL end)) STORED COMMENT 'Active Role Code',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`active_role_code`),
  KEY `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System Role Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `type`, `role_level`, `enabled`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  ('1900000000000000023','Super Administrator','R_SUPER','Has all system permissions',0,3,1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000073','Administrator','R_ADMIN','Has system administration permissions',0,2,1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000032','Regular User','R_USER','Has regular system permissions',0,1,1,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;

--
-- Table structure for table `sys_account`
--

DROP TABLE IF EXISTS `sys_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_account` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Username',
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Password',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Account Remark',
  `status` tinyint DEFAULT '1' COMMENT 'Account status (1-Enabled 2-Disabled 3-Abnormal 4-Cancelled)',
  `login_fail_count` int DEFAULT '0' COMMENT 'Login Failure Count',
  `login_fail_time` datetime DEFAULT NULL COMMENT 'Login Failure Time',
  `deletion_requested_at` datetime DEFAULT NULL COMMENT 'Deletion Requested At',
  `deletion_expires_at` datetime DEFAULT NULL COMMENT 'Deletion Cooling-Off Expires At',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  `active_username` varchar(50) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((case when (`deleted` = 0) then `username` else NULL end)) STORED COMMENT 'Active Username',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`active_username`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System Account Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_account`
--

/*!40000 ALTER TABLE `sys_account` DISABLE KEYS */;
INSERT INTO `sys_account` (`id`, `username`, `password`, `remark`, `status`, `login_fail_count`, `login_fail_time`, `deletion_requested_at`, `deletion_expires_at`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  ('1900000000000000027','Super','{bcrypt}$2a$12$w9oNXS.wlWOfoHg6vMVKKuRHIASL5uTI2BtybrocyvQCMmeSIhshu','Super administrator account',1,0,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000087','Admin','{bcrypt}$2a$12$w9oNXS.wlWOfoHg6vMVKKuRHIASL5uTI2BtybrocyvQCMmeSIhshu','Administrator account',1,0,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000048','User','{bcrypt}$2a$12$w9oNXS.wlWOfoHg6vMVKKuRHIASL5uTI2BtybrocyvQCMmeSIhshu','Default member account',1,0,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_account` ENABLE KEYS */;

--
-- Table structure for table `sys_profile`
--

DROP TABLE IF EXISTS `sys_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_profile` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `account_id` bigint NOT NULL COMMENT 'Account ID',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Nickname',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Avatar',
  `gender` tinyint DEFAULT '0' COMMENT 'Gender (0-Unknown 1-Male 2-Female 3-Other)',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Full Name',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Display Email',
  `mobile` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Display Mobile',
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Address',
  `introduction` text COLLATE utf8mb4_unicode_ci COMMENT 'Bio',
  `signature` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Signature',
  `position` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Position',
  `company` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Company',
  `tags` json DEFAULT NULL COMMENT 'Tags',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_id` (`account_id`),
  CONSTRAINT `fk_sys_profile_account_id` FOREIGN KEY (`account_id`) REFERENCES `sys_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User Profile Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_profile`
--

/*!40000 ALTER TABLE `sys_profile` DISABLE KEYS */;
INSERT INTO `sys_profile` VALUES
  ('1900000000000000075','1900000000000000027','Virtual Gemini','https://api.dicebear.com/7.x/avataaars/svg?seed=Super',2,'Prefer not to disclose','super.public@velox.web','15800158001','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','[\"Cute\", \"Very Cute\", \"Extremely Cute\"]','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000057','1900000000000000087','Virtual Gemini','https://api.dicebear.com/7.x/avataaars/svg?seed=Admin',1,'Prefer not to disclose','admin.public@velox.web','15800158002','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','[\"Cute\", \"Very Cute\", \"Extremely Cute\"]','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000014','1900000000000000048','Virtual Gemini','https://api.dicebear.com/7.x/avataaars/svg?seed=User',1,'Prefer not to disclose','user.public@velox.web','15800158003','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','[\"Cute\", \"Very Cute\", \"Extremely Cute\"]','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_profile` ENABLE KEYS */;

--
-- Table structure for table `sys_account_role`
--

DROP TABLE IF EXISTS `sys_account_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_account_role` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `account_id` bigint NOT NULL COMMENT 'Account ID',
  `role_id` bigint NOT NULL COMMENT 'Role ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  `active_account_role` tinyint GENERATED ALWAYS AS ((case when (`deleted` = 0) then 1 else NULL end)) STORED COMMENT 'Active Relation Marker',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_role` (`account_id`,`role_id`,`active_account_role`),
  KEY `idx_account_role_account_id` (`account_id`),
  KEY `idx_user_role_role_id` (`role_id`),
  CONSTRAINT `fk_sys_account_role_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`),
  CONSTRAINT `fk_sys_account_role_user_id` FOREIGN KEY (`account_id`) REFERENCES `sys_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User Role Relation Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_account_session`
--

DROP TABLE IF EXISTS `sys_account_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_account_session` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `account_id` bigint NOT NULL COMMENT 'Account ID',
  `token_hash` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Token Digest',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT 'Session Status (1-online 2-logged-out)',
  `login_time` datetime DEFAULT NULL COMMENT 'Login Time',
  `last_active_time` datetime DEFAULT NULL COMMENT 'Last Active Time',
  `logout_time` datetime DEFAULT NULL COMMENT 'Logout Time',
  `presence_expire_time` datetime DEFAULT NULL COMMENT 'Presence Expire Time',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT 'Logical Delete (0-active 1-deleted)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_session_token_hash` (`token_hash`),
  KEY `idx_account_session_account_id` (`account_id`),
  KEY `idx_user_session_presence_expire_time` (`presence_expire_time`),
  CONSTRAINT `fk_sys_account_session_user_id` FOREIGN KEY (`account_id`) REFERENCES `sys_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User Login Session Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_account_role`
--

/*!40000 ALTER TABLE `sys_account_role` DISABLE KEYS */;
INSERT INTO `sys_account_role` (`id`, `account_id`, `role_id`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  ('1900000000000000021','1900000000000000027','1900000000000000023','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000012','1900000000000000087','1900000000000000073','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000024','1900000000000000048','1900000000000000032','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_account_role` ENABLE KEYS */;

--
-- Table structure for table `sys_account_security`
--

DROP TABLE IF EXISTS `sys_account_security`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_account_security` (
  `id` bigint NOT NULL COMMENT 'Primary key',
  `account_id` bigint NOT NULL COMMENT 'Account ID',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Bound email',
  `login_methods` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT 'password' COMMENT 'Allowed login methods (comma-separated: password,email_code)',
  `disabled_login_methods` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Admin-disabled login methods (comma-separated); user cannot enable or use',
  `disabled_oauth_channels` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Admin-disabled third-party channels (comma-separated); user cannot enable or use',
  `mfa_email_enabled` tinyint DEFAULT '0' COMMENT 'Email MFA enabled',
  `mfa_totp_enabled` tinyint DEFAULT '0' COMMENT 'TOTP MFA enabled (placeholder)',
  `mfa_totp_secret` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'TOTP secret (placeholder)',
  `mfa_totp_recovery_codes` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'TOTP recovery code hash list (comma-separated)',
  `email_verified_at` datetime DEFAULT NULL COMMENT 'Email last verification time',
  `last_password_change_at` datetime DEFAULT NULL COMMENT 'Last password change time',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `create_by` bigint DEFAULT NULL COMMENT 'Creator',
  `update_by` bigint DEFAULT NULL COMMENT 'Updater',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical delete (0-active 1-deleted)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_security_account_id` (`account_id`),
  CONSTRAINT `fk_sys_account_security_user_id` FOREIGN KEY (`account_id`) REFERENCES `sys_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Account security configuration';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_account_security`
--

/*!40000 ALTER TABLE `sys_account_security` DISABLE KEYS */;
INSERT INTO `sys_account_security` (`id`, `account_id`, `email`, `login_methods`, `mfa_email_enabled`, `mfa_totp_enabled`, `mfa_totp_secret`, `mfa_totp_recovery_codes`, `email_verified_at`, `last_password_change_at`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  ('1900000000000000126','1900000000000000027','super@velox.web','password',0,0,NULL,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000127','1900000000000000087','admin@velox.web','password',0,0,NULL,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000128','1900000000000000048','user@velox.web','password',0,0,NULL,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_account_security` ENABLE KEYS */;

--
--
-- Table structure for table `sys_access_control`
--

DROP TABLE IF EXISTS `sys_access_control`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_access_control` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `general_register_enabled` tinyint DEFAULT '1' COMMENT 'General Registration Switch (0-Off 1-On)',
  `forgot_password_enabled` tinyint DEFAULT '1' COMMENT 'Forgot Password Switch (0-Off 1-On)',
  `login_methods` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Enabled normal login methods (comma-separated)',
  `third_party_login_channels` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Enabled third-party login channels (comma-separated)',
  `third_party_register_channels` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Enabled third-party registration channels (comma-separated)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Access Control Config Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_access_control`
--

/*!40000 ALTER TABLE `sys_access_control` DISABLE KEYS */;
INSERT INTO `sys_access_control` (`id`, `general_register_enabled`, `forgot_password_enabled`, `login_methods`, `third_party_login_channels`, `third_party_register_channels`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  ('1900000000000002010',1,1,'password,email_code','github,linuxdo','github,linuxdo','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_access_control` ENABLE KEYS */;

-- Table structure for table `sys_menu`
--

DROP TABLE IF EXISTS `sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `parent_id` bigint DEFAULT NULL COMMENT 'Parent Menu ID',
  `menu_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Menu Type (menu/button)',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Route Name or Permission Name',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Menu Title or Button Title',
  `path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Route Path',
  `component` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Component Path',
  `redirect` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Redirect Path',
  `icon` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Icon',
  `auth_mark` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Auth Mark',
  `is_enable` tinyint DEFAULT '1' COMMENT 'Is Enabled',
  `sort` int DEFAULT '1' COMMENT 'Sort Order',
  `keep_alive` tinyint DEFAULT '0' COMMENT 'Keep Alive',
  `is_hide` tinyint DEFAULT '0' COMMENT 'Hide Menu',
  `is_hide_tab` tinyint DEFAULT '0' COMMENT 'Hide Tab',
  `link` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'External Link',
  `is_iframe` tinyint DEFAULT '0' COMMENT 'Is Iframe',
  `show_badge` tinyint DEFAULT '0' COMMENT 'Show Badge',
  `show_text_badge` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Text Badge',
  `fixed_tab` tinyint DEFAULT '0' COMMENT 'Is Fixed Tab',
  `active_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Active Menu Path',
  `is_full_page` tinyint DEFAULT '0' COMMENT 'Is Fullscreen Page',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  `active_auth_mark` varchar(100) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((case when (`deleted` = 0) then `auth_mark` else NULL end)) STORED COMMENT 'Active Auth Mark',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_auth_mark` (`active_auth_mark`),
  KEY `idx_menu_parent_id` (`parent_id`),
  KEY `idx_menu_auth_mark` (`auth_mark`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System Menu Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_menu`
--

/*!40000 ALTER TABLE `sys_menu` DISABLE KEYS */;
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `name`, `title`, `path`, `component`, `redirect`, `icon`, `auth_mark`, `is_enable`, `sort`, `keep_alive`, `is_hide`, `is_hide_tab`, `link`, `is_iframe`, `show_badge`, `show_text_badge`, `fixed_tab`, `active_path`, `is_full_page`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  -- Dashboard
  ('1900000000000000077',NULL,'menu','Dashboard','menus.dashboard.title','/dashboard','/index/index',NULL,'ri:pie-chart-line',NULL,1,0,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000089','1900000000000000077','menu','Console','menus.dashboard.console','console','/dashboard/console',NULL,NULL,'dashboard:console',1,1,0,0,0,NULL,0,0,NULL,1,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000200','1900000000000000089','button','ConsoleView','Page Access',NULL,NULL,NULL,NULL,'dashboard:console:view',1,0,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System
  ('1900000000000000080',NULL,'menu','System','menus.system.title','/system','/index/index',NULL,'ri:user-3-line',NULL,1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > Account
  ('1900000000000000016','1900000000000000080','menu','Account','menus.system.account','account','/system/account',NULL,NULL,NULL,1,1,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000201','1900000000000000016','button','AccountView','Page Access',NULL,NULL,NULL,NULL,'system:account:view',1,0,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000007','1900000000000000016','button','AccountQuery','Query',NULL,NULL,NULL,NULL,'system:account:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000020','1900000000000000016','button','AccountCreate','Create',NULL,NULL,NULL,NULL,'system:account:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000060','1900000000000000016','button','AccountUpdate','Edit',NULL,NULL,NULL,NULL,'system:account:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000030','1900000000000000016','button','AccountDelete','Delete',NULL,NULL,NULL,NULL,'system:account:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > Role
  ('1900000000000000046','1900000000000000080','menu','Role','menus.system.role','role','/system/role',NULL,NULL,NULL,1,2,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000202','1900000000000000046','button','RoleView','Page Access',NULL,NULL,NULL,NULL,'system:role:view',1,0,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000085','1900000000000000046','button','RoleQuery','Query',NULL,NULL,NULL,NULL,'system:role:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000004','1900000000000000046','button','RoleCreate','Create',NULL,NULL,NULL,NULL,'system:role:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000066','1900000000000000046','button','RoleUpdate','Edit',NULL,NULL,NULL,NULL,'system:role:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000092','1900000000000000046','button','RoleDelete','Delete',NULL,NULL,NULL,NULL,'system:role:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000053','1900000000000000046','button','RolePermission','Menu Permission',NULL,NULL,NULL,NULL,'system:role:permission',1,5,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > AccountCenter
  ('1900000000000000035','1900000000000000080','menu','AccountCenter','menus.system.accountCenter','account-center','/system/account-center',NULL,NULL,NULL,1,3,1,1,1,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000001035','1900000000000000035','menu','AccountCenterProfileGroup','Profile',NULL,NULL,NULL,NULL,NULL,1,1,0,1,1,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000106','1900000000000001035','button','AccountCenterProfileQuery','Query',NULL,NULL,NULL,NULL,'system:account-center:profile-query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000096','1900000000000001035','button','AccountCenterAvatarUpload','Avatar Upload',NULL,NULL,NULL,NULL,'system:account-center:avatar-upload',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000094','1900000000000001035','button','AccountCenterProfileUpdate','Profile Update',NULL,NULL,NULL,NULL,'system:account-center:profile-update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000001036','1900000000000000035','menu','AccountCenterAccountGroup','Account Settings',NULL,NULL,NULL,NULL,NULL,1,2,0,1,1,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000130','1900000000000001036','button','AccountCenterAccountQuery','Query',NULL,NULL,NULL,NULL,'system:account-center:account-query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000131','1900000000000001036','button','AccountCenterUsernameUpdate','Username Update',NULL,NULL,NULL,NULL,'system:account-center:username-update',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000132','1900000000000001036','button','AccountCenterAccountDelete','Account Delete',NULL,NULL,NULL,NULL,'system:account-center:account-delete',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000133','1900000000000001036','button','AccountCenterAccountRecover','Account Recover',NULL,NULL,NULL,NULL,'system:account-center:account-recover',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000001037','1900000000000000035','menu','AccountCenterSecurityGroup','Security',NULL,NULL,NULL,NULL,NULL,1,3,0,1,1,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000110','1900000000000001037','button','AccountCenterSecurityQuery','Query',NULL,NULL,NULL,NULL,'system:account-center:security-query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000095','1900000000000001037','button','AccountCenterPasswordReset','Password Reset',NULL,NULL,NULL,NULL,'system:account-center:password-reset',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000112','1900000000000001037','button','AccountCenterEmailRebind','Email Rebind',NULL,NULL,NULL,NULL,'system:account-center:email-rebind',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000134','1900000000000001037','button','AccountCenterEmailUnbind','Unbind',NULL,NULL,NULL,NULL,'system:account-center:email-unbind',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000111','1900000000000001037','button','AccountCenterLoginMethodManage','Login Method Management',NULL,NULL,NULL,NULL,'system:account-center:login-method-manage',1,5,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000113','1900000000000001037','button','AccountCenterMfaManage','MFA Management',NULL,NULL,NULL,NULL,'system:account-center:mfa-manage',1,6,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000001038','1900000000000000035','menu','AccountCenterThirdPartyGroup','Third-Party Accounts',NULL,NULL,NULL,NULL,NULL,1,4,0,1,1,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000114','1900000000000001038','button','AccountCenterThirdPartyQuery','Query',NULL,NULL,NULL,NULL,'system:account-center:third-party-query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000115','1900000000000001038','button','AccountCenterThirdPartyBind','Bind Account',NULL,NULL,NULL,NULL,'system:account-center:third-party-bind',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000001039','1900000000000001038','button','AccountCenterThirdPartyUnbind','Unbind Account',NULL,NULL,NULL,NULL,'system:account-center:third-party-unbind',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > Menus
  ('1900000000000000063','1900000000000000080','menu','Menus','menus.system.menu','menu','/system/menu',NULL,NULL,NULL,1,4,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000203','1900000000000000063','button','MenusView','Page Access',NULL,NULL,NULL,NULL,'system:menu:view',1,0,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000086','1900000000000000063','button','MenusQuery','Query',NULL,NULL,NULL,NULL,'system:menu:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000084','1900000000000000063','button','MenusCreate','Create',NULL,NULL,NULL,NULL,'system:menu:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000042','1900000000000000063','button','MenusUpdate','Edit',NULL,NULL,NULL,NULL,'system:menu:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000071','1900000000000000063','button','MenusDelete','Delete',NULL,NULL,NULL,NULL,'system:menu:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > FileManage
  ('1900000000000000038','1900000000000000080','menu','FileManage','menus.system.fileManage','file-manage','/system/file-manage',NULL,'',NULL,1,5,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000204','1900000000000000038','button','FileManageView','Page Access',NULL,NULL,NULL,NULL,'system:file:view',1,0,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000008','1900000000000000038','button','FileQuery','Query',NULL,NULL,NULL,NULL,'system:file:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000064','1900000000000000038','button','FileCreate','Create',NULL,NULL,NULL,NULL,'system:file:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000093','1900000000000000038','button','FileUpdate','Edit',NULL,NULL,NULL,NULL,'system:file:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000069','1900000000000000038','button','FileDelete','Delete',NULL,NULL,NULL,NULL,'system:file:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000083','1900000000000000038','button','FileDownload','Download',NULL,NULL,NULL,NULL,'system:file:download',1,5,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000006','1900000000000000038','button','FileUpload','Upload',NULL,NULL,NULL,NULL,'system:file:upload',1,6,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Config
  ('1900000000000000029',NULL,'menu','Config','menus.config.title','/config','/index/index',NULL,'ri:settings-3-line',NULL,1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Config > FileConfig
  ('1900000000000000003','1900000000000000029','menu','FileConfig','menus.config.fileConfig','file-config','/config/file-config',NULL,'',NULL,1,1,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000205','1900000000000000003','button','FileConfigView','Page Access',NULL,NULL,NULL,NULL,'system:file-config:view',1,0,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000062','1900000000000000003','button','FileConfigQuery','Query',NULL,NULL,NULL,NULL,'system:file-config:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000039','1900000000000000003','button','FileConfigCreate','Create',NULL,NULL,NULL,NULL,'system:file-config:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000076','1900000000000000003','button','FileConfigUpdate','Edit',NULL,NULL,NULL,NULL,'system:file-config:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000044','1900000000000000003','button','FileConfigDelete','Delete',NULL,NULL,NULL,NULL,'system:file-config:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Settings
  ('1900000000000002001',NULL,'menu','Settings','menus.settings.title','/settings','/index/index',NULL,'ri:settings-4-line',NULL,1,90,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- Settings > AccessControl
  ('1900000000000002002','1900000000000002001','menu','AccessControl','menus.settings.accessControl','access-control','/settings/access-control',NULL,NULL,NULL,1,1,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000206','1900000000000002002','button','AccessControlView','Page Access',NULL,NULL,NULL,NULL,'settings:access-control:view',1,0,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002003','1900000000000002002','button','AccessControlQuery','Query',NULL,NULL,NULL,NULL,'settings:access-control:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002004','1900000000000002002','button','AccessControlGeneralRegister','General Registration',NULL,NULL,NULL,NULL,'settings:access-control:general-register',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002005','1900000000000002002','button','AccessControlThirdPartyRegister','Third-party Registration',NULL,NULL,NULL,NULL,'settings:access-control:third-party-register',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002006','1900000000000002002','button','AccessControlLoginMethod','Login Methods',NULL,NULL,NULL,NULL,'settings:access-control:login-method',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002007','1900000000000002002','button','AccessControlThirdPartyLogin','Third-party Login',NULL,NULL,NULL,NULL,'settings:access-control:third-party-login',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002008','1900000000000002002','button','AccessControlForgotPassword','Forgot Password',NULL,NULL,NULL,NULL,'settings:access-control:forgot-password',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_menu` ENABLE KEYS */;

--
-- Table structure for table `sys_role_menu_permission`
--

DROP TABLE IF EXISTS `sys_role_menu_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menu_permission` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `role_id` bigint NOT NULL COMMENT 'Role ID',
  `menu_id` bigint NOT NULL COMMENT 'Menu/Button ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  `active_role_menu_permission` tinyint GENERATED ALWAYS AS ((case when (`deleted` = 0) then 1 else NULL end)) STORED COMMENT 'Active Relation Marker',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu_permission` (`role_id`,`menu_id`,`active_role_menu_permission`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`),
  CONSTRAINT `fk_sys_role_menu_permission_menu_id` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`),
  CONSTRAINT `fk_sys_role_menu_permission_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role Menu Permission Relation Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_menu_permission`
--

/*!40000 ALTER TABLE `sys_role_menu_permission` DISABLE KEYS */;
INSERT INTO `sys_role_menu_permission` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  -- R_SUPER -> Dashboard
  ('1900000000000000052','1900000000000000023','1900000000000000077','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000031','1900000000000000023','1900000000000000089','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002030','1900000000000000023','1900000000000000200','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_SUPER -> System
  ('1900000000000000068','1900000000000000023','1900000000000000080','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_SUPER -> System > Account
  ('1900000000000000043','1900000000000000023','1900000000000000016','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002031','1900000000000000023','1900000000000000201','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000002','1900000000000000023','1900000000000000007','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000082','1900000000000000023','1900000000000000020','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000009','1900000000000000023','1900000000000000060','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000036','1900000000000000023','1900000000000000030','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_SUPER -> System > Role
  ('1900000000000000088','1900000000000000023','1900000000000000046','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002032','1900000000000000023','1900000000000000202','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000056','1900000000000000023','1900000000000000085','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000018','1900000000000000023','1900000000000000004','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000019','1900000000000000023','1900000000000000066','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000034','1900000000000000023','1900000000000000092','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000049','1900000000000000023','1900000000000000053','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_SUPER -> System > AccountCenter
  ('1900000000000000097','1900000000000000023','1900000000000000094','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000098','1900000000000000023','1900000000000000095','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000099','1900000000000000023','1900000000000000096','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000107','1900000000000000023','1900000000000000106','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000114','1900000000000000023','1900000000000000110','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000115','1900000000000000023','1900000000000000111','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000116','1900000000000000023','1900000000000000112','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000146','1900000000000000023','1900000000000000134','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000117','1900000000000000023','1900000000000000113','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000134','1900000000000000023','1900000000000000130','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000135','1900000000000000023','1900000000000000131','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000136','1900000000000000023','1900000000000000132','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000137','1900000000000000023','1900000000000000133','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_SUPER -> System > Menus
  ('1900000000000000037','1900000000000000023','1900000000000000063','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002033','1900000000000000023','1900000000000000203','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000058','1900000000000000023','1900000000000000086','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000074','1900000000000000023','1900000000000000084','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000045','1900000000000000023','1900000000000000042','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000047','1900000000000000023','1900000000000000071','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_SUPER -> System > FileManage
  ('1900000000000000065','1900000000000000023','1900000000000000038','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002034','1900000000000000023','1900000000000000204','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000050','1900000000000000023','1900000000000000008','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000001','1900000000000000023','1900000000000000064','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000026','1900000000000000023','1900000000000000093','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000013','1900000000000000023','1900000000000000069','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000025','1900000000000000023','1900000000000000083','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000054','1900000000000000023','1900000000000000006','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_SUPER -> Config
  ('1900000000000000061','1900000000000000023','1900000000000000029','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_SUPER -> Config > FileConfig
  ('1900000000000000059','1900000000000000023','1900000000000000003','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002035','1900000000000000023','1900000000000000205','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000055','1900000000000000023','1900000000000000062','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000017','1900000000000000023','1900000000000000039','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000067','1900000000000000023','1900000000000000076','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000081','1900000000000000023','1900000000000000044','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_ADMIN -> Dashboard
  ('1900000000000000028','1900000000000000073','1900000000000000077','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000033','1900000000000000073','1900000000000000089','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002040','1900000000000000073','1900000000000000200','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_ADMIN -> System
  ('1900000000000000022','1900000000000000073','1900000000000000080','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_ADMIN -> System > Account
  ('1900000000000000015','1900000000000000073','1900000000000000016','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002041','1900000000000000073','1900000000000000201','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000091','1900000000000000073','1900000000000000007','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000078','1900000000000000073','1900000000000000020','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000079','1900000000000000073','1900000000000000060','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000051','1900000000000000073','1900000000000000030','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_ADMIN -> System > AccountCenter
  ('1900000000000000100','1900000000000000073','1900000000000000094','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000101','1900000000000000073','1900000000000000095','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000102','1900000000000000073','1900000000000000096','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000108','1900000000000000073','1900000000000000106','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000118','1900000000000000073','1900000000000000110','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000119','1900000000000000073','1900000000000000111','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000120','1900000000000000073','1900000000000000112','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000147','1900000000000000073','1900000000000000134','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000121','1900000000000000073','1900000000000000113','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000138','1900000000000000073','1900000000000000130','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000139','1900000000000000073','1900000000000000131','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000140','1900000000000000073','1900000000000000132','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000141','1900000000000000073','1900000000000000133','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_ADMIN -> System > Role
  ('1900000000000002042','1900000000000000073','1900000000000000046','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002043','1900000000000000073','1900000000000000202','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002044','1900000000000000073','1900000000000000085','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002045','1900000000000000073','1900000000000000004','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002046','1900000000000000073','1900000000000000066','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002047','1900000000000000073','1900000000000000092','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002048','1900000000000000073','1900000000000000053','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_ADMIN -> System > FileManage
  ('1900000000000002049','1900000000000000073','1900000000000000038','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002050','1900000000000000073','1900000000000000204','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002051','1900000000000000073','1900000000000000008','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002052','1900000000000000073','1900000000000000064','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002053','1900000000000000073','1900000000000000093','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002054','1900000000000000073','1900000000000000069','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002055','1900000000000000073','1900000000000000083','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002056','1900000000000000073','1900000000000000006','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_USER -> Dashboard
  ('1900000000000002057','1900000000000000032','1900000000000000077','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002058','1900000000000000032','1900000000000000089','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002059','1900000000000000032','1900000000000000200','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_USER -> System
  ('1900000000000002060','1900000000000000032','1900000000000000080','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_USER -> System > Account
  ('1900000000000002061','1900000000000000032','1900000000000000016','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002062','1900000000000000032','1900000000000000201','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002063','1900000000000000032','1900000000000000007','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_USER -> System > Role
  ('1900000000000002064','1900000000000000032','1900000000000000046','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002065','1900000000000000032','1900000000000000202','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002066','1900000000000000032','1900000000000000085','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_USER -> System > AccountCenter
  ('1900000000000000103','1900000000000000032','1900000000000000094','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000104','1900000000000000032','1900000000000000095','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000105','1900000000000000032','1900000000000000096','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000109','1900000000000000032','1900000000000000106','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000122','1900000000000000032','1900000000000000110','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000123','1900000000000000032','1900000000000000111','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000124','1900000000000000032','1900000000000000112','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000148','1900000000000000032','1900000000000000134','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000125','1900000000000000032','1900000000000000113','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000142','1900000000000000032','1900000000000000130','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000143','1900000000000000032','1900000000000000131','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000144','1900000000000000032','1900000000000000132','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000145','1900000000000000032','1900000000000000133','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_USER -> System > FileManage
  ('1900000000000002067','1900000000000000032','1900000000000000038','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002068','1900000000000000032','1900000000000000204','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002069','1900000000000000032','1900000000000000008','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_SUPER -> Settings
  ('1900000000000002020','1900000000000000023','1900000000000002001','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_SUPER -> Settings > AccessControl
  ('1900000000000002021','1900000000000000023','1900000000000002002','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002036','1900000000000000023','1900000000000000206','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002022','1900000000000000023','1900000000000002003','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002023','1900000000000000023','1900000000000002004','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002024','1900000000000000023','1900000000000002005','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002025','1900000000000000023','1900000000000002006','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002026','1900000000000000023','1900000000000002007','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000002027','1900000000000000023','1900000000000002008','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_role_menu_permission` ENABLE KEYS */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-10 14:30:14
