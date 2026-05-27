package com.velox.module.system.permission.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PermissionService {

    Set<String> getRoleMenuIds(String roleId);

    Set<String> getRoleMenuIds(Collection<String> roleIds);

    void assignRoleMenu(String roleId, Collection<String> menuIds);

    Set<String> getUserRoleIds(String userId);

    Map<String, Set<String>> getUserRoleIds(Collection<String> userIds);

    List<String> getUserRoleCodes(String userId);

    Map<String, List<String>> getUserRoleCodes(Collection<String> userIds);

    Integer getUserHighestRoleLevel(String userId);

    Map<String, Integer> getUserHighestRoleLevels(Collection<String> userIds);

    List<String> getUserPermissionMarks(String userId);

    /**
     * 获取指定用户可见的菜单 ID 集合（启用、未删除）。
     * R_SUPER 直通所有启用菜单；普通用户为其角色累积的菜单 ID。
     * 用于：导航菜单渲染、角色授权范围校验等"个人可见范围"场景。
     */
    Set<String> getUserPermittedMenuIds(String userId);
}
