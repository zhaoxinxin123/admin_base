package com.admin.base.entity.keys;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author ZXX
 * @version 1.0
 * @date 2023/3/28 14:25
 * @desc 关键词表
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_keys")
public class Keys extends Model<Keys> implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 关键词
     */
    private String keyWord;
    /**
     * 备注
     */
    private String note;
    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
