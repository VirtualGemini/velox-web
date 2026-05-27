package com.velox.module.system.user.service.impl;

import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.common.result.PageResult;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.module.system.domain.model.Profile;
import com.velox.module.system.domain.model.Role;
import com.velox.module.system.domain.model.User;
import com.velox.module.system.domain.model.UserRole;
import com.velox.module.system.persistence.ProfileMapper;
import com.velox.module.system.persistence.RoleMapper;
import com.velox.module.system.persistence.UserMapper;
import com.velox.module.system.persistence.UserRoleMapper;
import com.velox.module.system.auth.service.PasswordCipherService;
import com.velox.module.system.auth.status.ActiveUserStatusService;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.permission.service.PermissionService;
import com.velox.framework.web.RequestDateTimeFormatter;
import com.velox.module.system.user.dto.UserListItemDTO;
import com.velox.module.system.user.dto.UserQuery;
import com.velox.module.system.user.dto.UserSaveCommand;
import com.velox.module.system.user.service.UserManageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
public class UserManageServiceImpl implements UserManageService {

    private final UserMapper userMapper;
    private final ProfileMapper profileMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordCipherService passwordCipherService;
    private final PermissionService permissionService;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final ActiveUserStatusService activeUserStatusService;
    private final SecuritySessionService securitySessionService;

    public UserManageServiceImpl(UserMapper userMapper,
                                 ProfileMapper profileMapper,
                                 RoleMapper roleMapper,
                                 UserRoleMapper userRoleMapper,
                                 PasswordCipherService passwordCipherService,
                                 PermissionService permissionService,
                                 SystemEntityIdGenerator entityIdGenerator,
                                 ActiveUserStatusService activeUserStatusService,
                                 SecuritySessionService securitySessionService) {
        this.userMapper = userMapper;
        this.profileMapper = profileMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordCipherService = passwordCipherService;
        this.permissionService = permissionService;
        this.entityIdGenerator = entityIdGenerator;
        this.activeUserStatusService = activeUserStatusService;
        this.securitySessionService = securitySessionService;
    }

    @Override
    public PageResult<UserListItemDTO> list(UserQuery query) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);
        if (StringUtils.hasText(query.getStatus())) {
            String normalizedStatus = query.getStatus().trim();
            if ("4".equals(normalizedStatus)) {
                wrapper.eq(User::getStatus, 4);
            } else if ("3".equals(normalizedStatus)) {
                wrapper.eq(User::getStatus, 3);
            }
        }
        if (StringUtils.hasText(query.getUserName())) {
            wrapper.like(User::getUsername, query.getUserName().trim());
        }
        if (StringUtils.hasText(query.getUserPhone())) {
            wrapper.like(User::getPhone, query.getUserPhone().trim());
        }
        if (StringUtils.hasText(query.getUserEmail())) {
            wrapper.like(User::getEmail, query.getUserEmail().trim());
        }
        if (StringUtils.hasText(query.getCreateTimeStart())) {
            wrapper.ge(User::getCreateTime, RequestDateTimeFormatter.parseToUtc(query.getCreateTimeStart()));
        }
        if (StringUtils.hasText(query.getCreateTimeEnd())) {
            wrapper.le(User::getCreateTime, RequestDateTimeFormatter.parseToUtc(query.getCreateTimeEnd()));
        }
        if (StringUtils.hasText(query.getUpdateTimeStart())) {
            wrapper.ge(User::getUpdateTime, RequestDateTimeFormatter.parseToUtc(query.getUpdateTimeStart()));
        }
        if (StringUtils.hasText(query.getUpdateTimeEnd())) {
            wrapper.le(User::getUpdateTime, RequestDateTimeFormatter.parseToUtc(query.getUpdateTimeEnd()));
        }
        if (StringUtils.hasText(query.getUserGender())) {
            List<String> genderUserIds = profileMapper.selectList(new LambdaQueryWrapper<Profile>()
                            .eq(Profile::getDeleted, 0)
                            .eq(Profile::getGender, normalizeListGender(Integer.parseInt(query.getUserGender()))))
                    .stream()
                    .map(Profile::getUserId)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .toList();
            if (genderUserIds.isEmpty()) {
                return new PageResult<>(0, query.getCurrent(), query.getSize(), List.of());
            }
            wrapper.in(User::getId, genderUserIds);
        }
        wrapper.orderByDesc(User::getCreateTime)
                .orderByDesc(User::getUpdateTime);

        String statusFilter = StringUtils.hasText(query.getStatus()) ? query.getStatus().trim() : null;
        boolean activeStatusFilter = ActiveUserStatusService.STATUS_ONLINE.equals(statusFilter)
                || ActiveUserStatusService.STATUS_OFFLINE.equals(statusFilter);

        List<User> users;
        long total;
        if (activeStatusFilter) {
            users = userMapper.selectList(wrapper);
            total = users.size();
        } else {
            Page<User> page = new Page<>(query.getCurrent(), query.getSize());
            Page<User> result = userMapper.selectPage(page, wrapper);
            users = result.getRecords();
            total = result.getTotal();
        }

        List<String> userIds = users.stream().map(User::getId).toList();
        Map<String, Profile> profileMap = getActiveProfileMap(userIds);
        Map<String, List<String>> roleCodeMap = userIds.isEmpty() ? Map.of() : new LinkedHashMap<>(permissionService.getUserRoleCodes(userIds));
        Map<String, String> activeStatusMap = activeUserStatusService.resolveStatuses(userIds);

        List<UserListItemDTO> list = users.stream()
                .map(user -> toUserListItem(
                        user,
                        profileMap.get(user.getId()),
                        roleCodeMap.getOrDefault(user.getId(), List.of()),
                        resolveDisplayStatus(user, activeStatusMap)))
                .collect(Collectors.toList());

        if (activeStatusFilter) {
            list = list.stream()
                    .filter(item -> statusFilter.equals(item.getStatus()))
                    .collect(Collectors.toList());
            total = list.size();
            long current = Math.max(query.getCurrent(), 1L);
            long size = Math.max(query.getSize(), 1L);
            int fromIndex = (int) Math.min((current - 1) * size, list.size());
            int toIndex = (int) Math.min(fromIndex + size, list.size());
            list = list.subList(fromIndex, toIndex);
        }
        return new PageResult<>(total, query.getCurrent(), query.getSize(), list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(UserSaveCommand command) {
        validateCreateCommand(command);
        ensureUsernameUnique(command.getUsername(), null);

        List<Role> roles = resolveRoles(command.getRoleCodes());
        String operator = currentOperator();

        User user = new User();
        user.setId(entityIdGenerator.nextId(User.class));
        user.setUsername(command.getUsername().trim());
        user.setPassword(passwordCipherService.encode(command.getPassword().trim()));
        user.setEmail(normalizeNullable(command.getEmail()));
        user.setPhone(command.getPhone().trim());
        user.setStatus(1);
        user.setLoginFailCount(0);
        user.setDeleted(0);
        user.setCreateBy(operator);
        user.setUpdateBy(operator);
        userMapper.insert(user);

        upsertProfile(user, command.getNickname().trim(), normalizeGender(command.getGender()), operator);
        assignUserRoles(user.getId(), roles);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(String userId, UserSaveCommand command) {
        User user = getActiveUser(userId);
        ensureCanUpdateUser(user.getId());
        ensureUsernameUnique(command.getUsername(), userId);

        List<Role> roles = resolveRoles(command.getRoleCodes());
        String operator = currentOperator();

        user.setUsername(command.getUsername().trim());
        user.setEmail(normalizeNullable(command.getEmail()));
        user.setPhone(command.getPhone().trim());
        user.setUpdateBy(operator);
        if (StringUtils.hasText(command.getPassword())) {
            user.setPassword(passwordCipherService.encode(command.getPassword().trim()));
        }
        userMapper.updateById(user);

        upsertProfile(user, command.getNickname().trim(), normalizeGender(command.getGender()), operator);
        assignUserRoles(userId, roles);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(String userId) {
        User user = getActiveUser(userId);
        ensureCanDeleteUser(user.getId());
        String operator = currentOperator();

        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getDeleted, 0)
                .set(User::getDeleted, 1)
                .set(User::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC))
                .set(User::getUpdateBy, operator));

        profileMapper.update(null, new LambdaUpdateWrapper<Profile>()
                .eq(Profile::getUserId, userId)
                .eq(Profile::getDeleted, 0)
                .set(Profile::getDeleted, 1)
                .set(Profile::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC))
                .set(Profile::getUpdateBy, operator));

        List<String> activeRelationIds = userRoleMapper.selectAllByUserId(userId).stream()
                .filter(relation -> Integer.valueOf(0).equals(relation.getDeleted()))
                .map(UserRole::getId)
                .filter(Objects::nonNull)
                .toList();
        if (!activeRelationIds.isEmpty()) {
            userRoleMapper.logicalDeleteByIds(activeRelationIds, operator);
        }
        return true;
    }

    private void ensureCanUpdateUser(String userId) {
        String currentUserId = securitySessionService.currentLoginIdOrNull();
        if (!StringUtils.hasText(currentUserId)) {
            throw new ApiException(BusinessErrorCode.USER_UPDATE_FORBIDDEN);
        }
        if (currentUserId.equals(userId)) {
            return;
        }
        int operatorLevel = permissionService.getUserHighestRoleLevel(currentUserId);
        int targetLevel = permissionService.getUserHighestRoleLevel(userId);
        if (operatorLevel <= targetLevel) {
            throw new ApiException(BusinessErrorCode.USER_UPDATE_FORBIDDEN);
        }
    }

    private void ensureCanDeleteUser(String userId) {
        String currentUserId = securitySessionService.currentLoginIdOrNull();
        if (!StringUtils.hasText(currentUserId)) {
            throw new ApiException(BusinessErrorCode.USER_DELETE_FORBIDDEN);
        }
        if (currentUserId.equals(userId)) {
            throw new ApiException(BusinessErrorCode.USER_OPERATE_SELF_FORBIDDEN);
        }
        int operatorLevel = permissionService.getUserHighestRoleLevel(currentUserId);
        int targetLevel = permissionService.getUserHighestRoleLevel(userId);
        if (operatorLevel <= targetLevel) {
            throw new ApiException(BusinessErrorCode.USER_DELETE_FORBIDDEN);
        }
    }

    private UserListItemDTO toUserListItem(User user, Profile profile, List<String> roleCodes, String status) {
        UserListItemDTO dto = new UserListItemDTO();
        dto.setId(user.getId());
        dto.setUserId(user.getId());
        dto.setAvatar(resolveAvatar(profile, user.getUsername()));
        dto.setStatus(status);
        dto.setUserName(user.getUsername());
        dto.setUserGender(resolveGenderLabel(profile == null ? null : profile.getGender()));
        dto.setNickName(profile == null ? user.getUsername() : defaultString(profile.getNickname(), user.getUsername()));
        dto.setUserPhone(user.getPhone());
        dto.setUserEmail(user.getEmail());
        dto.setUserRoles(roleCodes);
        dto.setCreateBy(user.getCreateBy());
        dto.setCreateTime(RequestDateTimeFormatter.format(user.getCreateTime()));
        dto.setUpdateBy(user.getUpdateBy());
        dto.setUpdateTime(RequestDateTimeFormatter.format(user.getUpdateTime()));
        return dto;
    }

    private String resolveDisplayStatus(User user, Map<String, String> activeStatusMap) {
        Integer userStatus = user.getStatus();
        if (Integer.valueOf(4).equals(userStatus)) {
            return "4";
        }
        if (Integer.valueOf(3).equals(userStatus)) {
            return "3";
        }
        return activeStatusMap.getOrDefault(user.getId(), ActiveUserStatusService.STATUS_OFFLINE);
    }

    private String resolveGenderLabel(Integer gender) {
        return switch (normalizeListGender(gender)) {
            case 2 -> "女";
            case 3 -> "其它";
            case 0 -> "未知";
            default -> "男";
        };
    }

    private void validateCreateCommand(UserSaveCommand command) {
        if (!StringUtils.hasText(command.getPassword())) {
            throw new ApiException(BusinessErrorCode.USER_PASSWORD_REQUIRED_FOR_CREATE);
        }
    }

    private User getActiveUser(String userId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getDeleted, 0)
                .last("limit 1"));
        if (user == null) {
            throw new ApiException(BusinessErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private void ensureUsernameUnique(String username, String excludeUserId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
                .eq(User::getUsername, username == null ? null : username.trim());
        if (excludeUserId != null) {
            wrapper.ne(User::getId, excludeUserId);
        }
        if (userMapper.selectCount(wrapper) > 0) {
            throw new ApiException(BusinessErrorCode.USER_ALREADY_EXISTS);
        }
    }

    private List<Role> resolveRoles(Collection<String> roleCodes) {
        Set<String> distinctCodes = roleCodes == null ? Set.of() : roleCodes.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (distinctCodes.isEmpty()) {
            throw new ApiException(BusinessErrorCode.ROLE_REQUIRED);
        }

        List<Role> roles = roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .eq(Role::getDeleted, 0)
                .in(Role::getRoleCode, distinctCodes));
        if (roles.size() != distinctCodes.size()) {
            throw new ApiException(BusinessErrorCode.ROLE_INVALID);
        }
        return roles;
    }

    private void assignUserRoles(String userId, List<Role> roles) {
        Set<String> requestedRoleIds = roles.stream()
                .map(Role::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        String operator = currentOperator();

        Map<String, UserRole> activeRelationMap = new LinkedHashMap<>();
        Map<String, UserRole> deletedRelationMap = new LinkedHashMap<>();
        for (UserRole relation : userRoleMapper.selectAllByUserId(userId)) {
            String roleId = relation.getRoleId();
            if (roleId == null) {
                continue;
            }
            if (Integer.valueOf(1).equals(relation.getDeleted())) {
                deletedRelationMap.merge(roleId, relation, this::preferLatestRelation);
                continue;
            }
            activeRelationMap.merge(roleId, relation, this::preferLatestRelation);
        }

        List<String> deleteIds = activeRelationMap.entrySet().stream()
                .filter(entry -> !requestedRoleIds.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .map(UserRole::getId)
                .filter(Objects::nonNull)
                .toList();
        if (!deleteIds.isEmpty()) {
            userRoleMapper.logicalDeleteByIds(deleteIds, operator);
        }

        List<String> restoreIds = requestedRoleIds.stream()
                .filter(roleId -> !activeRelationMap.containsKey(roleId))
                .map(deletedRelationMap::get)
                .filter(Objects::nonNull)
                .map(UserRole::getId)
                .filter(Objects::nonNull)
                .toList();
        if (!restoreIds.isEmpty()) {
            userRoleMapper.restoreByIds(restoreIds, operator);
        }

        List<UserRole> entities = new ArrayList<>(roles.size());
        for (Role role : roles) {
            String roleId = role.getId();
            if (roleId == null || activeRelationMap.containsKey(roleId) || deletedRelationMap.containsKey(roleId)) {
                continue;
            }
            UserRole userRole = new UserRole();
            userRole.setId(entityIdGenerator.nextId(UserRole.class));
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setDeleted(0);
            userRole.setCreateBy(operator);
            userRole.setUpdateBy(operator);
            entities.add(userRole);
        }

        for (UserRole entity : entities) {
            userRoleMapper.insert(entity);
        }
    }

    private Profile getOrInitProfile(User user, String operator) {
        Profile profile = profileMapper.selectOne(new LambdaQueryWrapper<Profile>()
                .eq(Profile::getUserId, user.getId())
                .last("limit 1"));
        if (profile != null) {
            if (!StringUtils.hasText(profile.getAvatar())) {
                profile.setAvatar(buildDefaultAvatar(user.getUsername()));
            }
            return profile;
        }

        Profile created = new Profile();
        created.setId(entityIdGenerator.nextId(Profile.class));
        created.setUserId(user.getId());
        created.setAvatar(buildDefaultAvatar(user.getUsername()));
        created.setDeleted(0);
        created.setCreateBy(operator);
        created.setUpdateBy(operator);
        return created;
    }

    private void upsertProfile(User user, String nickname, Integer gender, String operator) {
        Profile profile = getOrInitProfile(user, operator);
        profile.setNickname(nickname);
        profile.setGender(gender);
        profile.setDeleted(0);
        profile.setUpdateBy(operator);
        if (!StringUtils.hasText(profile.getRealName())) {
            profile.setRealName(nickname);
        }

        if (profileMapper.selectById(profile.getId()) == null) {
            profileMapper.insert(profile);
            return;
        }
        profileMapper.updateById(profile);
    }

    private Map<String, Profile> getActiveProfileMap(Collection<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return profileMapper.selectList(new LambdaQueryWrapper<Profile>()
                        .eq(Profile::getDeleted, 0)
                        .in(Profile::getUserId, userIds))
                .stream()
                .filter(profile -> StringUtils.hasText(profile.getUserId()))
                .collect(Collectors.toMap(Profile::getUserId, profile -> profile, (left, right) -> right, LinkedHashMap::new));
    }

    private UserRole preferLatestRelation(UserRole left, UserRole right) {
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

    private Integer normalizeGender(Integer gender) {
        return switch (gender == null ? 1 : gender) {
            case 2, 3 -> gender;
            default -> 1;
        };
    }

    private Integer normalizeListGender(Integer gender) {
        if (gender == null) {
            return 0;
        }
        return switch (gender) {
            case 1, 2, 3 -> gender;
            default -> 0;
        };
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String defaultString(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String resolveAvatar(Profile profile, String username) {
        if (profile != null && StringUtils.hasText(profile.getAvatar())) {
            String avatar = profile.getAvatar();
            // 如果是完整 URL（http/https 开头）或已带路径前缀，直接返回
            if (avatar.startsWith("http://") || avatar.startsWith("https://") || avatar.startsWith("/")) {
                return avatar;
            }
            // 否则视为 DB 存储的文件 ID（如 F0000001），补全为可访问的 URL
            return "/api/file/db/" + avatar;
        }
        return buildDefaultAvatar(username);
    }

    private String buildDefaultAvatar(String username) {
        String seed = StringUtils.hasText(username) ? username.trim() : "user";
        return "https://api.dicebear.com/7.x/avataaars/svg?seed=" + seed;
    }

    private String currentOperator() {
        String loginId = securitySessionService.currentLoginIdOrNull();
        return StringUtils.hasText(loginId) ? loginId : "system";
    }
}
