-- Admin Base v2 Seed Data Draft
-- Local-development seed data for retained system tables.
-- BCrypt password hash is for local dev only; production uses environment-specific credentials.

INSERT INTO tb_sys_admin (admin_id, nickname, user_name, password, state)
VALUES (1, '管理员', 'admin', '$2a$10$KCq.c/d5K6ZuWDlKxOtokON5Vr3zssxrW1IMDaQpnF9oge1f9qwUi', 0);

INSERT INTO tb_sys_role (role_id, role_name, note)
VALUES (1, 'ROLE_ADMIN', '管理员');

INSERT INTO tb_sys_global_config (config_id, config_key, config_value, note)
VALUES
  (1, 'global_download_path', '/tmp/admin-base/download', '文件下载路径'),
  (2, 'global_upload_path', '/tmp/admin-base/upload', '文件上传路径'),
  (3, 'sys_version', '2.0.0', '系统版本号');