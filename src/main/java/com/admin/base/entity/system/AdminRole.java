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
 * <p>
 * 
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("tb_sys_admin_role")
public class AdminRole extends Model<AdminRole> implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer adminId;

    private Integer roleId;

    private LocalDateTime createTime;


}
