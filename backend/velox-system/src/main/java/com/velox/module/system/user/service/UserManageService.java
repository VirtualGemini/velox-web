package com.velox.module.system.user.service;

import com.velox.common.result.PageResult;
import com.velox.module.system.user.dto.UserListItemDTO;
import com.velox.module.system.user.dto.UserQuery;
import com.velox.module.system.user.dto.UserSaveCommand;

public interface UserManageService {

    PageResult<UserListItemDTO> list(UserQuery query);

    String create(UserSaveCommand command);

    Boolean update(String userId, UserSaveCommand command);

    Boolean delete(String userId);
}
