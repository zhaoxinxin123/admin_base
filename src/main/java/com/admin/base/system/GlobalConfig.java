package com.admin.base.system;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/16 10:58 上午
 * @desc
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("tb_sys_global_config")
public class GlobalConfig extends Model<GlobalConfig> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "config_id", type = IdType.AUTO)
    private Integer configId;

    /**
     * 配置名
     */
    private String configValue;

    /**
     * key
     */
    private String configKey;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 备注
     */
    private String note;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
