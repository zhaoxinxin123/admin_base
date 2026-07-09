-- Historical bootstrap script. New schema lives in docs/database/admin-base-schema-v2.sql.

/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 50731
 Source Host           : localhost:3306
 Source Schema         : mark

 Target Server Type    : MySQL
 Target Server Version : 50731
 File Encoding         : 65001

 Date: 18/05/2023 11:46:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_sys_admin
-- ----------------------------
DROP TABLE IF EXISTS `tb_sys_admin`;
CREATE TABLE `tb_sys_admin`  (
  `admin_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '昵称',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `state` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '账号',
  `password_show` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '明文密码，预留字段，按需使用',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`admin_id`) USING BTREE,
  UNIQUE INDEX `uk_user_name`(`user_name`) USING BTREE COMMENT '用户名唯一'
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_sys_admin
-- ----------------------------
INSERT INTO `tb_sys_admin` VALUES (1, '管理员', '$2a$10$KCq.c/d5K6ZuWDlKxOtokON5Vr3zssxrW1IMDaQpnF9oge1f9qwUi', '0', 'kyle', NULL, '2021-09-13 11:51:40', '2021-09-26 16:55:55');
INSERT INTO `tb_sys_admin` VALUES (11, 'sjg', '$2a$10$TgLjzH38KDk5XHiBZs.cVuFOXu4C3CVSQhrJqA8ESMCYt4yPaBV7S', '0', 'sjg', NULL, '2023-03-09 09:46:36', '2023-03-09 09:46:36');

-- ----------------------------
-- Table structure for tb_sys_admin_role
-- ----------------------------
DROP TABLE IF EXISTS `tb_sys_admin_role`;
CREATE TABLE `tb_sys_admin_role`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `admin_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  `create_time` datetime(0) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admin_role`(`admin_id`, `role_id`) USING BTREE COMMENT '一个管理只会对应同一个角色ID'
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_sys_admin_role
-- ----------------------------
INSERT INTO `tb_sys_admin_role` VALUES (1, 1, 1, '2021-09-13 15:38:39');
INSERT INTO `tb_sys_admin_role` VALUES (21, 11, 1, '2023-03-09 09:46:36');

-- ----------------------------
-- Table structure for tb_sys_global_config
-- ----------------------------
DROP TABLE IF EXISTS `tb_sys_global_config`;
CREATE TABLE `tb_sys_global_config`  (
  `config_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `config_value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置值',
  `config_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'key',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `note` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `update_time` datetime(0) NOT NULL,
  PRIMARY KEY (`config_id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key`) USING BTREE COMMENT '配置key是唯一的'
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'tb_global_config' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_sys_global_config
-- ----------------------------
INSERT INTO `tb_sys_global_config` VALUES (1, '/test', 'global_download_path', '2021-09-16 14:48:35', '存放下载文件', '2021-09-26 17:22:03');
INSERT INTO `tb_sys_global_config` VALUES (2, '/test', 'global_upload_path', '2021-09-16 14:49:47', '文件上传路径', '2021-09-26 09:58:17');
INSERT INTO `tb_sys_global_config` VALUES (3, '1.01', 'sys_version', '2021-09-16 14:50:24', '系统版本号', '2021-09-26 10:10:46');

-- ----------------------------
-- Table structure for tb_sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `tb_sys_operation_log`;
CREATE TABLE `tb_sys_operation_log`  (
  `operation_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作模块',
  `business_type` int(11) NULL DEFAULT NULL COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方法',
  `request_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方式',
  `operation_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作人员',
  `operation_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求url',
  `operation_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作地址',
  `operation_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求参数',
  `json_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '返回参数',
  `status` int(11) NULL DEFAULT NULL COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '错误消息',
  `operation_time` datetime(0) NULL DEFAULT '1000-01-01 00:00:00' COMMENT '操作时间',
  PRIMARY KEY (`operation_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 489 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'tb_sys_operation_log' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_sys_operation_log
-- ----------------------------
INSERT INTO `tb_sys_operation_log` VALUES (461, 'sys', 3, 'com.my.admin.controller.system.PermissionsController.delete()', 'POST', 'kyle', '/admin-api/permissions/delete', '172.16.13.40', '{\"permissionId\":215}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:27:16');
INSERT INTO `tb_sys_operation_log` VALUES (462, 'sys', 1, 'com.my.admin.controller.system.PermissionsController.add()', 'POST', 'kyle', '/admin-api/permissions/add', '172.16.13.40', '{\"parentId\":0,\"perm\":\"sss\",\"icon\":\"bug\",\"name\":\"null\",\"state\":1,\"title\":\"ssss\",\"path\":\"sss\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:29:40');
INSERT INTO `tb_sys_operation_log` VALUES (463, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":216,\"parentId\":0,\"perm\":\"sss\",\"icon\":\"bug\",\"name\":\"null\",\"state\":1,\"title\":\"更新个名字\",\"path\":\"sss\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:30:30');
INSERT INTO `tb_sys_operation_log` VALUES (464, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":216,\"parentId\":0,\"perm\":\"sss\",\"icon\":\"bug\",\"name\":\"null\",\"state\":1,\"title\":\"更新个名字12\",\"path\":\"sss\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:34:20');
INSERT INTO `tb_sys_operation_log` VALUES (465, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":1,\"parentId\":0,\"perm\":\"sys:manager\",\"icon\":\"documentation\",\"name\":\"null\",\"state\":1,\"title\":\"系统管理\",\"path\":\"system\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:34:39');
INSERT INTO `tb_sys_operation_log` VALUES (466, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":11,\"parentId\":216,\"perm\":\"sys:operationList\",\"icon\":\"null\",\"name\":\"null\",\"state\":1,\"title\":\"日志列表\",\"path\":\"operationLog\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:35:45');
INSERT INTO `tb_sys_operation_log` VALUES (467, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":12,\"parentId\":216,\"perm\":\"sys:settingList\",\"icon\":\"null\",\"name\":\"null\",\"state\":1,\"title\":\"全局配置\",\"path\":\"config\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:36:34');
INSERT INTO `tb_sys_operation_log` VALUES (468, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":11,\"parentId\":1,\"perm\":\"sys:operationList\",\"icon\":\"null\",\"name\":\"null\",\"state\":1,\"title\":\"日志列表\",\"path\":\"operationLog\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:37:22');
INSERT INTO `tb_sys_operation_log` VALUES (469, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":12,\"parentId\":1,\"perm\":\"sys:settingList\",\"icon\":\"null\",\"name\":\"null\",\"state\":1,\"title\":\"全局配置\",\"path\":\"config\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:37:39');
INSERT INTO `tb_sys_operation_log` VALUES (470, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":12,\"parentId\":1,\"perm\":\"sys:settingList\",\"icon\":\"dashboard\",\"name\":\"null\",\"state\":1,\"title\":\"全局配置\",\"path\":\"config\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:39:14');
INSERT INTO `tb_sys_operation_log` VALUES (471, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":12,\"parentId\":1,\"perm\":\"sys:settingList\",\"icon\":\"404\",\"name\":\"null\",\"state\":1,\"title\":\"全局配置\",\"path\":\"config\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:43:34');
INSERT INTO `tb_sys_operation_log` VALUES (472, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":12,\"parentId\":1,\"perm\":\"sys:settingList\",\"icon\":\"bug\",\"name\":\"null\",\"state\":1,\"title\":\"全局配置\",\"path\":\"config\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:44:24');
INSERT INTO `tb_sys_operation_log` VALUES (473, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":12,\"parentId\":216,\"perm\":\"sys:settingList\",\"icon\":\"bug\",\"name\":\"null\",\"state\":1,\"title\":\"全局配置\",\"path\":\"config\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:45:43');
INSERT INTO `tb_sys_operation_log` VALUES (474, 'sys', 1, 'com.my.admin.controller.system.AdminController.addAdmin()', 'POST', 'kyle', '/admin-api/admin/add', '172.16.13.40', '{\"account\":\"sjg\",\"password\":\"pzkj@10086\",\"nickName\":\"sjg\",\"roleIds\":[1]}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:46:36');
INSERT INTO `tb_sys_operation_log` VALUES (475, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":2,\"parentId\":1,\"perm\":\"sys:adminList\",\"icon\":\"clipboard\",\"name\":\"null\",\"state\":1,\"title\":\"管理员列表\",\"path\":\"system/admin\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:53:20');
INSERT INTO `tb_sys_operation_log` VALUES (476, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":3,\"parentId\":1,\"perm\":\"sys:roleList\",\"icon\":\"user\",\"name\":\"null\",\"state\":1,\"title\":\"角色列表\",\"path\":\"system/role\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:57:00');
INSERT INTO `tb_sys_operation_log` VALUES (477, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":7,\"parentId\":1,\"perm\":\"sys:permission\",\"icon\":\"null\",\"name\":\"null\",\"state\":1,\"title\":\"菜单列表\",\"path\":\"system/menu\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:57:09');
INSERT INTO `tb_sys_operation_log` VALUES (478, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":12,\"parentId\":216,\"perm\":\"sys:settingList\",\"icon\":\"bug\",\"name\":\"null\",\"state\":1,\"title\":\"全局配置\",\"path\":\"system/config\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:57:50');
INSERT INTO `tb_sys_operation_log` VALUES (481, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":11,\"parentId\":216,\"perm\":\"sys:operationList\",\"icon\":\"documentation\",\"name\":\"null\",\"state\":1,\"title\":\"日志列表\",\"path\":\"system/operationLog\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:58:54');
INSERT INTO `tb_sys_operation_log` VALUES (482, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":12,\"parentId\":1,\"perm\":\"sys:settingList\",\"icon\":\"bug\",\"name\":\"null\",\"state\":1,\"title\":\"全局配置\",\"path\":\"system/config\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:59:22');
INSERT INTO `tb_sys_operation_log` VALUES (483, 'sys', 3, 'com.my.admin.controller.system.OperationLogController.deleteByIds()', 'POST', 'kyle', '/admin-api/sys_operation_log/deleteBatch', '172.16.13.40', '{\"logIds\":[480]}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:59:31');
INSERT INTO `tb_sys_operation_log` VALUES (484, 'sys', 3, 'com.my.admin.controller.system.OperationLogController.deleteByIds()', 'POST', 'kyle', '/admin-api/sys_operation_log/deleteBatch', '172.16.13.40', '{\"logIds\":[479]}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:59:35');
INSERT INTO `tb_sys_operation_log` VALUES (485, 'sys', 2, 'com.my.admin.controller.system.PermissionsController.update()', 'POST', 'kyle', '/admin-api/permissions/update', '172.16.13.40', '{\"permissionId\":11,\"parentId\":1,\"perm\":\"sys:operationList\",\"icon\":\"documentation\",\"name\":\"null\",\"state\":1,\"title\":\"日志列表\",\"path\":\"system/operationLog\"}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-03-09 09:59:36');
INSERT INTO `tb_sys_operation_log` VALUES (486, 'sys', 3, 'com.my.admin.controller.system.OperationLogController.deleteByIds()', 'POST', 'kyle', '/admin-api/sys_operation_log/deleteBatch', '172.16.13.96', '{\"logIds\":[459]}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-04-13 15:05:31');
INSERT INTO `tb_sys_operation_log` VALUES (487, 'sys', 3, 'com.my.admin.controller.system.OperationLogController.deleteByIds()', 'POST', 'kyle', '/admin-api/sys_operation_log/deleteBatch', '172.16.13.96', '{\"logIds\":[460]}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-04-13 15:05:35');
INSERT INTO `tb_sys_operation_log` VALUES (488, 'sys', 3, 'com.my.admin.controller.system.PermissionsController.delete()', 'POST', 'kyle', '/admin-api/permissions/delete', '172.16.13.96', '{\"permissionId\":216}', 'JsonResponse(code=200, msg=成功, data=null)', 200, '', '2023-04-14 09:59:04');

-- ----------------------------
-- Table structure for tb_sys_permissions
-- ----------------------------
DROP TABLE IF EXISTS `tb_sys_permissions`;
CREATE TABLE `tb_sys_permissions`  (
  `permission_id` int(20) NOT NULL AUTO_INCREMENT,
  `level` int(11) NOT NULL,
  `parent_id` bigint(20) NOT NULL,
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `perm` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `require_auth` int(11) NOT NULL,
  `state` int(11) NOT NULL,
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NOT NULL,
  `title` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `update_time` datetime(0) NOT NULL,
  PRIMARY KEY (`permission_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_sys_permissions
-- ----------------------------
INSERT INTO `tb_sys_permissions` VALUES (1, 0, 0, 'system', 'sys:manager', 1, 1, 'documentation', '2021-09-17 15:17:59', '系统管理', '2023-03-09 09:34:39');
INSERT INTO `tb_sys_permissions` VALUES (2, 1, 1, 'system/admin', 'sys:adminList', 1, 1, 'clipboard', '2021-09-17 15:18:02', '管理员列表', '2023-03-09 09:53:20');
INSERT INTO `tb_sys_permissions` VALUES (3, 1, 1, 'system/role', 'sys:roleList', 1, 1, 'user', '2021-09-17 15:18:07', '角色列表', '2023-03-09 09:57:00');
INSERT INTO `tb_sys_permissions` VALUES (5, 2, 3, 'operation', 'sys:roleList:operation', 1, 1, NULL, '2021-09-17 15:18:18', '角色列表-新增|修改', '2021-09-24 11:39:44');
INSERT INTO `tb_sys_permissions` VALUES (6, 2, 3, 'delete', 'sys:roleList:delete', 1, 1, NULL, '2021-09-17 15:18:21', '角色列表-删除', '2021-09-24 11:39:44');
INSERT INTO `tb_sys_permissions` VALUES (7, 1, 1, 'system/menu', 'sys:permission', 1, 1, 'build', '2021-09-18 15:19:33', '菜单列表', '2023-03-09 09:58:04');
INSERT INTO `tb_sys_permissions` VALUES (8, 2, 7, 'add', 'sys:permission:add', 1, 1, NULL, '2021-09-17 15:19:42', '菜单列表-新增', '2021-09-24 11:39:44');
INSERT INTO `tb_sys_permissions` VALUES (9, 2, 7, 'delete', 'sys:permission:delete', 1, 1, NULL, '2021-09-17 15:17:34', '菜单列表-删除', '2021-09-24 11:39:44');
INSERT INTO `tb_sys_permissions` VALUES (11, 1, 1, 'system/operationLog', 'sys:operationList', 1, 1, 'documentation', '2021-09-23 11:07:27', '日志列表', '2023-03-09 09:59:36');
INSERT INTO `tb_sys_permissions` VALUES (12, 1, 1, 'system/config', 'sys:settingList', 1, 1, 'bug', '2021-09-23 11:10:59', '全局配置', '2023-03-09 09:59:22');
INSERT INTO `tb_sys_permissions` VALUES (13, 2, 12, 'add', 'sys:setting:add', 1, 1, NULL, '2021-09-23 11:12:01', '新增全局配置', '2021-09-24 11:39:44');
INSERT INTO `tb_sys_permissions` VALUES (14, 2, 12, 'update', 'sys:setting:update', 1, 1, NULL, '2021-09-23 11:12:57', '修改全局配置', '2021-09-24 11:39:44');
INSERT INTO `tb_sys_permissions` VALUES (15, 2, 12, 'delete', 'sys:setting:delete', 1, 1, NULL, '2021-09-23 11:13:36', '删除全局配置', '2021-09-24 11:39:44');
INSERT INTO `tb_sys_permissions` VALUES (16, 2, 11, 'delete', 'sys:operationList:delete', 1, 1, NULL, '2021-09-23 11:14:54', '删除操作日志', '2021-09-24 11:39:44');
INSERT INTO `tb_sys_permissions` VALUES (17, 2, 7, 'update', 'sys:permission:update', 1, 1, NULL, '2021-09-17 15:34:58', '菜单列表-修改', '2021-09-24 11:39:44');
INSERT INTO `tb_sys_permissions` VALUES (18, 2, 2, 'updateRole', 'sys:adminList:updateRole', 1, 1, NULL, '2021-09-24 14:18:34', '管理员列表-分配角色', '2021-09-24 11:39:44');
INSERT INTO `tb_sys_permissions` VALUES (19, 2, 11, 'detail', 'sys:operationList:detail', 1, 1, NULL, '2021-09-23 11:14:54', '操作日志详情', '2021-09-24 11:39:44');
INSERT INTO `tb_sys_permissions` VALUES (20, 2, 2, 'delete', 'sys:adminList:delete', 1, 1, 'button', '2021-09-26 11:20:03', '管理员列表-删除', '2021-09-26 11:28:40');
INSERT INTO `tb_sys_permissions` VALUES (21, 2, 2, 'resetPassword', 'sys:adminList:resetPassword', 1, 1, 'button', '2021-09-26 11:26:37', '管理员列表-重置密码', '2021-09-26 11:42:27');
INSERT INTO `tb_sys_permissions` VALUES (22, 2, 2, 'add', 'sys:adminList:add', 1, 1, 'button', '2021-09-26 11:46:55', '管理员列表-添加', '2021-09-26 11:46:55');

-- ----------------------------
-- Table structure for tb_sys_role
-- ----------------------------
DROP TABLE IF EXISTS `tb_sys_role`;
CREATE TABLE `tb_sys_role`  (
  `role_id` int(11) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` datetime(0) NOT NULL,
  `note` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` datetime(0) NOT NULL,
  PRIMARY KEY (`role_id`) USING BTREE,
  UNIQUE INDEX `uk_role_name`(`role_name`) USING BTREE COMMENT '角色名称是唯一的'
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_sys_role
-- ----------------------------
INSERT INTO `tb_sys_role` VALUES (1, 'ROLE_ADMIN', '2021-09-17 15:17:15', '管理员', '2021-09-26 09:45:12');

-- ----------------------------
-- Table structure for tb_sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `tb_sys_role_permission`;
CREATE TABLE `tb_sys_role_permission`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `permission_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  `create_time` datetime(0) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`permission_id`, `role_id`) USING BTREE COMMENT '角色ID对应的权限ID是唯一的'
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_sys_role_permission
-- ----------------------------
INSERT INTO `tb_sys_role_permission` VALUES (1, 1, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (2, 2, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (3, 3, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (5, 5, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (6, 6, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (7, 7, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (8, 8, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (9, 9, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (11, 11, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (12, 12, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (13, 13, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (14, 14, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (15, 15, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (16, 16, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (17, 17, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (18, 18, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (19, 19, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (20, 20, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (21, 21, 1, '2021-09-23 17:18:29');
INSERT INTO `tb_sys_role_permission` VALUES (22, 22, 1, '2021-09-23 17:18:29');

SET FOREIGN_KEY_CHECKS = 1;
