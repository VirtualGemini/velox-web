package com.velox.module.system.menu.service.impl;

import com.velox.module.system.common.constants.SystemRoleCode;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.module.system.domain.model.Menu;
import com.velox.module.system.domain.model.Role;
import com.velox.module.system.persistence.MenuMapper;
import com.velox.module.system.persistence.RoleMapper;
import com.velox.module.system.persistence.RoleMenuPermissionMapper;
import com.velox.module.system.persistence.support.MenuQuerySupport;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.framework.web.RequestDateTimeFormatter;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.permission.service.PermissionService;
import com.velox.module.system.menu.dto.AuthItemDTO;
import com.velox.module.system.menu.dto.MenuMetaDTO;
import com.velox.module.system.menu.dto.MenuRouteDTO;
import com.velox.module.system.menu.dto.MenuSaveCommand;
import com.velox.module.system.menu.service.MenuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
public class MenuServiceImpl implements MenuService {

    private static final String MENU_TYPE_BUTTON = "button";

    private final MenuMapper menuMapper;
    private final RoleMapper roleMapper;
    private final RoleMenuPermissionMapper roleMenuPermissionMapper;
    private final PermissionService permissionService;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final SecuritySessionService securitySessionService;

    public MenuServiceImpl(
            MenuMapper menuMapper,
            RoleMapper roleMapper,
            RoleMenuPermissionMapper roleMenuPermissionMapper,
            PermissionService permissionService,
            SystemEntityIdGenerator entityIdGenerator,
            SecuritySessionService securitySessionService
    ) {
        this.menuMapper = menuMapper;
        this.roleMapper = roleMapper;
        this.roleMenuPermissionMapper = roleMenuPermissionMapper;
        this.permissionService = permissionService;
        this.entityIdGenerator = entityIdGenerator;
        this.securitySessionService = securitySessionService;
    }

    @Override
    public List<MenuRouteDTO> getSimpleMenus() {
        Set<String> permittedMenuIds = permissionService.getUserPermittedMenuIds(
                securitySessionService.requireCurrentLoginId());
        if (permittedMenuIds.isEmpty()) {
            return List.of();
        }

        // 把祖先菜单一并纳入：仅用于让路由树保持连通（譬如 /system/user-center 需要 System 父节点），
        // 不等同于授予按钮权限——按钮鉴权仍走 PermissionService 的 authMark 集合。
        Set<String> menuTreeIds = expandWithAncestors(permittedMenuIds);

        List<Menu> menus = menuMapper.selectList(
                MenuQuerySupport.selectColumns(new LambdaQueryWrapper<Menu>())
                        .eq(Menu::getDeleted, 0)
                        .eq(Menu::getIsEnable, 1)
                        .in(Menu::getId, menuTreeIds)
                        .orderByAsc(Menu::getSort)
                        .orderByAsc(Menu::getCreateTime)
        );

        Map<String, List<String>> menuRoleCodeMap = getMenuRoleCodes(menus.stream()
                .map(Menu::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)));

        Map<String, MenuRouteDTO> routeMap = new LinkedHashMap<>();
        List<Menu> buttonMenus = new ArrayList<>();

        for (Menu menu : menus) {
            if (MENU_TYPE_BUTTON.equalsIgnoreCase(menu.getMenuType())) {
                buttonMenus.add(menu);
                continue;
            }
            routeMap.put(menu.getId(), toRoute(menu, menuRoleCodeMap.get(menu.getId())));
        }

        for (Menu buttonMenu : buttonMenus) {
            MenuRouteDTO parent = routeMap.get(buttonMenu.getParentId());
            if (parent == null || parent.getMeta() == null) {
                continue;
            }
            AuthItemDTO authItemDTO = new AuthItemDTO();
            authItemDTO.setId(buttonMenu.getId());
            authItemDTO.setTitle(buttonMenu.getTitle());
            authItemDTO.setAuthMark(buttonMenu.getAuthMark());
            authItemDTO.setSort(buttonMenu.getSort());
            parent.getMeta().getAuthList().add(authItemDTO);
        }

        List<MenuRouteDTO> roots = new ArrayList<>();
        for (Menu menu : menus) {
            if (MENU_TYPE_BUTTON.equalsIgnoreCase(menu.getMenuType())) {
                continue;
            }
            MenuRouteDTO route = routeMap.get(menu.getId());
            if (route == null) {
                continue;
            }
            if (menu.getParentId() == null) {
                roots.add(route);
                continue;
            }
            MenuRouteDTO parent = routeMap.get(menu.getParentId());
            if (parent != null) {
                parent.getChildren().add(route);
            } else {
                roots.add(route);
            }
        }

        sortRoutes(roots);
        return roots;
    }

    private MenuRouteDTO toRoute(Menu menu, List<String> roleCodes) {
        MenuRouteDTO route = new MenuRouteDTO();
        route.setId(menu.getId());
        route.setPath(menu.getPath());
        route.setName(menu.getName());
        route.setComponent(menu.getComponent());
        route.setRedirect(menu.getRedirect());
        route.setMeta(toMeta(menu, roleCodes));
        return route;
    }

    private MenuMetaDTO toMeta(Menu menu, List<String> roleCodes) {
        MenuMetaDTO meta = new MenuMetaDTO();
        meta.setTitle(menu.getTitle());
        meta.setIcon(menu.getIcon());
        meta.setAuthMark(menu.getAuthMark());
        meta.setIsEnable(isTrue(menu.getIsEnable()));
        meta.setSort(menu.getSort());
        if (roleCodes != null && !roleCodes.isEmpty()) {
            meta.setRoles(roleCodes);
        }
        meta.setShowBadge(isTrue(menu.getShowBadge()));
        meta.setShowTextBadge(menu.getShowTextBadge());
        meta.setIsHide(isTrue(menu.getIsHide()));
        meta.setIsHideTab(isTrue(menu.getIsHideTab()));
        meta.setLink(menu.getLink());
        meta.setIsIframe(isTrue(menu.getIsIframe()));
        meta.setKeepAlive(isTrue(menu.getKeepAlive()));
        meta.setFixedTab(isTrue(menu.getFixedTab()));
        meta.setActivePath(menu.getActivePath());
        meta.setIsFullPage(isTrue(menu.getIsFullPage()));
        meta.setCreateTime(RequestDateTimeFormatter.format(menu.getCreateTime()));
        meta.setUpdateTime(RequestDateTimeFormatter.format(menu.getUpdateTime()));
        return meta;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(MenuSaveCommand command) {
        validateMenuCommand(command, null);
        ensureAuthMarkUnique(command.getAuthMark(), null);

        Menu menu = new Menu();
        menu.setId(entityIdGenerator.nextId(Menu.class));
        applyCommand(menu, command);
        menu.setDeleted(0);
        menu.setCreateBy(currentOperator());
        menu.setUpdateBy(currentOperator());
        menuMapper.insert(menu);
        bindMenuToSuperRole(menu.getId());
        return menu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(String menuId, MenuSaveCommand command) {
        Menu menu = getActiveMenu(menuId);
        validateMenuCommand(command, menuId);
        ensureAuthMarkUnique(command.getAuthMark(), menuId);
        applyCommand(menu, command);
        menu.setUpdateBy(currentOperator());
        menuMapper.updateById(menu);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(String menuId) {
        getActiveMenu(menuId);
        Set<String> deleteIds = collectDescendantIds(Set.of(menuId));
        String operator = currentOperator();

        roleMenuPermissionMapper.delete(new LambdaQueryWrapper<com.velox.module.system.domain.model.RoleMenuPermission>()
                .in(com.velox.module.system.domain.model.RoleMenuPermission::getMenuId, deleteIds));

        menuMapper.update(null, new LambdaUpdateWrapper<Menu>()
                .in(Menu::getId, deleteIds)
                .eq(Menu::getDeleted, 0)
                .set(Menu::getDeleted, 1)
                .set(Menu::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC))
                .set(Menu::getUpdateBy, operator));
        return true;
    }

    private boolean isTrue(Integer value) {
        return Integer.valueOf(1).equals(value);
    }

    private void sortRoutes(List<MenuRouteDTO> routes) {
        routes.sort((left, right) -> {
            Integer leftSort = left.getMeta() == null || left.getMeta().getSort() == null ? Integer.MAX_VALUE : left.getMeta().getSort();
            Integer rightSort = right.getMeta() == null || right.getMeta().getSort() == null ? Integer.MAX_VALUE : right.getMeta().getSort();
            int sortCompare = Integer.compare(leftSort, rightSort);
            if (sortCompare != 0) {
                return sortCompare;
            }
            String leftName = left.getName() == null ? "~" : left.getName();
            String rightName = right.getName() == null ? "~" : right.getName();
            return leftName.compareTo(rightName);
        });
        for (MenuRouteDTO route : routes) {
            if (route.getChildren() != null && !route.getChildren().isEmpty()) {
                sortRoutes(route.getChildren());
            }
        }
    }



    private void applyCommand(Menu menu, MenuSaveCommand command) {
        boolean isButton = MENU_TYPE_BUTTON.equalsIgnoreCase(command.getMenuType());
        menu.setParentId(isButton ? normalizeParentId(command.getParentId()) : normalizeMenuParentId(command.getParentId()));
        menu.setMenuType(command.getMenuType().trim().toLowerCase());
        menu.setName(command.getName().trim());
        menu.setTitle(command.getTitle().trim());
        menu.setPath(isButton ? null : normalizeNullable(command.getPath()));
        menu.setComponent(isButton ? null : normalizeNullable(command.getComponent()));
        menu.setRedirect(isButton ? null : normalizeNullable(command.getRedirect()));
        menu.setIcon(isButton ? null : normalizeNullable(command.getIcon()));
        menu.setAuthMark(normalizeNullable(command.getAuthMark()));
        menu.setIsEnable(Boolean.TRUE.equals(command.getIsEnable()) ? 1 : 0);
        menu.setSort(command.getSort() == null ? 1 : command.getSort());
        menu.setKeepAlive(Boolean.TRUE.equals(command.getKeepAlive()) ? 1 : 0);
        menu.setIsHide(Boolean.TRUE.equals(command.getIsHide()) ? 1 : 0);
        menu.setIsHideTab(Boolean.TRUE.equals(command.getIsHideTab()) ? 1 : 0);
        menu.setLink(normalizeNullable(command.getLink()));
        menu.setIsIframe(Boolean.TRUE.equals(command.getIsIframe()) ? 1 : 0);
        menu.setShowBadge(Boolean.TRUE.equals(command.getShowBadge()) ? 1 : 0);
        menu.setShowTextBadge(normalizeNullable(command.getShowTextBadge()));
        menu.setFixedTab(Boolean.TRUE.equals(command.getFixedTab()) ? 1 : 0);
        menu.setActivePath(normalizeNullable(command.getActivePath()));
        menu.setIsFullPage(Boolean.TRUE.equals(command.getIsFullPage()) ? 1 : 0);
    }

    private void validateMenuCommand(MenuSaveCommand command, String menuId) {
        String menuType = command.getMenuType() == null ? "" : command.getMenuType().trim().toLowerCase();
        if (!"menu".equals(menuType) && !MENU_TYPE_BUTTON.equals(menuType)) {
            throw new ApiException(BusinessErrorCode.MENU_TYPE_INVALID);
        }

        if (MENU_TYPE_BUTTON.equals(menuType)) {
            String parentId = normalizeParentId(command.getParentId());
            Menu parentMenu = getActiveMenu(parentId);
            if (MENU_TYPE_BUTTON.equalsIgnoreCase(parentMenu.getMenuType())) {
                throw new ApiException(BusinessErrorCode.MENU_BUTTON_PARENT_INVALID);
            }
        }

        if ("menu".equals(menuType) && !StringUtils.hasText(command.getPath())) {
            throw new ApiException(BusinessErrorCode.MENU_PATH_REQUIRED);
        }

        if (!StringUtils.hasText(command.getAuthMark())) {
            throw new ApiException(BusinessErrorCode.MENU_AUTH_MARK_REQUIRED);
        }

        ensureNameUnique(command.getName(), menuId);
    }

    private void ensureNameUnique(String name, String excludeMenuId) {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<Menu>()
                .eq(Menu::getDeleted, 0)
                .eq(Menu::getName, name == null ? null : name.trim());
        if (excludeMenuId != null) {
            wrapper.ne(Menu::getId, excludeMenuId);
        }
        if (menuMapper.selectCount(wrapper) > 0) {
            throw new ApiException(BusinessErrorCode.DATA_ALREADY_EXISTS);
        }
    }

    private void ensureAuthMarkUnique(String authMark, String excludeMenuId) {
        if (!StringUtils.hasText(authMark)) {
            return;
        }
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<Menu>()
                .eq(Menu::getDeleted, 0)
                .eq(Menu::getAuthMark, authMark.trim());
        if (excludeMenuId != null) {
            wrapper.ne(Menu::getId, excludeMenuId);
        }
        if (menuMapper.selectCount(wrapper) > 0) {
            throw new ApiException(BusinessErrorCode.DATA_ALREADY_EXISTS);
        }
    }

    private Menu getActiveMenu(String menuId) {
        Menu menu = menuMapper.selectOne(MenuQuerySupport.selectColumns(new LambdaQueryWrapper<Menu>())
                .eq(Menu::getId, menuId)
                .eq(Menu::getDeleted, 0)
                .last("limit 1"));
        if (menu == null) {
            throw new ApiException(BusinessErrorCode.DATA_NOT_FOUND);
        }
        return menu;
    }

    private Set<String> collectDescendantIds(Collection<String> rootIds) {
        Set<String> collected = new LinkedHashSet<>(rootIds);
        Set<String> frontier = new LinkedHashSet<>(rootIds);
        while (!frontier.isEmpty()) {
            List<Menu> children = menuMapper.selectList(MenuQuerySupport.selectColumns(new LambdaQueryWrapper<Menu>())
                    .eq(Menu::getDeleted, 0)
                    .in(Menu::getParentId, frontier));
            frontier = children.stream()
                    .map(Menu::getId)
                    .filter(Objects::nonNull)
                    .filter(id -> !collected.contains(id))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            collected.addAll(frontier);
        }
        return collected;
    }

    /**
     * 把入参菜单 ID 集合扩展为「自身 + 所有启用的祖先菜单」。
     * 用于让前端拿到的菜单树保持连通：仅授权了 UserCenter（其 parent 为 System）时，
     * System 节点必须出现在返回结果中，否则前端拼接出来的路径会变成 /user-center 而非 /system/user-center。
     */
    private Set<String> expandWithAncestors(Set<String> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return Set.of();
        }
        Set<String> expanded = new LinkedHashSet<>(menuIds);
        Set<String> frontier = new LinkedHashSet<>(menuIds);
        while (!frontier.isEmpty()) {
            List<Menu> menus = menuMapper.selectList(MenuQuerySupport.selectColumns(new LambdaQueryWrapper<Menu>())
                    .eq(Menu::getDeleted, 0)
                    .eq(Menu::getIsEnable, 1)
                    .in(Menu::getId, frontier));
            Set<String> nextFrontier = new LinkedHashSet<>();
            for (Menu menu : menus) {
                String parentId = menu.getParentId();
                if (parentId == null || expanded.contains(parentId)) {
                    continue;
                }
                expanded.add(parentId);
                nextFrontier.add(parentId);
            }
            frontier = nextFrontier;
        }
        return expanded;
    }

    private Map<String, List<String>> getMenuRoleCodes(Collection<String> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return Map.of();
        }

        Map<String, Set<String>> menuRoleIdMap = new LinkedHashMap<>();
        roleMenuPermissionMapper.selectList(new LambdaQueryWrapper<com.velox.module.system.domain.model.RoleMenuPermission>()
                        .eq(com.velox.module.system.domain.model.RoleMenuPermission::getDeleted, 0)
                        .in(com.velox.module.system.domain.model.RoleMenuPermission::getMenuId, menuIds))
                .forEach(item -> menuRoleIdMap
                        .computeIfAbsent(item.getMenuId(), key -> new LinkedHashSet<>())
                        .add(item.getRoleId()));

        Set<String> roleIds = menuRoleIdMap.values().stream()
                .flatMap(Set::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (roleIds.isEmpty()) {
            return Map.of();
        }

        Map<String, String> roleCodeMap = roleMapper.selectList(new LambdaQueryWrapper<Role>()
                        .eq(Role::getDeleted, 0)
                        .in(Role::getId, roleIds))
                .stream()
                .filter(role -> StringUtils.hasText(role.getRoleCode()))
                .collect(Collectors.toMap(Role::getId, Role::getRoleCode, (left, right) -> left, LinkedHashMap::new));

        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : menuRoleIdMap.entrySet()) {
            List<String> roleCodes = entry.getValue().stream()
                    .map(roleCodeMap::get)
                    .filter(Objects::nonNull)
                    .toList();
            if (!roleCodes.isEmpty()) {
                result.put(entry.getKey(), roleCodes);
            }
        }
        return result;
    }

    private String normalizeParentId(String parentId) {
        if (!StringUtils.hasText(parentId)) {
            throw new ApiException(BusinessErrorCode.MENU_PARENT_REQUIRED);
        }
        return parentId.trim();
    }

    private String normalizeMenuParentId(String parentId) {
        return StringUtils.hasText(parentId) ? parentId.trim() : null;
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private void bindMenuToSuperRole(String menuId) {
        if (!StringUtils.hasText(menuId)) {
            return;
        }
        Role superRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getDeleted, 0)
                .eq(Role::getRoleCode, SystemRoleCode.R_SUPER)
                .last("limit 1"));
        if (superRole == null || !StringUtils.hasText(superRole.getId())) {
            return;
        }
        LinkedHashSet<String> menuIds = new LinkedHashSet<>(permissionService.getRoleMenuIds(superRole.getId()));
        menuIds.add(menuId);
        permissionService.assignRoleMenu(superRole.getId(), menuIds);
    }

    private String currentOperator() {
        String loginId = securitySessionService.currentLoginIdOrNull();
        return StringUtils.hasText(loginId) ? loginId : "system";
    }
}
