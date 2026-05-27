package com.velox.module.system.user.service.impl;

import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.module.system.domain.model.CurrentUserInfo;
import com.velox.module.system.domain.model.Menu;
import com.velox.module.system.domain.model.Profile;
import com.velox.module.system.domain.model.User;
import com.velox.module.system.persistence.MenuMapper;
import com.velox.module.system.persistence.ProfileMapper;
import com.velox.module.system.persistence.UserMapper;
import com.velox.module.system.persistence.support.MenuQuerySupport;
import com.velox.module.system.auth.service.PasswordCipherService;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.permission.service.PermissionService;
import com.velox.module.system.user.dto.UserPasswordUpdateCommand;
import com.velox.module.system.user.dto.UserInfoBasicDTO;
import com.velox.module.system.user.dto.UserInfoDTO;
import com.velox.module.system.user.dto.UserProfileUpdateCommand;
import com.velox.module.system.user.service.UserInfoService;
import com.velox.module.system.user.store.UserLanguageStore;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    private final UserMapper userMapper;
    private final ProfileMapper profileMapper;
    private final PermissionService permissionService;
    private final MenuMapper menuMapper;
    private final PasswordCipherService passwordCipherService;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final ObjectMapper objectMapper;
    private final SecuritySessionService securitySessionService;
    private final UserLanguageStore userLanguageStore;

    public UserInfoServiceImpl(
            UserMapper userMapper,
            ProfileMapper profileMapper,
            PermissionService permissionService,
            MenuMapper menuMapper,
            PasswordCipherService passwordCipherService,
            SystemEntityIdGenerator entityIdGenerator,
            ObjectMapper objectMapper,
            SecuritySessionService securitySessionService,
            UserLanguageStore userLanguageStore
    ) {
        this.userMapper = userMapper;
        this.profileMapper = profileMapper;
        this.permissionService = permissionService;
        this.menuMapper = menuMapper;
        this.passwordCipherService = passwordCipherService;
        this.entityIdGenerator = entityIdGenerator;
        this.objectMapper = objectMapper;
        this.securitySessionService = securitySessionService;
        this.userLanguageStore = userLanguageStore;
    }

    @Override
    public CurrentUserInfo getCurrentUserInfo() {
        String userId = securitySessionService.requireCurrentLoginId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ApiException(BusinessErrorCode.USER_NOT_FOUND);
        }
        Profile profile = getActiveProfile(userId);

        CurrentUserInfo currentUserInfo = new CurrentUserInfo();
        currentUserInfo.setUserId(normalizeNullable(user.getId()));
        currentUserInfo.setUserName(user.getUsername());
        currentUserInfo.setEmail(user.getEmail() == null ? "" : user.getEmail());
        currentUserInfo.setPhone(user.getPhone() == null ? "" : user.getPhone());
        currentUserInfo.setAvatar(resolveAvatar(profile, user.getUsername()));
        currentUserInfo.setNickname(profile == null ? "" : defaultString(profile.getNickname()));
        currentUserInfo.setGender(profile == null ? 0 : normalizeGender(profile.getGender()));
        currentUserInfo.setRealName(profile == null ? "" : defaultString(profile.getRealName()));
        currentUserInfo.setAddress(profile == null ? "" : defaultString(profile.getAddress()));
        currentUserInfo.setIntroduction(profile == null ? "" : defaultString(profile.getIntroduction()));
        currentUserInfo.setSignature(profile == null ? "" : defaultString(profile.getSignature()));
        currentUserInfo.setPosition(profile == null ? "" : defaultString(profile.getPosition()));
        currentUserInfo.setCompany(profile == null ? "" : defaultString(profile.getCompany()));
        currentUserInfo.setTags(parseTags(profile == null ? null : profile.getTags()));
        currentUserInfo.setRoles(permissionService.getUserRoleCodes(userId));
        currentUserInfo.setButtons(getCurrentButtons(userId));
        return currentUserInfo;
    }

    @Override
    public UserInfoBasicDTO getUserInfoBasicDTO() {
        String userId = securitySessionService.requireCurrentLoginId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ApiException(BusinessErrorCode.USER_NOT_FOUND);
        }
        Profile profile = getActiveProfile(userId);

        UserInfoBasicDTO dto = new UserInfoBasicDTO();
        dto.setUserId(normalizeNullable(user.getId()));
        dto.setUserName(user.getUsername());
        dto.setEmail(user.getEmail() == null ? "" : user.getEmail());
        dto.setPhone(user.getPhone() == null ? "" : user.getPhone());
        dto.setAvatar(resolveAvatar(profile, user.getUsername()));
        dto.setRoles(permissionService.getUserRoleCodes(userId));
        dto.setButtons(getCurrentButtons(userId));
        userLanguageStore.find(userId).ifPresent(dto::setLanguage);
        return dto;
    }

    @Override
    public UserInfoDTO getUserInfoDTO() {
        CurrentUserInfo currentUserInfo = getCurrentUserInfo();
        UserInfoDTO dto = new UserInfoDTO();
        dto.setUserId(currentUserInfo.getUserId());
        dto.setUserName(currentUserInfo.getUserName());
        dto.setEmail(currentUserInfo.getEmail());
        dto.setPhone(currentUserInfo.getPhone());
        dto.setAvatar(currentUserInfo.getAvatar());
        dto.setNickname(currentUserInfo.getNickname());
        dto.setGender(currentUserInfo.getGender());
        dto.setRealName(currentUserInfo.getRealName());
        dto.setAddress(currentUserInfo.getAddress());
        dto.setIntroduction(currentUserInfo.getIntroduction());
        dto.setSignature(currentUserInfo.getSignature());
        dto.setPosition(currentUserInfo.getPosition());
        dto.setCompany(currentUserInfo.getCompany());
        dto.setTags(currentUserInfo.getTags());
        dto.setRoles(currentUserInfo.getRoles());
        dto.setButtons(currentUserInfo.getButtons());
        userLanguageStore.find(currentUserInfo.getUserId()).ifPresent(dto::setLanguage);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCurrentUserProfile(UserProfileUpdateCommand command) {
        String userId = securitySessionService.requireCurrentLoginId();
        User user = requireCurrentUser(userId);
        Profile profile = getOrInitProfile(user, currentOperator());

        user.setEmail(normalizeNullable(command.getEmail()));
        user.setPhone(command.getPhone().trim());
        user.setUpdateBy(currentOperator());
        userMapper.updateById(user);

        profile.setNickname(command.getNickname().trim());
        profile.setRealName(command.getRealName().trim());
        profile.setAddress(normalizeNullable(command.getAddress()));
        profile.setGender(normalizeGender(command.getGender()));
        profile.setIntroduction(normalizeNullable(command.getIntroduction()));
        profile.setSignature(normalizeNullable(command.getSignature()));
        profile.setPosition(normalizeNullable(command.getPosition()));
        profile.setCompany(normalizeNullable(command.getCompany()));
        profile.setTags(serializeTags(command.getTags()));
        profile.setUpdateBy(currentOperator());
        saveProfile(profile);
        return true;
    }

    private String serializeTags(List<String> tags) {
        if (tags == null) {
            return null;
        }
        List<String> normalized = tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .limit(10)
                .toList();
        if (normalized.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (Exception ex) {
            throw new ApiException(BusinessErrorCode.OPERATION_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCurrentUserPassword(UserPasswordUpdateCommand command) {
        if (!Objects.equals(command.getNewPassword(), command.getConfirmPassword())) {
            throw new ApiException(BusinessErrorCode.PASSWORD_MISMATCH);
        }

        String userId = securitySessionService.requireCurrentLoginId();
        User user = requireCurrentUser(userId);
        if (!passwordCipherService.matches(command.getCurrentPassword(), user.getPassword())) {
            throw new ApiException(BusinessErrorCode.PASSWORD_ERROR);
        }
        if (Objects.equals(command.getCurrentPassword(), command.getNewPassword())) {
            throw new ApiException(BusinessErrorCode.PASSWORD_SAME_AS_OLD);
        }

        user.setPassword(passwordCipherService.encode(command.getNewPassword().trim()));
        user.setUpdateBy(currentOperator());
        userMapper.updateById(user);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentUserAvatar(String avatarUrl) {
        String userId = securitySessionService.requireCurrentLoginId();
        User user = requireCurrentUser(userId);
        Profile profile = getOrInitProfile(user, currentOperator());
        profile.setAvatar(avatarUrl);
        profile.setUpdateBy(currentOperator());
        saveProfile(profile);
    }

    @Override
    public Boolean updateCurrentUserLanguage(String language) {
        String userId = securitySessionService.requireCurrentLoginId();
        userLanguageStore.save(userId, language);
        return true;
    }

    private List<String> getCurrentButtons(String userId) {
        Set<String> roleIds = permissionService.getUserRoleIds(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }

        Set<String> menuIds = permissionService.getRoleMenuIds(roleIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (menuIds.isEmpty()) {
            return List.of();
        }

        // 同时返回菜单级与按钮级 auth_mark，便于前端做权限判断和路由优先级。
        return menuMapper.selectList(MenuQuerySupport.selectColumns(new LambdaQueryWrapper<Menu>())
                        .eq(Menu::getDeleted, 0)
                        .in(Menu::getId, menuIds))
                .stream()
                .map(Menu::getAuthMark)
                .filter(mark -> mark != null && !mark.isBlank())
                .distinct()
                .toList();
    }

    private User requireCurrentUser(String userId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getDeleted, 0)
                .last("limit 1"));
        if (user == null) {
            throw new ApiException(BusinessErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private Profile getActiveProfile(String userId) {
        return profileMapper.selectOne(new LambdaQueryWrapper<Profile>()
                .eq(Profile::getUserId, userId)
                .eq(Profile::getDeleted, 0)
                .last("limit 1"));
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
        created.setNickname(user.getUsername());
        created.setAvatar(buildDefaultAvatar(user.getUsername()));
        created.setGender(0);
        created.setDeleted(0);
        created.setCreateBy(operator);
        created.setUpdateBy(operator);
        return created;
    }

    private void saveProfile(Profile profile) {
        profile.setDeleted(0);
        if (profileMapper.selectById(profile.getId()) == null) {
            profileMapper.insert(profile);
            return;
        }
        profileMapper.updateById(profile);
    }

    private String resolveAvatar(Profile profile, String username) {
        if (profile != null && StringUtils.hasText(profile.getAvatar())) {
            String avatar = profile.getAvatar();
            // 如果是完整 URL（http/https 开头）或已带路径前缀，直接返回
            if (avatar.startsWith("http://") || avatar.startsWith("https://") || avatar.startsWith("/")) {
                return avatar;
            }
            // 否则视为 DB 存储的文件 ID（如 F0000001），补全为可访问的 URL
            // 注意：如果文件不存在会返回 404，但这比返回无效 URL 更好
            return "/api/file/db/" + avatar;
        }
        return buildDefaultAvatar(username);
    }

    private String buildDefaultAvatar(String username) {
        String seed = StringUtils.hasText(username) ? username.trim() : "user";
        return "https://api.dicebear.com/7.x/avataaars/svg?seed=" + seed;
    }

    private List<String> parseTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return List.of();
        }
        try {
            List<String> values = objectMapper.readValue(tags, new TypeReference<List<String>>() {
            });
            return values == null ? List.of() : values;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private Integer normalizeGender(Integer gender) {
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

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String currentOperator() {
        String loginId = securitySessionService.currentLoginIdOrNull();
        return StringUtils.hasText(loginId) ? loginId : "system";
    }
}
