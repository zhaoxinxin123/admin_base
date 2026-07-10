package com.admin.base.utils;

import com.admin.base.constant.YesOrNo;
import com.admin.base.dto.response.system.PermissionResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZXX
 * @version 1.0
 * @date 2021/9/14 3:21 下午
 * @desc
 */
public class MenuUtils {
    /**
     * 递归将权限列表形成数   管理员登---获取列表
     *
     * @param permissionResponses 所有权限信息
     * @param permissionId        其中一个一级列表id
     * @param menu                其中一个一级列表id
     * @return 树状结构的权限列表
     */
    public static List<PermissionResponse> getChild(List<PermissionResponse> permissionResponses, Long permissionId, PermissionResponse menu) {
        List<PermissionResponse> childList = new ArrayList<>();
        permissionResponses.forEach(permissionResponse -> {
            if (permissionResponse.getParentId().equals(permissionId)) {
                childList.add(permissionResponse);
            }
        });
        //可以在此给子菜单排序
        childList.forEach(item -> getChild(permissionResponses, item.getPermissionId(), item));
        menu.setChild(childList);
        return childList;
    }

    /**
     * 穷举所有权限（rootMenus）   用户角色修改时 是否选中
     *
     * @param rootMenus 菜单权限
     * @param list      管理员拥有的权限id
     * @return rootMenus
     */
    public static List<PermissionResponse> setSelected(List<PermissionResponse> rootMenus, List<Long> list) {
        for (PermissionResponse rootMenu : rootMenus) {
            if (list.contains(rootMenu.getPermissionId())) {
                if (rootMenu.getChild() != null) {
                    setSelected(rootMenu.getChild(), list);
                }
                rootMenu.setSelected(YesOrNo.YES);
            }
        }
        return rootMenus;
    }
}
