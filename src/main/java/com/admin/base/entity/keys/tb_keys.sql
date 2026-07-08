-- auto Generated on 2023-03-28
-- DROP TABLE IF EXISTS `keys`;
CREATE TABLE `keys`(
	id INT (11) NOT NULL AUTO_INCREMENT COMMENT 'id',
    key_word VARCHAR (50) NOT NULL COMMENT '关键词',
	note VARCHAR (50) NOT NULL COMMENT '备注',
	user_id INT (11) NOT NULL COMMENT '用户ID',
	create_time DATETIME NOT NULL  COMMENT '创建时间',
	update_time DATETIME NOT NULL  COMMENT '更新时间',
	INDEX(key_word),
	INDEX(user_id),
	PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'keys';
