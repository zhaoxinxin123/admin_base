-- Admin Base v2 数据库 Schema 草案
-- 仅保留 7 张系统核心表，不使用外键（通过主键、唯一索引、普通索引、服务层校验保证数据一致性）
-- 字符集：utf8mb4，排序规则：utf8mb4_0900_ai_ci

SET NAMES utf8mb4;

-- 系统管理员表
CREATE TABLE tb_sys_admin (
  admin_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  nickname VARCHAR(64) NOT NULL COMMENT '昵称',
  user_name VARCHAR(64) NOT NULL COMMENT '登录账号',
  password VARCHAR(255) NOT NULL COMMENT 'BCrypt密码',
  state TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0正常，1禁用',
  create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (admin_id),
  UNIQUE KEY uk_sys_admin_user_name (user_name),
  KEY idx_sys_admin_state (state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统管理员';

-- 系统角色表
CREATE TABLE tb_sys_role (
  role_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
  note VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (role_id),
  UNIQUE KEY uk_sys_role_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色';

-- 菜单和按钮权限表
CREATE TABLE tb_sys_permissions (
  permission_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父权限ID，0表示根节点',
  level TINYINT NOT NULL COMMENT '层级',
  path VARCHAR(255) NOT NULL COMMENT '前端路由或按钮路径',
  perm VARCHAR(128) NOT NULL COMMENT '权限标识',
  require_auth TINYINT NOT NULL DEFAULT 1 COMMENT '是否需要认证：0否，1是',
  state TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用，1启用',
  url VARCHAR(255) DEFAULT NULL COMMENT '图标或扩展地址',
  title VARCHAR(64) NOT NULL COMMENT '标题',
  create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (permission_id),
  UNIQUE KEY uk_sys_permissions_perm (perm),
  KEY idx_sys_permissions_parent_id (parent_id),
  KEY idx_sys_permissions_state (state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单和按钮权限';

-- 管理员角色关系表
CREATE TABLE tb_sys_admin_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  admin_id BIGINT UNSIGNED NOT NULL COMMENT '管理员ID',
  role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_admin_role_admin_role (admin_id, role_id),
  KEY idx_sys_admin_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员角色关系';

-- 角色权限关系表
CREATE TABLE tb_sys_role_permission (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  permission_id BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
  create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_permission_role_permission (role_id, permission_id),
  KEY idx_sys_role_permission_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关系';

-- 全局配置表
CREATE TABLE tb_sys_global_config (
  config_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  config_key VARCHAR(128) NOT NULL COMMENT '配置键',
  config_value VARCHAR(512) NOT NULL COMMENT '配置值',
  note VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (config_id),
  UNIQUE KEY uk_sys_global_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='全局配置';

-- 操作日志表
CREATE TABLE tb_sys_operation_log (
  operation_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  title VARCHAR(64) DEFAULT NULL COMMENT '操作模块',
  business_type TINYINT DEFAULT NULL COMMENT '业务类型',
  method VARCHAR(255) DEFAULT NULL COMMENT 'Java方法',
  request_method VARCHAR(16) DEFAULT NULL COMMENT 'HTTP方法',
  operation_name VARCHAR(64) DEFAULT NULL COMMENT '操作人员',
  operation_url VARCHAR(255) DEFAULT NULL COMMENT '请求地址',
  operation_ip VARCHAR(64) DEFAULT NULL COMMENT '操作IP',
  operation_param JSON DEFAULT NULL COMMENT '请求参数',
  json_result JSON DEFAULT NULL COMMENT '响应结果',
  success TINYINT NOT NULL DEFAULT 1 COMMENT '是否成功：0失败，1成功',
  status_code INT DEFAULT NULL COMMENT '业务或HTTP状态码',
  error_msg TEXT DEFAULT NULL COMMENT '错误消息',
  operation_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作时间',
  PRIMARY KEY (operation_id),
  KEY idx_sys_operation_log_operation_name (operation_name),
  KEY idx_sys_operation_log_business_type (business_type),
  KEY idx_sys_operation_log_operation_time (operation_time),
  KEY idx_sys_operation_log_success (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';
