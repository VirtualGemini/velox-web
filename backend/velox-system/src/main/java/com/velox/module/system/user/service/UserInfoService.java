package com.velox.module.system.user.service;

import com.velox.module.system.domain.model.CurrentUserInfo;
import com.velox.module.system.user.dto.UserInfoBasicDTO;
import com.velox.module.system.user.dto.UserInfoDTO;
import com.velox.module.system.user.dto.UserPasswordUpdateCommand;
import com.velox.module.system.user.dto.UserProfileUpdateCommand;

public interface UserInfoService {

    CurrentUserInfo getCurrentUserInfo();

    UserInfoBasicDTO getUserInfoBasicDTO();

    UserInfoDTO getUserInfoDTO();

    Boolean updateCurrentUserProfile(UserProfileUpdateCommand command);

    Boolean updateCurrentUserPassword(UserPasswordUpdateCommand command);

    void updateCurrentUserAvatar(String avatarUrl);

    Boolean updateCurrentUserLanguage(String language);
}
