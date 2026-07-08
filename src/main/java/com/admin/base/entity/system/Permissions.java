package com.admin.base.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 菜单和按钮
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("tb_sys_permissions")
public class Permissions implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "permission_id", type = IdType.AUTO)
    private Integer permissionId;
    /**
     * 级别
     */
    private Integer level;
    /**
     * 上级Id
     */
    private Integer parentId;
    /**
     * 路径
     */
    private String path;
    /**
     * 权限名
     */
    private String perm;
    /**
     * 是否需要认证
     */
    private Integer requireAuth;
    /**
     * 状态   1 正常
     * 0 删除
     */
    private Integer state;
    /**
     * url 可以存放图标或者其他
     */
    private String url;
    /**
     * 标题
     */
    private String title;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
