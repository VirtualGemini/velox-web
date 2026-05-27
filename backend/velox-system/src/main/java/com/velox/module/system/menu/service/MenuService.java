package com.velox.module.system.menu.service;

import com.velox.module.system.menu.dto.MenuSaveCommand;
import com.velox.module.system.menu.dto.MenuRouteDTO;

import java.util.List;

public interface MenuService {

    List<MenuRouteDTO> getSimpleMenus();

    String create(MenuSaveCommand command);

    Boolean update(String menuId, MenuSaveCommand command);

    Boolean delete(String menuId);
}
