-- Admin Base v2 种子数据
-- 包含一个管理员账号、一个管理员角色、核心权限、角色权限关联和全局配置
-- BCrypt 密码哈希来自原始脚本，仅用于本地/开发种子数据

-- 管理员（密码：123456，BCrypt 哈希）
INSERT INTO tb_sys_admin (admin_id, nickname, user_name, password, state)
VALUES (1, '管理员', 'admin', '$2a$10$KCq.c/d5K6ZuWDlKxOtokON5Vr3zssxrW1IMDaQpnF9oge1f9qwUi', 0);

-- 角色
INSERT INTO tb_sys_role (role_id, role_name, note)
VALUES (1, 'ROLE_ADMIN', '管理员');

-- 核心权限（菜单与按钮）
INSERT INTO tb_sys_permissions (permission_id, parent_id, level, path, perm, require_auth, state, url, title)
VALUES
  (1, 0, 1, '/system', 'sys:manage', 1, 1, 'system', '系统管理'),
  (2, 1, 2, '/system/admin', 'sys:adminList', 1, 1, 'admin', '管理员列表'),
  (3, 1, 2, '/system/role', 'sys:roleList', 1, 1, 'role', '角色列表'),
  (4, 1, 2, '/system/permission', 'sys:permissionList', 1, 1, 'permission', '权限列表'),
  (5, 1, 2, '/system/config', 'sys:configList', 1, 1, 'config', '全局配置'),
  (6, 1, 2, '/system/log', 'sys:logList', 1, 1, 'log', '操作日志');

-- 角色权限关联（ROLE_ADMIN 拥有全部核心权限）
INSERT INTO tb_sys_role_permission (role_id, permission_id)
VALUES
  (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6);

-- 管理员角色关联
INSERT INTO tb_sys_admin_role (admin_id, role_id)
VALUES (1, 1);

-- 全局配置
INSERT INTO tb_sys_global_config (config_id, config_key, config_value, note)
VALUES
  (1, 'global_download_path', '/tmp/admin-base/download', '文件下载路径'),
  (2, 'global_upload_path', '/tmp/admin-base/upload', '文件上传路径'),
  (3, 'sys_version', '2.0.0', '系统版本号');