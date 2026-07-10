-- Admin Base Phase 2：将已有 v1 系统表迁移到 v2 JPA schema。
-- 本脚本只处理 Phase 2 保留的 7 张系统表，不新增关系约束。
--
-- 原因：
-- Phase 2 运行时使用 Spring Data JPA，并开启 hibernate.ddl-auto=validate。
-- 旧 MyBatis schema 创建的测试/预发数据库可能仍然存在 INT 主键、较短的
-- varchar 列、status 旧列名，以及普通文本格式的操作日志响应内容。
-- 如果这些保留表与 v2 schema 不一致，Hibernate 启动校验会失败。
-- 该迁移在保留已有数据的同时，将列名和列类型对齐到 JPA 实体使用的结构。

SET NAMES utf8mb4;

-- 转换 json_result 为 JSON 类型前，先保留并包装旧的非 JSON 响应文本。
UPDATE tb_sys_operation_log
SET json_result = JSON_QUOTE(json_result)
WHERE json_result IS NOT NULL
  AND JSON_VALID(json_result) = 0;

-- 将保留的账号、角色、权限和配置表对齐到 v2 实体的 ID 类型和列约束。
-- BIGINT ID 对应 JPA 实体中的 Long 标识符。
ALTER TABLE tb_sys_admin
  MODIFY admin_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  MODIFY nickname VARCHAR(64) NOT NULL,
  MODIFY user_name VARCHAR(64) NOT NULL,
  MODIFY state TINYINT NOT NULL DEFAULT 0;

ALTER TABLE tb_sys_role
  MODIFY role_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  MODIFY role_name VARCHAR(64) NOT NULL,
  MODIFY note VARCHAR(255) NULL;

ALTER TABLE tb_sys_permissions
  MODIFY permission_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  MODIFY level TINYINT NOT NULL,
  MODIFY parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
  MODIFY perm VARCHAR(128) NOT NULL,
  MODIFY require_auth TINYINT NOT NULL DEFAULT 1,
  MODIFY state TINYINT NOT NULL DEFAULT 1,
  MODIFY title VARCHAR(64) NOT NULL;

ALTER TABLE tb_sys_admin_role
  MODIFY id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  MODIFY admin_id BIGINT UNSIGNED NOT NULL,
  MODIFY role_id BIGINT UNSIGNED NOT NULL;

ALTER TABLE tb_sys_role_permission
  MODIFY id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  MODIFY role_id BIGINT UNSIGNED NOT NULL,
  MODIFY permission_id BIGINT UNSIGNED NOT NULL;

ALTER TABLE tb_sys_global_config
  MODIFY config_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  MODIFY config_key VARCHAR(128) NOT NULL,
  MODIFY config_value VARCHAR(512) NOT NULL,
  MODIFY note VARCHAR(255) NULL;

-- 操作日志从旧 status 列调整为明确的 status_code 与 success 标记。
-- JSON 列在修改类型前必须先保证内容是合法 JSON。
ALTER TABLE tb_sys_operation_log
  CHANGE status status_code INT NULL;

ALTER TABLE tb_sys_operation_log
  ADD COLUMN success TINYINT NOT NULL DEFAULT 1 AFTER json_result;

UPDATE tb_sys_operation_log
SET success = CASE WHEN status_code = 200 THEN 1 ELSE 0 END;

ALTER TABLE tb_sys_operation_log
  MODIFY operation_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  MODIFY title VARCHAR(64) NULL,
  MODIFY business_type TINYINT NULL,
  MODIFY request_method VARCHAR(16) NULL,
  MODIFY operation_name VARCHAR(64) NULL,
  MODIFY operation_ip VARCHAR(64) NULL,
  MODIFY operation_param JSON NULL,
  MODIFY json_result JSON NULL,
  MODIFY error_msg TEXT NULL,
  MODIFY operation_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);
