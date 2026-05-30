package com.velox.module.system.account.service.impl;

import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.common.result.PageResult;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.module.system.account.dto.AccountDetailCardDTO;
import com.velox.module.system.domain.model.Profile;
import com.velox.module.system.domain.model.Role;
import com.velox.module.system.domain.model.Account;
import com.velox.module.system.domain.model.AccountRole;
import com.velox.module.system.domain.model.AccountSecurity;
import com.velox.module.system.persistence.ProfileMapper;
import com.velox.module.system.persistence.RoleMapper;
import com.velox.module.system.persistence.AccountMapper;
import com.velox.module.system.persistence.AccountRoleMapper;
import com.velox.module.system.persistence.AccountSecurityMapper;
import com.velox.module.system.auth.service.PasswordCipherService;
import com.velox.module.system.auth.status.ActiveUserStatusService;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.permission.service.PermissionService;
import com.velox.framework.web.RequestDateTimeFormatter;
import com.velox.module.system.account.dto.AccountListItemDTO;
import com.velox.module.system.account.dto.AccountQuery;
import com.velox.module.system.account.dto.AccountSaveCommand;
import com.velox.module.system.account.service.AccountManageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
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
public class AccountManageServiceImpl implements AccountManageService {

    private final AccountMapper userMapper;
    private final ProfileMapper profileMapper;
    private final RoleMapper roleMapper;
    private final AccountRoleMapper userRoleMapper;
    private final AccountSecurityMapper accountSecurityMapper;
    private final PasswordCipherService passwordCipherService;
    private final PermissionService permissionService;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final ActiveUserStatusService activeUserStatusService;
    private final SecuritySessionService securitySessionService;

    public AccountManageServiceImpl(AccountMapper userMapper,
                                 ProfileMapper profileMapper,
                                 RoleMapper roleMapper,
                                 AccountRoleMapper userRoleMapper,
                                 AccountSecurityMapper accountSecurityMapper,
                                 PasswordCipherService passwordCipherService,
                                 PermissionService permissionService,
                                 SystemEntityIdGenerator entityIdGenerator,
                                 ActiveUserStatusService activeUserStatusService,
                                 SecuritySessionService securitySessionService) {
        this.userMapper = userMapper;
        this.profileMapper = profileMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.accountSecurityMapper = accountSecurityMapper;
        this.passwordCipherService = passwordCipherService;
        this.permissionService = permissionService;
        this.entityIdGenerator = entityIdGenerator;
        this.activeUserStatusService = activeUserStatusService;
        this.securitySessionService = securitySessionService;
    }

    @Override
    public PageResult<AccountListItemDTO> list(AccountQuery query) {
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getDeleted, 0);
        if (StringUtils.hasText(query.getStatus())) {
            String normalizedStatus = query.getStatus().trim();
            if ("4".equals(normalizedStatus)) {
                wrapper.eq(Account::getStatus, 4);
            } else if ("3".equals(normalizedStatus)) {
                wrapper.eq(Account::getStatus, 3);
            }
        }
        if (StringUtils.hasText(query.getUsername())) {
            wrapper.like(Account::getUsername, query.getUsername().trim());
        }
        if (StringUtils.hasText(query.getRemark())) {
            wrapper.like(Account::getRemark, query.getRemark().trim());
        }
        if (StringUtils.hasText(query.getCreateTimeStart())) {
            wrapper.ge(Account::getCreateTime, RequestDateTimeFormatter.parseToUtc(query.getCreateTimeStart()));
        }
        if (StringUtils.hasText(query.getCreateTimeEnd())) {
            wrapper.le(Account::getCreateTime, RequestDateTimeFormatter.parseToUtc(query.getCreateTimeEnd()));
        }
        if (StringUtils.hasText(query.getUpdateTimeStart())) {
            wrapper.ge(Account::getUpdateTime, RequestDateTimeFormatter.parseToUtc(query.getUpdateTimeStart()));
        }
        if (StringUtils.hasText(query.getUpdateTimeEnd())) {
            wrapper.le(Account::getUpdateTime, RequestDateTimeFormatter.parseToUtc(query.getUpdateTimeEnd()));
        }
        if (StringUtils.hasText(query.getEmail())) {
            List<String> accountIds = accountSecurityMapper.selectList(new LambdaQueryWrapper<AccountSecurity>()
                            .eq(AccountSecurity::getDeleted, 0)
                            .like(AccountSecurity::getEmail, query.getEmail().trim().toLowerCase()))
                    .stream()
                    .map(AccountSecurity::getAccountId)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .toList();
            if (accountIds.isEmpty()) {
                return new PageResult<>(0, query.getCurrent(), query.getSize(), List.of());
            }
            wrapper.in(Account::getId, accountIds);
        }
        wrapper.orderByDesc(Account::getCreateTime)
                .orderByDesc(Account::getUpdateTime);

        String statusFilter = StringUtils.hasText(query.getStatus()) ? query.getStatus().trim() : null;
        boolean activeStatusFilter = ActiveUserStatusService.STATUS_ONLINE.equals(statusFilter)
                || ActiveUserStatusService.STATUS_OFFLINE.equals(statusFilter);

        List<Account> users;
        long total;
        if (activeStatusFilter) {
            users = userMapper.selectList(wrapper);
            total = users.size();
        } else {
            Page<Account> page = new Page<>(query.getCurrent(), query.getSize());
            Page<Account> result = userMapper.selectPage(page, wrapper);
            users = result.getRecords();
            total = result.getTotal();
        }

        List<String> accountIds = users.stream().map(Account::getId).toList();
        Map<String, Profile> profileMap = getActiveProfileMap(accountIds);
        Map<String, AccountSecurity> securityMap = getActiveSecurityMap(accountIds);
        Map<String, String> activeStatusMap = activeUserStatusService.resolveStatuses(accountIds);

        List<AccountListItemDTO> list = users.stream()
                .map(account -> toAccountListItem(
                        account,
                        profileMap.get(account.getId()),
                        securityMap.get(account.getId()),
                        resolveDisplayStatus(account, activeStatusMap)))
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
    public AccountDetailCardDTO getDetailCard(String accountId) {
        Account account = getActiveUser(accountId);
        Profile profile = getActiveProfileMap(List.of(accountId)).get(accountId);
        AccountSecurity security = getActiveSecurityMap(List.of(accountId)).get(accountId);
        String status = resolveDisplayStatus(
                account,
                activeUserStatusService.resolveStatuses(List.of(accountId))
        );
        List<String> roleCodes = permissionService.getAccountRoleCodes(accountId);

        AccountDetailCardDTO dto = new AccountDetailCardDTO();
        dto.setHeader(buildHeader(account, profile, status, roleCodes));
        dto.setProfile(buildProfileSection(profile));
        dto.setAccount(buildAccountSection(account, status, roleCodes));
        dto.setSecurity(buildSecuritySection(security));
        dto.setThirdPartyProviders(buildThirdPartyProviders());
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(AccountSaveCommand command) {
        validateCreateCommand(command);
        ensureUsernameUnique(command.getUsername(), null);

        List<Role> roles = resolveRoles(command.getRoleCodes());
        String operator = currentOperator();

        Account account = new Account();
        account.setId(entityIdGenerator.nextId(Account.class));
        account.setUsername(command.getUsername().trim());
        account.setPassword(passwordCipherService.encode(command.getPassword().trim()));
        account.setRemark(normalizeNullable(command.getRemark()));
        account.setStatus(1);
        account.setLoginFailCount(0);
        account.setDeleted(0);
        account.setCreateBy(operator);
        account.setUpdateBy(operator);
        userMapper.insert(account);

        ensureProfile(account, operator);
        assignUserRoles(account.getId(), roles);
        return account.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(String userId, AccountSaveCommand command) {
        Account user = getActiveUser(userId);
        ensureCanUpdateUser(user.getId());
        ensureUsernameUnique(command.getUsername(), userId);

        List<Role> roles = resolveRoles(command.getRoleCodes());
        String operator = currentOperator();

        user.setUsername(command.getUsername().trim());
        user.setRemark(normalizeNullable(command.getRemark()));
        user.setUpdateBy(operator);
        if (StringUtils.hasText(command.getPassword())) {
            user.setPassword(passwordCipherService.encode(command.getPassword().trim()));
        }
        userMapper.updateById(user);

        ensureProfile(user, operator);
        assignUserRoles(userId, roles);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(String userId) {
        Account user = getActiveUser(userId);
        ensureCanDeleteUser(user.getId());
        String operator = currentOperator();

        userMapper.update(null, new LambdaUpdateWrapper<Account>()
                .eq(Account::getId, userId)
                .eq(Account::getDeleted, 0)
                .set(Account::getDeleted, 1)
                .set(Account::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC))
                .set(Account::getUpdateBy, operator));

        profileMapper.update(null, new LambdaUpdateWrapper<Profile>()
                .eq(Profile::getAccountId, userId)
                .eq(Profile::getDeleted, 0)
                .set(Profile::getDeleted, 1)
                .set(Profile::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC))
                .set(Profile::getUpdateBy, operator));

        List<String> activeRelationIds = userRoleMapper.selectAllByAccountId(userId).stream()
                .filter(relation -> Integer.valueOf(0).equals(relation.getDeleted()))
                .map(AccountRole::getId)
                .filter(Objects::nonNull)
                .toList();
        if (!activeRelationIds.isEmpty()) {
            userRoleMapper.logicalDeleteByIds(activeRelationIds, operator);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteBatch(List<String> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return true;
        }
        accountIds.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .forEach(this::delete);
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
        int operatorLevel = permissionService.getAccountHighestRoleLevel(currentUserId);
        int targetLevel = permissionService.getAccountHighestRoleLevel(userId);
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
        int operatorLevel = permissionService.getAccountHighestRoleLevel(currentUserId);
        int targetLevel = permissionService.getAccountHighestRoleLevel(userId);
        if (operatorLevel <= targetLevel) {
            throw new ApiException(BusinessErrorCode.USER_DELETE_FORBIDDEN);
        }
    }

    private AccountListItemDTO toAccountListItem(
            Account user,
            Profile profile,
            AccountSecurity security,
            String status
    ) {
        AccountListItemDTO dto = new AccountListItemDTO();
        dto.setAccountId(user.getId());
        dto.setAvatar(resolveAvatar(profile, user.getUsername()));
        dto.setStatus(status);
        dto.setUsername(user.getUsername());
        dto.setEmail(security == null ? "" : defaultString(security.getEmail(), ""));
        dto.setRemark(defaultString(user.getRemark(), ""));
        dto.setCreateTime(RequestDateTimeFormatter.format(user.getCreateTime()));
        dto.setUpdateTime(RequestDateTimeFormatter.format(user.getUpdateTime()));
        return dto;
    }

    private AccountDetailCardDTO.Header buildHeader(
            Account account,
            Profile profile,
            String status,
            List<String> roleCodes
    ) {
        AccountDetailCardDTO.Header header = new AccountDetailCardDTO.Header();
        header.setAvatar(resolveAvatar(profile, account.getUsername()));
        header.setUsername(account.getUsername());
        header.setNickname(profile == null ? null : profile.getNickname());
        header.setRealName(profile == null ? null : profile.getRealName());
        header.setStatus(status);
        header.setRemark(defaultString(account.getRemark(), ""));
        header.setRoleCodes(roleCodes);
        header.setCreateTime(RequestDateTimeFormatter.format(account.getCreateTime()));
        header.setUpdateTime(RequestDateTimeFormatter.format(account.getUpdateTime()));
        return header;
    }

    private AccountDetailCardDTO.ProfileSection buildProfileSection(Profile profile) {
        AccountDetailCardDTO.ProfileSection section = new AccountDetailCardDTO.ProfileSection();
        if (profile == null) {
            section.setTags(List.of());
            return section;
        }
        section.setNickname(profile.getNickname());
        section.setGender(profile.getGender());
        section.setRealName(profile.getRealName());
        section.setDisplayEmail(profile.getEmail());
        section.setDisplayMobile(profile.getMobile());
        section.setAddress(profile.getAddress());
        section.setPosition(profile.getPosition());
        section.setCompany(profile.getCompany());
        section.setSignature(profile.getSignature());
        section.setIntroduction(profile.getIntroduction());
        section.setTags(parseTags(profile.getTags()));
        return section;
    }

    private AccountDetailCardDTO.AccountSection buildAccountSection(Account account, String status, List<String> roleCodes) {
        AccountDetailCardDTO.AccountSection section = new AccountDetailCardDTO.AccountSection();
        section.setAccountId(account.getId());
        section.setUsername(account.getUsername());
        section.setRemark(account.getRemark());
        section.setStatus(status);
        section.setPendingDeletion(account.getDeletionExpiresAt() != null);
        section.setDeletionRequestedAt(RequestDateTimeFormatter.format(account.getDeletionRequestedAt()));
        section.setDeletionExpiresAt(RequestDateTimeFormatter.format(account.getDeletionExpiresAt()));
        section.setLoginFailCount(account.getLoginFailCount());
        section.setLoginFailTime(RequestDateTimeFormatter.format(account.getLoginFailTime()));
        section.setRoleCodes(roleCodes);
        return section;
    }

    private AccountDetailCardDTO.SecuritySection buildSecuritySection(AccountSecurity security) {
        AccountDetailCardDTO.SecuritySection section = new AccountDetailCardDTO.SecuritySection();
        if (security == null) {
            section.setLoginMethods(List.of());
            section.setEmailMfaEnabled(false);
            section.setTotpMfaEnabled(false);
            return section;
        }
        section.setSecurityEmail(security.getEmail());
        section.setEmailMfaEnabled(Integer.valueOf(1).equals(security.getMfaEmailEnabled()));
        section.setTotpMfaEnabled(Integer.valueOf(1).equals(security.getMfaTotpEnabled()));
        section.setLoginMethods(parseLoginMethods(security.getLoginMethods()));
        section.setEmailVerifiedAt(RequestDateTimeFormatter.format(security.getEmailVerifiedAt()));
        section.setLastPasswordChangeAt(RequestDateTimeFormatter.format(security.getLastPasswordChangeAt()));
        return section;
    }

    private List<AccountDetailCardDTO.ThirdPartyProvider> buildThirdPartyProviders() {
        List<AccountDetailCardDTO.ThirdPartyProvider> providers = new ArrayList<>();
        providers.add(buildThirdPartyProvider("github", "GitHub"));
        providers.add(buildThirdPartyProvider("linuxdo", "LinuxDo"));
        return providers;
    }

    private AccountDetailCardDTO.ThirdPartyProvider buildThirdPartyProvider(String key, String name) {
        AccountDetailCardDTO.ThirdPartyProvider provider = new AccountDetailCardDTO.ThirdPartyProvider();
        provider.setKey(key);
        provider.setName(name);
        provider.setBound(false);
        return provider;
    }

    private String resolveDisplayStatus(Account user, Map<String, String> activeStatusMap) {
        Integer userStatus = user.getStatus();
        if (Integer.valueOf(4).equals(userStatus)) {
            return "4";
        }
        if (Integer.valueOf(3).equals(userStatus)) {
            return "3";
        }
        return activeStatusMap.getOrDefault(user.getId(), ActiveUserStatusService.STATUS_OFFLINE);
    }

    private void validateCreateCommand(AccountSaveCommand command) {
        if (!StringUtils.hasText(command.getPassword())) {
            throw new ApiException(BusinessErrorCode.USER_PASSWORD_REQUIRED_FOR_CREATE);
        }
    }

    private Account getActiveUser(String userId) {
        Account user = userMapper.selectOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getId, userId)
                .eq(Account::getDeleted, 0)
                .last("limit 1"));
        if (user == null) {
            throw new ApiException(BusinessErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private void ensureUsernameUnique(String username, String excludeUserId) {
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<Account>()
                .eq(Account::getDeleted, 0)
                .eq(Account::getUsername, username == null ? null : username.trim());
        if (excludeUserId != null) {
            wrapper.ne(Account::getId, excludeUserId);
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

        Map<String, AccountRole> activeRelationMap = new LinkedHashMap<>();
        Map<String, AccountRole> deletedRelationMap = new LinkedHashMap<>();
        for (AccountRole relation : userRoleMapper.selectAllByAccountId(userId)) {
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
                .map(AccountRole::getId)
                .filter(Objects::nonNull)
                .toList();
        if (!deleteIds.isEmpty()) {
            userRoleMapper.logicalDeleteByIds(deleteIds, operator);
        }

        List<String> restoreIds = requestedRoleIds.stream()
                .filter(roleId -> !activeRelationMap.containsKey(roleId))
                .map(deletedRelationMap::get)
                .filter(Objects::nonNull)
                .map(AccountRole::getId)
                .filter(Objects::nonNull)
                .toList();
        if (!restoreIds.isEmpty()) {
            userRoleMapper.restoreByIds(restoreIds, operator);
        }

        List<AccountRole> entities = new ArrayList<>(roles.size());
        for (Role role : roles) {
            String roleId = role.getId();
            if (roleId == null || activeRelationMap.containsKey(roleId) || deletedRelationMap.containsKey(roleId)) {
                continue;
            }
            AccountRole userRole = new AccountRole();
            userRole.setId(entityIdGenerator.nextId(AccountRole.class));
            userRole.setAccountId(userId);
            userRole.setRoleId(roleId);
            userRole.setDeleted(0);
            userRole.setCreateBy(operator);
            userRole.setUpdateBy(operator);
            entities.add(userRole);
        }

        for (AccountRole entity : entities) {
            userRoleMapper.insert(entity);
        }
    }

    private Profile getOrInitProfile(Account user, String operator) {
        Profile profile = profileMapper.selectOne(new LambdaQueryWrapper<Profile>()
                .eq(Profile::getAccountId, user.getId())
                .last("limit 1"));
        if (profile != null) {
            if (!StringUtils.hasText(profile.getAvatar())) {
                profile.setAvatar(buildDefaultAvatar(user.getUsername()));
            }
            return profile;
        }

        Profile created = new Profile();
        created.setId(entityIdGenerator.nextId(Profile.class));
        created.setAccountId(user.getId());
        created.setAvatar(buildDefaultAvatar(user.getUsername()));
        created.setDeleted(0);
        created.setCreateBy(operator);
        created.setUpdateBy(operator);
        return created;
    }

    private void ensureProfile(Account user, String operator) {
        Profile profile = getOrInitProfile(user, operator);
        if (!StringUtils.hasText(profile.getNickname())) {
            profile.setNickname(user.getUsername());
        }
        profile.setDeleted(0);
        profile.setUpdateBy(operator);

        if (profileMapper.selectById(profile.getId()) == null) {
            profileMapper.insert(profile);
            return;
        }
        profileMapper.updateById(profile);
    }

    private Map<String, Profile> getActiveProfileMap(Collection<String> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return Map.of();
        }
        return profileMapper.selectList(new LambdaQueryWrapper<Profile>()
                        .eq(Profile::getDeleted, 0)
                        .in(Profile::getAccountId, accountIds))
                .stream()
                .filter(profile -> StringUtils.hasText(profile.getAccountId()))
                .collect(Collectors.toMap(Profile::getAccountId, profile -> profile, (left, right) -> right, LinkedHashMap::new));
    }

    private Map<String, AccountSecurity> getActiveSecurityMap(Collection<String> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return Map.of();
        }
        return accountSecurityMapper.selectList(new LambdaQueryWrapper<AccountSecurity>()
                        .eq(AccountSecurity::getDeleted, 0)
                        .in(AccountSecurity::getAccountId, accountIds))
                .stream()
                .filter(security -> StringUtils.hasText(security.getAccountId()))
                .collect(Collectors.toMap(AccountSecurity::getAccountId, security -> security, (left, right) -> right, LinkedHashMap::new));
    }

    private AccountRole preferLatestRelation(AccountRole left, AccountRole right) {
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

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String defaultString(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private List<String> parseTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return List.of();
        }
        String normalized = tags.trim();
        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        if (!StringUtils.hasText(normalized)) {
            return List.of();
        }
        return Arrays.stream(normalized.split(","))
                .map(String::trim)
                .map(item -> item.replace("\"", ""))
                .filter(StringUtils::hasText)
                .toList();
    }

    private List<String> parseLoginMethods(String loginMethods) {
        if (!StringUtils.hasText(loginMethods)) {
            return List.of();
        }
        return Arrays.stream(loginMethods.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
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
