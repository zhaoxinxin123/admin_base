-- Admin Base Phase 2: migrate existing v1 system tables to the v2 JPA schema.
-- This script only touches the seven retained system tables and does not add
-- relational constraints.

SET NAMES utf8mb4;

-- Preserve old non-JSON response text before converting the column to JSON.
UPDATE tb_sys_operation_log
SET json_result = JSON_QUOTE(json_result)
WHERE json_result IS NOT NULL
  AND JSON_VALID(json_result) = 0;

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
