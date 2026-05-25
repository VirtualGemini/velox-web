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
  ('sys_user',0),
  ('sys_role',0),
  ('sys_menu',0),
  ('sys_profile',0),
  ('sys_user_role',0),
  ('sys_user_session',0),
  ('sys_user_security',0),
  ('sys_role_menu_permission',0),
  ('sys_file_config',0),
  ('sys_file',0),
  ('sys_file_content',0);
/*!40000 ALTER TABLE `sys_id_sequence` ENABLE KEYS */;

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
  -- System
  ('1900000000000000080',NULL,'menu','System','menus.system.title','/system','/index/index',NULL,'ri:user-3-line',NULL,1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > User
  ('1900000000000000016','1900000000000000080','menu','User','menus.system.user','user','/system/user',NULL,NULL,NULL,1,1,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000007','1900000000000000016','button','UserQuery','Query',NULL,NULL,NULL,NULL,'system:user:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000020','1900000000000000016','button','UserCreate','Create',NULL,NULL,NULL,NULL,'system:user:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000060','1900000000000000016','button','UserUpdate','Edit',NULL,NULL,NULL,NULL,'system:user:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000030','1900000000000000016','button','UserDelete','Delete',NULL,NULL,NULL,NULL,'system:user:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > Role
  ('1900000000000000046','1900000000000000080','menu','Role','menus.system.role','role','/system/role',NULL,NULL,NULL,1,2,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000085','1900000000000000046','button','RoleQuery','Query',NULL,NULL,NULL,NULL,'system:role:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000004','1900000000000000046','button','RoleCreate','Create',NULL,NULL,NULL,NULL,'system:role:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000066','1900000000000000046','button','RoleUpdate','Edit',NULL,NULL,NULL,NULL,'system:role:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000092','1900000000000000046','button','RoleDelete','Delete',NULL,NULL,NULL,NULL,'system:role:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000053','1900000000000000046','button','RolePermission','Menu Permission',NULL,NULL,NULL,NULL,'system:role:permission',1,5,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > UserCenter
  ('1900000000000000035','1900000000000000080','menu','UserCenter','menus.system.userCenter','user-center','/system/user-center',NULL,NULL,'system:user-center',1,3,1,1,1,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000106','1900000000000000035','button','UserCenterProfileQuery','Profile Query',NULL,NULL,NULL,NULL,'system:user-center:profile-query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000094','1900000000000000035','button','UserCenterProfileUpdate','Update Profile',NULL,NULL,NULL,NULL,'system:user-center:profile-update',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000095','1900000000000000035','button','UserCenterPasswordUpdate','Update Password',NULL,NULL,NULL,NULL,'system:user-center:password-update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000096','1900000000000000035','button','UserCenterAvatarUpdate','Update Avatar',NULL,NULL,NULL,NULL,'system:user-center:avatar-update',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000110','1900000000000000035','button','UserCenterSecurityQuery','Security Query',NULL,NULL,NULL,NULL,'system:user-center:security-query',1,5,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000111','1900000000000000035','button','UserCenterSecurityUpdate','Security Update',NULL,NULL,NULL,NULL,'system:user-center:security-update',1,6,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000112','1900000000000000035','button','UserCenterEmailRebind','Rebind Email',NULL,NULL,NULL,NULL,'system:user-center:email-rebind',1,7,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000113','1900000000000000035','button','UserCenterMfaUpdate','MFA Management',NULL,NULL,NULL,NULL,'system:user-center:mfa-update',1,8,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > Menus
  ('1900000000000000063','1900000000000000080','menu','Menus','menus.system.menu','menu','/system/menu',NULL,NULL,NULL,1,4,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000086','1900000000000000063','button','MenusQuery','Query',NULL,NULL,NULL,NULL,'system:menu:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000084','1900000000000000063','button','MenusCreate','Create',NULL,NULL,NULL,NULL,'system:menu:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000042','1900000000000000063','button','MenusUpdate','Edit',NULL,NULL,NULL,NULL,'system:menu:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000071','1900000000000000063','button','MenusDelete','Delete',NULL,NULL,NULL,NULL,'system:menu:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- System > FileManage
  ('1900000000000000038','1900000000000000080','menu','FileManage','menus.system.fileManage','file-manage','/system/file-manage',NULL,'',NULL,1,5,1,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
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
  ('1900000000000000062','1900000000000000003','button','FileConfigQuery','Query',NULL,NULL,NULL,NULL,'system:file-config:query',1,1,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000039','1900000000000000003','button','FileConfigCreate','Create',NULL,NULL,NULL,NULL,'system:file-config:create',1,2,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000076','1900000000000000003','button','FileConfigUpdate','Edit',NULL,NULL,NULL,NULL,'system:file-config:update',1,3,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000044','1900000000000000003','button','FileConfigDelete','Delete',NULL,NULL,NULL,NULL,'system:file-config:delete',1,4,0,0,0,NULL,0,0,NULL,0,NULL,0,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_menu` ENABLE KEYS */;

--
-- Table structure for table `sys_profile`
--

DROP TABLE IF EXISTS `sys_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_profile` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Nickname',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Avatar',
  `gender` tinyint DEFAULT '0' COMMENT 'Gender (0-Unknown 1-Male 2-Female 3-Other)',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Full Name',
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
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_sys_profile_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User Profile Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_profile`
--

/*!40000 ALTER TABLE `sys_profile` DISABLE KEYS */;
INSERT INTO `sys_profile` VALUES
  ('1900000000000000075','1900000000000000027','Virtual Gemini','https://api.dicebear.com/7.x/avataaars/svg?seed=Super',2,'Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','[\"Cute\", \"Very Cute\", \"Extremely Cute\"]','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000057','1900000000000000087','Virtual Gemini','https://api.dicebear.com/7.x/avataaars/svg?seed=Admin',1,'Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','[\"Cute\", \"Very Cute\", \"Extremely Cute\"]','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000014','1900000000000000048','Virtual Gemini','https://api.dicebear.com/7.x/avataaars/svg?seed=User',1,'Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','Prefer not to disclose','[\"Cute\", \"Very Cute\", \"Extremely Cute\"]','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_profile` ENABLE KEYS */;

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
  -- R_SUPER -> System
  ('1900000000000000068','1900000000000000023','1900000000000000080','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_SUPER -> System > User
  ('1900000000000000043','1900000000000000023','1900000000000000016','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000002','1900000000000000023','1900000000000000007','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000082','1900000000000000023','1900000000000000020','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000009','1900000000000000023','1900000000000000060','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000036','1900000000000000023','1900000000000000030','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_SUPER -> System > Role
  ('1900000000000000088','1900000000000000023','1900000000000000046','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000056','1900000000000000023','1900000000000000085','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000018','1900000000000000023','1900000000000000004','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000019','1900000000000000023','1900000000000000066','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000034','1900000000000000023','1900000000000000092','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000049','1900000000000000023','1900000000000000053','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_SUPER -> System > UserCenter
  ('1900000000000000005','1900000000000000023','1900000000000000035','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000097','1900000000000000023','1900000000000000094','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000098','1900000000000000023','1900000000000000095','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000099','1900000000000000023','1900000000000000096','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000107','1900000000000000023','1900000000000000106','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000114','1900000000000000023','1900000000000000110','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000115','1900000000000000023','1900000000000000111','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000116','1900000000000000023','1900000000000000112','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000117','1900000000000000023','1900000000000000113','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_SUPER -> System > Menus
  ('1900000000000000037','1900000000000000023','1900000000000000063','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000058','1900000000000000023','1900000000000000086','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000074','1900000000000000023','1900000000000000084','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000045','1900000000000000023','1900000000000000042','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000047','1900000000000000023','1900000000000000071','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_SUPER -> System > FileManage
  ('1900000000000000065','1900000000000000023','1900000000000000038','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
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
  ('1900000000000000055','1900000000000000023','1900000000000000062','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000017','1900000000000000023','1900000000000000039','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000067','1900000000000000023','1900000000000000076','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000081','1900000000000000023','1900000000000000044','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_ADMIN -> Dashboard
  ('1900000000000000028','1900000000000000073','1900000000000000077','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000033','1900000000000000073','1900000000000000089','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_ADMIN -> System
  ('1900000000000000022','1900000000000000073','1900000000000000080','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_ADMIN -> System > User
  ('1900000000000000015','1900000000000000073','1900000000000000016','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000091','1900000000000000073','1900000000000000007','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000078','1900000000000000073','1900000000000000020','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000079','1900000000000000073','1900000000000000060','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000051','1900000000000000073','1900000000000000030','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_ADMIN -> System > UserCenter
  ('1900000000000000070','1900000000000000073','1900000000000000035','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000100','1900000000000000073','1900000000000000094','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000101','1900000000000000073','1900000000000000095','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000102','1900000000000000073','1900000000000000096','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000108','1900000000000000073','1900000000000000106','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000118','1900000000000000073','1900000000000000110','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000119','1900000000000000073','1900000000000000111','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000120','1900000000000000073','1900000000000000112','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000121','1900000000000000073','1900000000000000113','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  -- R_USER -> System > UserCenter
  ('1900000000000000010','1900000000000000032','1900000000000000035','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000103','1900000000000000032','1900000000000000094','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000104','1900000000000000032','1900000000000000095','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000105','1900000000000000032','1900000000000000096','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000109','1900000000000000032','1900000000000000106','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000122','1900000000000000032','1900000000000000110','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000123','1900000000000000032','1900000000000000111','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000124','1900000000000000032','1900000000000000112','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000125','1900000000000000032','1900000000000000113','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_role_menu_permission` ENABLE KEYS */;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Username',
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Password',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Email',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Phone Number',
  `status` tinyint DEFAULT '1' COMMENT 'Status (1-Online 2-Offline 3-Abnormal 4-Deactivated)',
  `login_fail_count` int DEFAULT '0' COMMENT 'Login Failure Count',
  `login_fail_time` datetime DEFAULT NULL COMMENT 'Login Failure Time',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  `active_username` varchar(50) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((case when (`deleted` = 0) then `username` else NULL end)) STORED COMMENT 'Active Username',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`active_username`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System User Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` (`id`, `username`, `password`, `email`, `phone`, `status`, `login_fail_count`, `login_fail_time`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  ('1900000000000000027','Super','{bcrypt}$2a$12$w9oNXS.wlWOfoHg6vMVKKuRHIASL5uTI2BtybrocyvQCMmeSIhshu','super@architecture.pro','15800158001',1,0,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000087','Admin','{bcrypt}$2a$12$w9oNXS.wlWOfoHg6vMVKKuRHIASL5uTI2BtybrocyvQCMmeSIhshu','admin@architecture.pro','15800158002',1,0,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000048','User','{bcrypt}$2a$12$w9oNXS.wlWOfoHg6vMVKKuRHIASL5uTI2BtybrocyvQCMmeSIhshu','user@architecture.pro','15800158003',1,0,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;

--
-- Table structure for table `sys_user_role`
--

DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `role_id` bigint NOT NULL COMMENT 'Role ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated Time',
  `create_by` bigint DEFAULT NULL COMMENT 'Created By',
  `update_by` bigint DEFAULT NULL COMMENT 'Updated By',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical Delete (0-Not Deleted 1-Deleted)',
  `active_user_role` tinyint GENERATED ALWAYS AS ((case when (`deleted` = 0) then 1 else NULL end)) STORED COMMENT 'Active Relation Marker',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`,`active_user_role`),
  KEY `idx_user_role_user_id` (`user_id`),
  KEY `idx_user_role_role_id` (`role_id`),
  CONSTRAINT `fk_sys_user_role_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`),
  CONSTRAINT `fk_sys_user_role_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User Role Relation Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_user_session`
--

DROP TABLE IF EXISTS `sys_user_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_session` (
  `id` bigint NOT NULL COMMENT 'Primary Key ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
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
  UNIQUE KEY `uk_user_session_token_hash` (`token_hash`),
  KEY `idx_user_session_user_id` (`user_id`),
  KEY `idx_user_session_presence_expire_time` (`presence_expire_time`),
  CONSTRAINT `fk_sys_user_session_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User Login Session Table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_role`
--

/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  ('1900000000000000021','1900000000000000027','1900000000000000023','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000012','1900000000000000087','1900000000000000073','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000024','1900000000000000048','1900000000000000032','2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;

--
-- Table structure for table `sys_user_security`
--

DROP TABLE IF EXISTS `sys_user_security`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_security` (
  `id` bigint NOT NULL COMMENT 'Primary key',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `login_methods` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT 'password' COMMENT 'Allowed login methods (comma-separated: password,email_code)',
  `mfa_email_enabled` tinyint DEFAULT '0' COMMENT 'Email MFA enabled',
  `mfa_totp_enabled` tinyint DEFAULT '0' COMMENT 'TOTP MFA enabled (placeholder)',
  `mfa_totp_secret` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'TOTP secret (placeholder)',
  `email_verified_at` datetime DEFAULT NULL COMMENT 'Email last verification time',
  `last_password_change_at` datetime DEFAULT NULL COMMENT 'Last password change time',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `create_by` bigint DEFAULT NULL COMMENT 'Creator',
  `update_by` bigint DEFAULT NULL COMMENT 'Updater',
  `deleted` tinyint DEFAULT '0' COMMENT 'Logical delete (0-active 1-deleted)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_security_user_id` (`user_id`),
  CONSTRAINT `fk_sys_user_security_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User account security configuration';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_security`
--

/*!40000 ALTER TABLE `sys_user_security` DISABLE KEYS */;
INSERT INTO `sys_user_security` (`id`, `user_id`, `login_methods`, `mfa_email_enabled`, `mfa_totp_enabled`, `mfa_totp_secret`, `email_verified_at`, `last_password_change_at`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
  ('1900000000000000126','1900000000000000027','password',0,0,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000127','1900000000000000087','password',0,0,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0),
  ('1900000000000000128','1900000000000000048','password',0,0,NULL,NULL,NULL,'2026-05-10 12:00:00','2026-05-10 12:00:00',1900000000000000027,1900000000000000027,0);
/*!40000 ALTER TABLE `sys_user_security` ENABLE KEYS */;

--
-- Dumping routines for database 'velox'
--
--
-- WARNING: can't read the INFORMATION_SCHEMA.libraries table. It's most probably an old server 8.0.45.
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-10 14:30:14
