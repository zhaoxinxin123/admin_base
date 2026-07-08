package com.admin.base.entity.system;

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
 * @date 2021/8/19 2:18 下午
 * @desc 继承Model<Admin>  可以使用实体类进行增删改查
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_sys_admin")
public class Admin extends Model<Admin> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "admin_id", type = IdType.AUTO)
    private Integer adminId;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 登录名
     */
    private String userName;
    /**
     * 密码
     */
    private String password;
    /**
     * 状态
     */
    private Integer state;

    /**
     * 密码
     */
    private String passwordShow;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
