package com.velox.module.system.role.service.impl;

import com.velox.module.system.common.constants.SystemRoleCode;
import com.velox.module.system.common.enums.RoleTypeEnum;
import com.velox.common.result.PageResult;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.module.system.domain.model.Menu;
import com.velox.module.system.domain.model.Role;
import com.velox.module.system.domain.model.RoleMenuPermission;
import com.velox.module.system.domain.model.UserRole;
import com.velox.module.system.persistence.MenuMapper;
import com.velox.module.system.persistence.RoleMapper;
import com.velox.module.system.persistence.RoleMenuPermissionMapper;
import com.velox.module.system.persistence.UserRoleMapper;
import com.velox.module.system.persistence.support.MenuQuerySupport;
import com.velox.framework.web.RequestDateTimeFormatter;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.permission.service.PermissionService;
import com.velox.module.system.role.dto.RoleListItemDTO;
import com.velox.module.system.role.dto.RoleMenuPermissionUpdateCommand;
import com.velox.module.system.role.dto.RoleQuery;
import com.velox.module.system.role.dto.RoleSaveCommand;
import com.velox.module.system.role.service.RoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final MenuMapper menuMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuPermissionMapper roleMenuPermissionMapper;
    private final PermissionService permissionService;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final SecuritySessionService securitySessionService;

    public RoleServiceImpl(
            MenuMapper menuMapper,
            RoleMapper roleMapper,
            UserRoleMapper userRoleMapper,
            RoleMenuPermissionMapper roleMenuPermissionMapper,
            PermissionService permissionService,
            SystemEntityIdGenerator entityIdGenerator,
            SecuritySessionService securitySessionService
    ) {
        this.menuMapper = menuMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMenuPermissionMapper = roleMenuPermissionMapper;
        this.permissionService = permissionService;
        this.entityIdGenerator = entityIdGenerator;
        this.securitySessionService = securitySessionService;
    }

    @Override
    public PageResult<RoleListItemDTO> list(RoleQuery query) {
        Page<Role> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getDeleted, 0);
        if (query.getRoleName() != null && !query.getRoleName().isEmpty()) {
            wrapper.like(Role::getRoleName, query.getRoleName());
        }
        if (query.getRoleCode() != null && !query.getRoleCode().isEmpty()) {
            wrapper.like(Role::getRoleCode, query.getRoleCode());
        }
        if (query.getDescription() != null && !query.getDescription().isEmpty()) {
            wrapper.like(Role::getDescription, query.getDescription());
        }
        if (query.getType() != null) {
            wrapper.eq(Role::getType, query.getType());
        }
        if (query.getEnabled() != null) {
            wrapper.eq(Role::getEnabled, query.getEnabled() ? 1 : 0);
        }
        if (StringUtils.hasText(query.getCreateTimeStart())) {
            wrapper.ge(Role::getCreateTime, RequestDateTimeFormatter.parseToUtc(query.getCreateTimeStart()));
        } else if (StringUtils.hasText(query.getStartTime())) {
            wrapper.ge(Role::getCreateTime,
                    RequestDateTimeFormatter.toUtcStartOfDay(LocalDate.parse(query.getStartTime())));
        }
        if (StringUtils.hasText(query.getCreateTimeEnd())) {
            wrapper.le(Role::getCreateTime, RequestDateTimeFormatter.parseToUtc(query.getCreateTimeEnd()));
        } else if (StringUtils.hasText(query.getEndTime())) {
            wrapper.le(Role::getCreateTime,
                    RequestDateTimeFormatter.toUtcEndOfDay(LocalDate.parse(query.getEndTime())));
        }
        if (StringUtils.hasText(query.getUpdateTimeStart())) {
            wrapper.ge(Role::getUpdateTime, RequestDateTimeFormatter.parseToUtc(query.getUpdateTimeStart()));
        }
        if (StringUtils.hasText(query.getUpdateTimeEnd())) {
            wrapper.le(Role::getUpdateTime, RequestDateTimeFormatter.parseToUtc(query.getUpdateTimeEnd()));
        }
        wrapper.orderByDesc(Role::getCreateTime)
                .orderByDesc(Role::getUpdateTime);

        Page<Role> result = roleMapper.selectPage(page, wrapper);

        List<RoleListItemDTO> list = result.getRecords().stream()
                .map(this::toRoleListItem)
                .collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), query.getCurrent(), query.getSize(), list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(RoleSaveCommand command) {
        ensureRoleCodeUnique(command.getRoleCode(), null);
        Role role = new Role();
        role.setId(entityIdGenerator.nextId(Role.class));
        role.setRoleName(command.getRoleName().trim());
        role.setRoleCode(command.getRoleCode().trim());
        role.setDescription(command.getDescription().trim());
        role.setType(RoleTypeEnum.CUSTOM.getCode());
        role.setEnabled(Boolean.TRUE.equals(command.getEnabled()) ? 1 : 0);
        role.setDeleted(0);
        role.setCreateBy(currentOperator());
        role.setUpdateBy(currentOperator());
        roleMapper.insert(role);
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(String roleId, RoleSaveCommand command) {
        Role role = getActiveRole(roleId);
        ensureRoleEditable(role, command);
        ensureRoleCodeUnique(command.getRoleCode(), roleId);
        role.setRoleName(command.getRoleName().trim());
        role.setRoleCode(command.getRoleCode().trim());
        role.setDescription(command.getDescription().trim());
        role.setEnabled(Boolean.TRUE.equals(command.getEnabled()) ? 1 : 0);
        role.setUpdateBy(currentOperator());
        roleMapper.updateById(role);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(String roleId) {
        Role role = getActiveRole(roleId);
        if (Integer.valueOf(RoleTypeEnum.SYSTEM.getCode()).equals(role.getType())) {
            throw new ApiException(BusinessErrorCode.SYSTEM_ROLE_DELETE_FORBIDDEN);
        }
        long userBindingCount = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getDeleted, 0)
                .eq(UserRole::getRoleId, roleId));
        if (userBindingCount > 0) {
            throw new ApiException(BusinessErrorCode.DATA_IN_USE);
        }

        roleMapper.update(null, new LambdaUpdateWrapper<Role>()
                .eq(Role::getId, roleId)
                .eq(Role::getDeleted, 0)
                .set(Role::getDeleted, 1)
                .set(Role::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC))
                .set(Role::getUpdateBy, currentOperator()));
        roleMenuPermissionMapper.delete(new LambdaQueryWrapper<RoleMenuPermission>().eq(RoleMenuPermission::getRoleId, roleId));
        return role != null;
    }

    @Override
    public List<String> getRoleMenuPermissions(String roleId) {
        getActiveRole(roleId);
        Set<String> grantedMenuIds = permissionService.getRoleMenuIds(roleId);
        if (grantedMenuIds.isEmpty()) {
            return List.of();
        }
        return compressGrantedMenuIds(grantedMenuIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRoleMenuPermissions(String roleId, RoleMenuPermissionUpdateCommand command) {
        List<String> requestedMenuIds = command == null ? List.of() : command.getMenuIds();
        ensureWithinCurrentUserScope(requestedMenuIds);
        permissionService.assignRoleMenu(roleId, requestedMenuIds);
        return true;
    }

    /**
     * 防止越权授权：只允许把自己拥有的菜单授给别的角色。
     * R_SUPER 由 {@link PermissionService#getUserPermittedMenuIds} 内部直通全部菜单，天然通过。
     */
    private void ensureWithinCurrentUserScope(List<String> requestedMenuIds) {
        if (requestedMenuIds == null || requestedMenuIds.isEmpty()) {
            return;
        }
        String currentUserId = securitySessionService.requireCurrentLoginId();
        Set<String> permittedMenuIds = permissionService.getUserPermittedMenuIds(currentUserId);
        boolean hasBeyondScope = requestedMenuIds.stream()
                .filter(Objects::nonNull)
                .anyMatch(menuId -> !permittedMenuIds.contains(menuId));
        if (hasBeyondScope) {
            throw new ApiException(BusinessErrorCode.ROLE_MENU_GRANT_BEYOND_SCOPE);
        }
    }

    private List<String> compressGrantedMenuIds(Set<String> grantedMenuIds) {
        List<Menu> menus = menuMapper.selectList(MenuQuerySupport.selectColumns(new LambdaQueryWrapper<Menu>())
                .eq(Menu::getDeleted, 0)
                .eq(Menu::getIsEnable, 1)
                .orderByAsc(Menu::getSort)
                .orderByAsc(Menu::getCreateTime));
        if (menus.isEmpty()) {
            return List.of();
        }

        Map<String, Menu> menuMap = menus.stream()
                .filter(menu -> menu.getId() != null)
                .collect(Collectors.toMap(Menu::getId, menu -> menu, (left, right) -> left, LinkedHashMap::new));
        Map<String, List<Menu>> childrenMap = new LinkedHashMap<>();
        List<Menu> roots = new ArrayList<>();

        for (Menu menu : menus) {
            if (menu.getId() == null) {
                continue;
            }
            Menu parent = menu.getParentId() == null ? null : menuMap.get(menu.getParentId());
            if (parent == null) {
                roots.add(menu);
                continue;
            }
            childrenMap.computeIfAbsent(parent.getId(), key -> new ArrayList<>()).add(menu);
        }

        LinkedHashSet<String> explicitMenuIds = new LinkedHashSet<>();
        for (Menu root : roots) {
            collectExplicitMenuIds(root, childrenMap, grantedMenuIds, explicitMenuIds);
        }

        grantedMenuIds.stream()
                .filter(Objects::nonNull)
                .filter(grantedId -> !menuMap.containsKey(grantedId))
                .forEach(explicitMenuIds::add);
        return new ArrayList<>(explicitMenuIds);
    }

    private PermissionGrantState collectExplicitMenuIds(
            Menu menu,
            Map<String, List<Menu>> childrenMap,
            Set<String> grantedMenuIds,
            Set<String> explicitMenuIds
    ) {
        String menuId = menu.getId();
        boolean selfGranted = menuId != null && grantedMenuIds.contains(menuId);
        List<Menu> children = childrenMap.getOrDefault(menuId, List.of());

        if (children.isEmpty()) {
            if (selfGranted && menuId != null) {
                explicitMenuIds.add(menuId);
            }
            return new PermissionGrantState(selfGranted, selfGranted);
        }

        boolean anyChildGranted = false;
        boolean allChildrenFullyGranted = true;
        LinkedHashSet<String> childExplicitIds = new LinkedHashSet<>();
        for (Menu child : children) {
            PermissionGrantState childState = collectExplicitMenuIds(child, childrenMap, grantedMenuIds, childExplicitIds);
            anyChildGranted = anyChildGranted || childState.anyGranted;
            allChildrenFullyGranted = allChildrenFullyGranted && childState.fullyGranted;
        }

        if (selfGranted && (allChildrenFullyGranted || !anyChildGranted)) {
            if (menuId != null) {
                explicitMenuIds.add(menuId);
            }
        } else {
            explicitMenuIds.addAll(childExplicitIds);
        }

        return new PermissionGrantState(selfGranted || anyChildGranted, selfGranted && allChildrenFullyGranted);
    }

    private static final class PermissionGrantState {
        private final boolean anyGranted;
        private final boolean fullyGranted;

        private PermissionGrantState(boolean anyGranted, boolean fullyGranted) {
            this.anyGranted = anyGranted;
            this.fullyGranted = fullyGranted;
        }
    }

    private RoleListItemDTO toRoleListItem(Role role) {
        RoleListItemDTO dto = new RoleListItemDTO();
        dto.setRoleId(role.getId());
        dto.setRoleName(role.getRoleName());
        dto.setRoleCode(role.getRoleCode());
        dto.setDescription(role.getDescription());
        dto.setType(role.getType());
        dto.setTypeName(RoleTypeEnum.getDesc(role.getType()));
        dto.setEnabled(Integer.valueOf(1).equals(role.getEnabled()));
        dto.setCreateTime(RequestDateTimeFormatter.format(role.getCreateTime()));
        dto.setUpdateTime(RequestDateTimeFormatter.format(role.getUpdateTime()));
        return dto;
    }

    private void ensureRoleEditable(Role role, RoleSaveCommand command) {
        if (SystemRoleCode.R_SUPER.equals(role.getRoleCode())) {
            throw new ApiException(BusinessErrorCode.SUPER_ROLE_EDIT_FORBIDDEN);
        }
        if (Integer.valueOf(RoleTypeEnum.SYSTEM.getCode()).equals(role.getType())) {
            String targetRoleCode = command.getRoleCode() == null ? null : command.getRoleCode().trim();
            if (StringUtils.hasText(targetRoleCode) && !targetRoleCode.equals(role.getRoleCode())) {
                throw new ApiException(BusinessErrorCode.SYSTEM_ROLE_CODE_IMMUTABLE);
            }
        }
    }

    private void ensureRoleCodeUnique(String roleCode, String excludeRoleId) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<Role>()
                .eq(Role::getDeleted, 0)
                .eq(Role::getRoleCode, roleCode == null ? null : roleCode.trim());
        if (excludeRoleId != null) {
            wrapper.ne(Role::getId, excludeRoleId);
        }
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new ApiException(BusinessErrorCode.DATA_ALREADY_EXISTS);
        }
    }

    private Role getActiveRole(String roleId) {
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getId, roleId)
                .eq(Role::getDeleted, 0)
                .last("limit 1"));
        if (role == null) {
            throw new ApiException(BusinessErrorCode.ROLE_NOT_FOUND);
        }
        return role;
    }

    private String currentOperator() {
        String loginId = securitySessionService.currentLoginIdOrNull();
        return StringUtils.hasText(loginId) ? loginId : "system";
    }
}
