package com.velox.module.system.persistence.support;

import com.velox.module.system.domain.model.Menu;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

public final class MenuQuerySupport {

    private MenuQuerySupport() {
    }

    public static LambdaQueryWrapper<Menu> selectColumns(LambdaQueryWrapper<Menu> wrapper) {
        return wrapper.select(
                Menu::getId,
                Menu::getParentId,
                Menu::getMenuType,
                Menu::getName,
                Menu::getTitle,
                Menu::getPath,
                Menu::getComponent,
                Menu::getRedirect,
                Menu::getIcon,
                Menu::getAuthMark,
                Menu::getIsEnable,
                Menu::getSort,
                Menu::getKeepAlive,
                Menu::getIsHide,
                Menu::getIsHideTab,
                Menu::getLink,
                Menu::getIsIframe,
                Menu::getShowBadge,
                Menu::getShowTextBadge,
                Menu::getFixedTab,
                Menu::getActivePath,
                Menu::getIsFullPage,
                Menu::getCreateTime,
                Menu::getUpdateTime,
                Menu::getCreateBy,
                Menu::getUpdateBy,
                Menu::getDeleted
        );
    }
}
