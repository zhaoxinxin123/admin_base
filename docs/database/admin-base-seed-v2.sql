-- Admin Base v2 种子数据
-- 包含一个管理员账号、一个管理员角色、核心权限、角色权限关联和全局配置
-- BCrypt 密码哈希来自原始脚本，仅用于本地/开发种子数据
--
-- 权限命名约定（perm 字段）：
--   level 1 目录：  sys:<模块名>       （如 sys:manage）
--   level 2 菜单：  sys:<模块名>List   （如 sys:adminList，兼容旧命名）
--   level 3 按钮：  sys:<模块>:<动作>  （如 sys:admin:list / sys:admin:add / sys:admin:edit / sys:admin:delete）
-- 注意：level 2 的 perm 保留旧命名（如 sys:adminList）以兼容现有 @PreAuthorize 注解，
-- level 3 按钮 perm 采用新命名规范（冒号分隔），后续 Task 会将 @PreAuthorize 统一迁移至按钮级。

-- 管理员（密码：123456，BCrypt 哈希）
INSERT INTO tb_sys_admin (admin_id, nickname, user_name, password, state, create_time, update_time)
VALUES (1, '管理员', 'admin', '$2a$10$KCq.c/d5K6ZuWDlKxOtokON5Vr3zssxrW1IMDaQpnF9oge1f9qwUi', 0, NOW(), NOW());

-- 角色
INSERT INTO tb_sys_role (role_id, role_name, note, create_time, update_time)
VALUES (1, 'ROLE_ADMIN', '管理员', NOW(), NOW());

-- 核心权限（level 1 目录 + level 2 菜单 + level 3 按钮）
INSERT INTO tb_sys_permissions (permission_id, parent_id, level, path, perm, require_auth, state, url, title, create_time, update_time)
VALUES
  -- level 1: 系统管理目录
  (1, 0, 1, '/system', 'sys:manage', 1, 1, 'system', '系统管理', NOW(), NOW()),

  -- level 2: 管理员列表
  (2, 1, 2, '/system/admin', 'sys:adminList', 1, 1, 'admin', '管理员列表', NOW(), NOW()),
  -- level 3: 管理员按钮
  (7, 2, 3, '', 'sys:admin:list', 1, 1, '', '管理员查询', NOW(), NOW()),
  (8, 2, 3, '', 'sys:admin:add', 1, 1, '', '管理员新增', NOW(), NOW()),
  (9, 2, 3, '', 'sys:admin:edit', 1, 1, '', '管理员编辑', NOW(), NOW()),
  (10, 2, 3, '', 'sys:admin:delete', 1, 1, '', '管理员删除', NOW(), NOW()),
  (11, 2, 3, '', 'sys:admin:resetPwd', 1, 1, '', '重置密码', NOW(), NOW()),

  -- level 2: 角色列表
  (3, 1, 2, '/system/role', 'sys:roleList', 1, 1, 'role', '角色列表', NOW(), NOW()),
  -- level 3: 角色按钮
  (12, 3, 3, '', 'sys:role:list', 1, 1, '', '角色查询', NOW(), NOW()),
  (13, 3, 3, '', 'sys:role:add', 1, 1, '', '角色新增', NOW(), NOW()),
  (14, 3, 3, '', 'sys:role:edit', 1, 1, '', '角色编辑', NOW(), NOW()),
  (15, 3, 3, '', 'sys:role:delete', 1, 1, '', '角色删除', NOW(), NOW()),
  (16, 3, 3, '', 'sys:role:assignPerm', 1, 1, '', '分配权限', NOW(), NOW()),

  -- level 2: 权限列表
  (4, 1, 2, '/system/permission', 'sys:permissionList', 1, 1, 'permission', '权限列表', NOW(), NOW()),
  -- level 3: 权限按钮
  (17, 4, 3, '', 'sys:permission:list', 1, 1, '', '权限查询', NOW(), NOW()),
  (18, 4, 3, '', 'sys:permission:add', 1, 1, '', '权限新增', NOW(), NOW()),
  (19, 4, 3, '', 'sys:permission:edit', 1, 1, '', '权限编辑', NOW(), NOW()),
  (20, 4, 3, '', 'sys:permission:delete', 1, 1, '', '权限删除', NOW(), NOW()),

  -- level 2: 全局配置
  (5, 1, 2, '/system/config', 'sys:configList', 1, 1, 'config', '全局配置', NOW(), NOW()),
  -- level 3: 配置按钮
  (21, 5, 3, '', 'sys:config:list', 1, 1, '', '配置查询', NOW(), NOW()),
  (22, 5, 3, '', 'sys:config:add', 1, 1, '', '配置新增', NOW(), NOW()),
  (23, 5, 3, '', 'sys:config:edit', 1, 1, '', '配置编辑', NOW(), NOW()),
  (24, 5, 3, '', 'sys:config:delete', 1, 1, '', '配置删除', NOW(), NOW()),

  -- level 2: 操作日志
  (6, 1, 2, '/system/log', 'sys:logList', 1, 1, 'log', '操作日志', NOW(), NOW()),
  -- level 3: 日志按钮
  (25, 6, 3, '', 'sys:log:list', 1, 1, '', '日志查询', NOW(), NOW()),
  (26, 6, 3, '', 'sys:log:delete', 1, 1, '', '日志删除', NOW(), NOW());

-- 角色权限关联（ROLE_ADMIN 拥有全部权限）
INSERT INTO tb_sys_role_permission (role_id, permission_id, create_time)
VALUES
  (1, 1, NOW()), (1, 2, NOW()), (1, 3, NOW()), (1, 4, NOW()), (1, 5, NOW()), (1, 6, NOW()),
  (1, 7, NOW()), (1, 8, NOW()), (1, 9, NOW()), (1, 10, NOW()), (1, 11, NOW()),
  (1, 12, NOW()), (1, 13, NOW()), (1, 14, NOW()), (1, 15, NOW()), (1, 16, NOW()),
  (1, 17, NOW()), (1, 18, NOW()), (1, 19, NOW()), (1, 20, NOW()),
  (1, 21, NOW()), (1, 22, NOW()), (1, 23, NOW()), (1, 24, NOW()),
  (1, 25, NOW()), (1, 26, NOW());

-- 管理员角色关联
INSERT INTO tb_sys_admin_role (admin_id, role_id, create_time)
VALUES (1, 1, NOW());

-- 全局配置
INSERT INTO tb_sys_global_config (config_id, config_key, config_value, note, create_time, update_time)
VALUES
  (1, 'global_download_path', '/tmp/admin-base/download', '文件下载路径', NOW(), NOW()),
  (2, 'global_upload_path', '/tmp/admin-base/upload', '文件上传路径', NOW(), NOW()),
  (3, 'sys_version', '2.0.0', '系统版本号', NOW(), NOW());
