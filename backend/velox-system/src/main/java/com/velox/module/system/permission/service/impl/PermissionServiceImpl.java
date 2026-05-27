package com.velox.module.system.permission.service.impl;

import com.velox.module.system.common.constants.SystemRoleCode;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.module.system.domain.model.Menu;
import com.velox.module.system.domain.model.Role;
import com.velox.module.system.domain.model.RoleMenuPermission;
import com.velox.module.system.domain.model.UserRole;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.persistence.MenuMapper;
import com.velox.module.system.persistence.RoleMapper;
import com.velox.module.system.persistence.RoleMenuPermissionMapper;
import com.velox.module.system.persistence.UserRoleMapper;
import com.velox.module.system.persistence.support.MenuQuerySupport;
import com.velox.module.system.permission.service.PermissionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final RoleMenuPermissionMapper roleMenuPermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final SecuritySessionService securitySessionService;

    public PermissionServiceImpl(RoleMapper roleMapper,
                                 MenuMapper menuMapper,
                                 RoleMenuPermissionMapper roleMenuPermissionMapper,
                                 UserRoleMapper userRoleMapper,
                                 SystemEntityIdGenerator entityIdGenerator,
                                 SecuritySessionService securitySessionService) {
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.roleMenuPermissionMapper = roleMenuPermissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.entityIdGenerator = entityIdGenerator;
        this.securitySessionService = securitySessionService;
    }

    @Override
    public Set<String> getRoleMenuIds(String roleId) {
        String normalizedRoleId = normalizeId(roleId);
        if (normalizedRoleId == null) {
            return Set.of();
        }
        return getRoleMenuIds(Set.of(normalizedRoleId));
    }

    @Override
    public Set<String> getRoleMenuIds(Collection<String> roleIds) {
        Set<String> normalizedRoleIds = normalizeIds(roleIds);
        if (normalizedRoleIds.isEmpty()) {
            return Set.of();
        }
        return roleMenuPermissionMapper.selectList(new LambdaQueryWrapper<RoleMenuPermission>()
                        .eq(RoleMenuPermission::getDeleted, 0)
                        .in(RoleMenuPermission::getRoleId, normalizedRoleIds))
                .stream()
                .map(RoleMenuPermission::getMenuId)
                .map(this::normalizeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleMenu(String roleId, Collection<String> menuIds) {
        String normalizedRoleId = normalizeId(roleId);
        Role role = normalizedRoleId == null ? null : roleMapper.selectById(normalizedRoleId);
        if (role == null || Integer.valueOf(1).equals(role.getDeleted())) {
            throw new ApiException(BusinessErrorCode.ROLE_NOT_FOUND);
        }

        Set<String> requestedMenuIds = normalizeIds(menuIds);

        Map<String, RoleMenuPermission> activePermissionMap = new LinkedHashMap<>();
        Map<String, RoleMenuPermission> deletedPermissionMap = new LinkedHashMap<>();
        for (RoleMenuPermission permission : roleMenuPermissionMapper.selectAllByRoleId(normalizedRoleId)) {
            String menuId = normalizeId(permission.getMenuId());
            if (menuId == null) {
                continue;
            }
            if (Integer.valueOf(1).equals(permission.getDeleted())) {
                deletedPermissionMap.merge(menuId, permission, this::preferLatestPermission);
                continue;
            }
            activePermissionMap.merge(menuId, permission, this::preferLatestPermission);
        }
        Set<String> dbMenuIds = new LinkedHashSet<>(activePermissionMap.keySet());

        Set<String> validMenuIds = requestedMenuIds.isEmpty() ? Set.of() : menuMapper.selectList(MenuQuerySupport.selectColumns(new LambdaQueryWrapper<Menu>())
                        .eq(Menu::getDeleted, 0)
                        .eq(Menu::getIsEnable, 1)
                        .in(Menu::getId, requestedMenuIds))
                .stream()
                .map(Menu::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> createIds = new LinkedHashSet<>(validMenuIds);
        createIds.removeAll(dbMenuIds);

        Set<String> deleteIds = new LinkedHashSet<>(dbMenuIds);
        deleteIds.removeAll(validMenuIds);

        String operator = currentOperator();
        if (!deleteIds.isEmpty()) {
            List<String> deleteRelationIds = deleteIds.stream()
                    .map(activePermissionMap::get)
                    .filter(Objects::nonNull)
                    .map(RoleMenuPermission::getId)
                    .filter(Objects::nonNull)
                    .toList();
            if (!deleteRelationIds.isEmpty()) {
                roleMenuPermissionMapper.logicalDeleteByIds(deleteRelationIds, operator);
            }
        }

        List<String> restoreRelationIds = createIds.stream()
                .map(deletedPermissionMap::get)
                .filter(Objects::nonNull)
                .map(RoleMenuPermission::getId)
                .filter(Objects::nonNull)
                .toList();
        if (!restoreRelationIds.isEmpty()) {
            roleMenuPermissionMapper.restoreByIds(restoreRelationIds, operator);
            createIds.removeIf(deletedPermissionMap::containsKey);
        }

        if (!createIds.isEmpty()) {
            List<RoleMenuPermission> entities = new ArrayList<>(createIds.size());
            for (String menuId : createIds) {
                RoleMenuPermission permission = new RoleMenuPermission();
                permission.setId(entityIdGenerator.nextId(RoleMenuPermission.class));
                permission.setRoleId(normalizedRoleId);
                permission.setMenuId(menuId);
                permission.setDeleted(0);
                permission.setCreateBy(operator);
                permission.setUpdateBy(operator);
                entities.add(permission);
            }
            for (RoleMenuPermission entity : entities) {
                roleMenuPermissionMapper.insert(entity);
            }
        }
    }

    @Override
    public Set<String> getUserRoleIds(String userId) {
        String normalizedUserId = normalizeId(userId);
        if (normalizedUserId == null) {
            return Set.of();
        }
        return getUserRoleIds(Set.of(normalizedUserId)).getOrDefault(normalizedUserId, Set.of());
    }

    @Override
    public Map<String, Set<String>> getUserRoleIds(Collection<String> userIds) {
        Set<String> normalizedUserIds = normalizeIds(userIds);
        if (normalizedUserIds.isEmpty()) {
            return Map.of();
        }
        Map<String, Set<String>> userRoles = new LinkedHashMap<>();
        userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getDeleted, 0)
                        .in(UserRole::getUserId, normalizedUserIds))
                .forEach(item -> {
                    String normalizedUserId = normalizeId(item.getUserId());
                    String normalizedRoleId = normalizeId(item.getRoleId());
                    if (normalizedUserId == null || normalizedRoleId == null) {
                        return;
                    }
                    userRoles.computeIfAbsent(normalizedUserId, key -> new LinkedHashSet<>()).add(normalizedRoleId);
                });
        return userRoles;
    }

    @Override
    public List<String> getUserRoleCodes(String userId) {
        String normalizedUserId = normalizeId(userId);
        if (normalizedUserId == null) {
            return List.of();
        }
        return getUserRoleCodes(Set.of(normalizedUserId)).getOrDefault(normalizedUserId, List.of());
    }

    @Override
    public Map<String, List<String>> getUserRoleCodes(Collection<String> userIds) {
        Set<String> normalizedUserIds = normalizeIds(userIds);
        if (normalizedUserIds.isEmpty()) {
            return Map.of();
        }
        Map<String, Set<String>> userRoleIdMap = getUserRoleIds(normalizedUserIds);
        Set<String> allRoleIds = userRoleIdMap.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (allRoleIds.isEmpty()) {
            return Map.of();
        }

        Map<String, String> roleCodeMap = roleMapper.selectList(new LambdaQueryWrapper<Role>()
                        .eq(Role::getDeleted, 0)
                        .in(Role::getId, allRoleIds))
                .stream()
                .map(role -> Map.entry(normalizeId(role.getId()), role.getRoleCode()))
                .filter(entry -> entry.getKey() != null && entry.getValue() != null && !entry.getValue().isBlank())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left, LinkedHashMap::new));

        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : userRoleIdMap.entrySet()) {
            List<String> roleCodes = entry.getValue().stream()
                    .map(roleCodeMap::get)
                    .filter(Objects::nonNull)
                    .toList();
            result.put(entry.getKey(), roleCodes);
        }
        return result;
    }

    @Override
    public Integer getUserHighestRoleLevel(String userId) {
        String normalizedUserId = normalizeId(userId);
        if (normalizedUserId == null) {
            return 0;
        }
        return getUserHighestRoleLevels(Set.of(normalizedUserId)).getOrDefault(normalizedUserId, 0);
    }

    @Override
    public Map<String, Integer> getUserHighestRoleLevels(Collection<String> userIds) {
        Set<String> normalizedUserIds = normalizeIds(userIds);
        if (normalizedUserIds.isEmpty()) {
            return Map.of();
        }

        Map<String, Set<String>> userRoleIdMap = getUserRoleIds(normalizedUserIds);
        Set<String> allRoleIds = userRoleIdMap.values().stream()
                .flatMap(Set::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, Integer> roleLevelMap = allRoleIds.isEmpty()
                ? Map.of()
                : roleMapper.selectList(new LambdaQueryWrapper<Role>()
                                .eq(Role::getDeleted, 0)
                                .in(Role::getId, allRoleIds))
                        .stream()
                        .collect(Collectors.toMap(
                                role -> normalizeId(role.getId()),
                                role -> role.getRoleLevel() == null ? 0 : role.getRoleLevel(),
                                (left, right) -> left,
                                LinkedHashMap::new
                        ));

        Map<String, Integer> result = new LinkedHashMap<>();
        for (String userId : normalizedUserIds) {
            int highestLevel = userRoleIdMap.getOrDefault(userId, Set.of()).stream()
                    .map(roleLevelMap::get)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(0);
            result.put(userId, highestLevel);
        }
        return result;
    }

    @Override
    public List<String> getUserPermissionMarks(String userId) {
        String normalizedUserId = normalizeId(userId);
        if (normalizedUserId == null) {
            return List.of();
        }

        boolean isSuperAdmin = getUserRoleCodes(normalizedUserId).stream().anyMatch(SystemRoleCode.R_SUPER::equals);
        LambdaQueryWrapper<Menu> wrapper = MenuQuerySupport.selectColumns(new LambdaQueryWrapper<Menu>())
                .eq(Menu::getDeleted, 0)
                .eq(Menu::getIsEnable, 1);

        if (!isSuperAdmin) {
            Set<String> roleIds = getUserRoleIds(normalizedUserId);
            if (roleIds.isEmpty()) {
                return List.of();
            }

            Set<String> menuIds = getRoleMenuIds(roleIds);
            if (menuIds.isEmpty()) {
                return List.of();
            }
            wrapper.in(Menu::getId, menuIds);
        }

        return menuMapper.selectList(wrapper).stream()
                .map(Menu::getAuthMark)
                .filter(mark -> mark != null && !mark.isBlank())
                .distinct()
                .toList();
    }

    @Override
    public Set<String> getUserPermittedMenuIds(String userId) {
        String normalizedUserId = normalizeId(userId);
        if (normalizedUserId == null) {
            return Set.of();
        }

        if (getUserRoleCodes(normalizedUserId).stream().anyMatch(SystemRoleCode.R_SUPER::equals)) {
            return menuMapper.selectList(MenuQuerySupport.selectColumns(new LambdaQueryWrapper<Menu>())
                            .eq(Menu::getDeleted, 0)
                            .eq(Menu::getIsEnable, 1))
                    .stream()
                    .map(Menu::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        Set<String> roleIds = getUserRoleIds(normalizedUserId);
        if (roleIds.isEmpty()) {
            return Set.of();
        }
        return getRoleMenuIds(roleIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private RoleMenuPermission preferLatestPermission(RoleMenuPermission left, RoleMenuPermission right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        if (right.getUpdateTime() != null && (left.getUpdateTime() == null || right.getUpdateTime().isAfter(left.getUpdateTime()))) {
            return right;
        }
        if (left.getUpdateTime() != null && right.getUpdateTime() == null) {
            return left;
        }
        if (right.getCreateTime() != null && (left.getCreateTime() == null || right.getCreateTime().isAfter(left.getCreateTime()))) {
            return right;
        }
        return left;
    }

    private String currentOperator() {
        String loginId = securitySessionService.currentLoginIdOrNull();
        return loginId == null || loginId.isBlank() ? "system" : loginId;
    }

    private String normalizeId(String id) {
        if (id == null) {
            return null;
        }
        String normalized = id.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private Set<String> normalizeIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        return ids.stream()
                .map(this::normalizeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
