
-- auto Generated on 2023-04-03
DROP TABLE IF EXISTS tb_records;
CREATE TABLE tb_records(
	id INT (11) NOT NULL AUTO_INCREMENT COMMENT '关键词ID',
	file_name VARCHAR (50) NOT NULL COMMENT '上传文件名',
	user_id INT (50) NOT NULL COMMENT '上传用户名',
	result_path VARCHAR (50) NOT NULL COMMENT '识别结果',
	crete_time DATETIME NOT NULL COMMENT '创建时间',
	update_time DATETIME NOT NULL COMMENT '更新时间',
	PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'tb_records';
-- auto Generated on 2023-04-03
DROP TABLE IF EXISTS records;
CREATE TABLE records(
	id INT (11) NOT NULL AUTO_INCREMENT COMMENT '关键词ID',
	file_name VARCHAR (50) NOT NULL COMMENT '上传文件名',
	user_id INT (11) NOT NULL COMMENT '上传用户名',
	key_word_list VARCHAR (50) NOT NULL COMMENT '关键词列表',
	result_path VARCHAR (50) NOT NULL COMMENT '识别结果',
	create_time DATETIME NOT NULL COMMENT '创建时间',
	update_time DATETIME NOT NULL COMMENT '更新时间',
	PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'records';
-- auto Generated on 2023-04-06
-- DROP TABLE IF EXISTS tb_records;
CREATE TABLE tb_records(
	id INT (11) NOT NULL AUTO_INCREMENT COMMENT '关键词ID',
	file_name VARCHAR (50) NOT NULL COMMENT '上传文件名',
	user_id INT (11) NOT NULL COMMENT '上传用户名',
	key_word_list VARCHAR (50) NOT NULL COMMENT '关键词列表',
	result_path VARCHAR (50) NOT NULL COMMENT '识别结果',
	result_state INT (11) NOT NULL COMMENT '是否有识别结果
     * 0:未匹配到关键词内容
     * 1：匹配到关键词内容',
	create_time DATETIME NOT NULL COMMENT '创建时间',
	update_time DATETIME NOT NULL COMMENT '更新时间',
	PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'tb_records';
