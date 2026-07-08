package com.admin.base.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.admin.base.dto.request.system.LoginParam;
import com.admin.base.dto.response.system.LoginResponse;
import com.admin.base.entity.system.Admin;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author ZXX
 * @since 2021-09-05
 */
public interface IAdminService extends IService<Admin> {
    /**
     * 通过登录名查询管理员
     *
     * @param username 登录名
     * @return 管理员
     */
    Admin selectByUserName(String username);


    /**
     * 新增管理员
     *
     * @param username 用户名
     * @param password 密码
     * @param nickName 昵称
     * @param roleIds   角色Ids
     */
    void addAdmin(String username, String password, String nickName, List<Integer> roleIds);


    /**
     * 删除管理员
     *
     * @param adminId 管理员ID
     */
    void deleteAdmin(Integer adminId);

    /**
     * 修改密码
     *
     * @param adminId    管理员Id
     * @param password 密码
     */
    void updatePassword(Integer adminId, String password);

    /**
     * 查询管理员列表
     *
     * @param page     当前页码
     * @param size     每页大小
     * @param username 用户名
     * @return IPage<Admin>
     */
    IPage<Admin> getAdminList(Integer page, Integer size, String username);

    /**
     * 登录
     * @param loginParam  登录参数
     * @return 登录信息
     */
    LoginResponse login(LoginParam loginParam);

    /**
     * 更新管理员状态
     * @param adminId  管理员id
     * @param state  管理员状态
     */
    void updateAdminState(Integer adminId, Integer state);
}
