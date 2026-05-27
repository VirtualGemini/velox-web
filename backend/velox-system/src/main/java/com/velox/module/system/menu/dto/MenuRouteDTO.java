package com.velox.module.system.menu.dto;

import java.util.ArrayList;
import java.util.List;

public class MenuRouteDTO {
    private String id;
    private String path;
    private String name;
    private String component;
    private String redirect;
    private MenuMetaDTO meta;
    private List<MenuRouteDTO> children = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public MenuMetaDTO getMeta() {
        return meta;
    }

    public void setMeta(MenuMetaDTO meta) {
        this.meta = meta;
    }

    public List<MenuRouteDTO> getChildren() {
        return children;
    }

    public void setChildren(List<MenuRouteDTO> children) {
        this.children = children;
    }
}
