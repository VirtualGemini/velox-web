package com.velox.module.system.persistence;

import com.velox.framework.persistence.api.mapper.BaseMapperExt;
import com.velox.module.system.domain.model.UserSecurity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserSecurityMapper extends BaseMapperExt<UserSecurity> {
}
