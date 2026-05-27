package com.velox.module.system.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MenuSaveCommand {

    private String parentId;

    @NotBlank(message = "{validation.system.menu.save.menu_type.not_blank}")
    private String menuType;

    @NotBlank(message = "{validation.system.menu.save.name.not_blank}")
    @Size(max = 100, message = "{validation.system.menu.save.name.size}")
    private String name;

    @NotBlank(message = "{validation.system.menu.save.title.not_blank}")
    @Size(max = 100, message = "{validation.system.menu.save.title.size}")
    private String title;

    @Size(max = 255, message = "{validation.system.menu.save.path.size}")
    private String path;

    @Size(max = 255, message = "{validation.system.menu.save.component.size}")
    private String component;

    @Size(max = 255, message = "{validation.system.menu.save.redirect.size}")
    private String redirect;

    @Size(max = 100, message = "{validation.system.menu.save.icon.size}")
    private String icon;

    @Size(max = 100, message = "{validation.system.menu.save.auth_mark.size}")
    private String authMark;

    @NotNull(message = "{validation.system.menu.save.is_enable.not_null}")
    private Boolean isEnable;

    @NotNull(message = "{validation.system.menu.save.sort.not_null}")
    private Integer sort;

    private Boolean keepAlive;
    private Boolean isHide;
    private Boolean isHideTab;

    @Size(max = 255, message = "{validation.system.menu.save.link.size}")
    private String link;

    private Boolean isIframe;
    private Boolean showBadge;

    @Size(max = 50, message = "{validation.system.menu.save.show_text_badge.size}")
    private String showTextBadge;

    private Boolean fixedTab;

    @Size(max = 255, message = "{validation.system.menu.save.active_path.size}")
    private String activePath;

    private Boolean isFullPage;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAuthMark() {
        return authMark;
    }

    public void setAuthMark(String authMark) {
        this.authMark = authMark;
    }

    public Boolean getIsEnable() {
        return isEnable;
    }

    public void setIsEnable(Boolean isEnable) {
        this.isEnable = isEnable;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Boolean getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public Boolean getIsHide() {
        return isHide;
    }

    public void setIsHide(Boolean isHide) {
        this.isHide = isHide;
    }

    public Boolean getIsHideTab() {
        return isHideTab;
    }

    public void setIsHideTab(Boolean isHideTab) {
        this.isHideTab = isHideTab;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Boolean getIsIframe() {
        return isIframe;
    }

    public void setIsIframe(Boolean isIframe) {
        this.isIframe = isIframe;
    }

    public Boolean getShowBadge() {
        return showBadge;
    }

    public void setShowBadge(Boolean showBadge) {
        this.showBadge = showBadge;
    }

    public String getShowTextBadge() {
        return showTextBadge;
    }

    public void setShowTextBadge(String showTextBadge) {
        this.showTextBadge = showTextBadge;
    }

    public Boolean getFixedTab() {
        return fixedTab;
    }

    public void setFixedTab(Boolean fixedTab) {
        this.fixedTab = fixedTab;
    }

    public String getActivePath() {
        return activePath;
    }

    public void setActivePath(String activePath) {
        this.activePath = activePath;
    }

    public Boolean getIsFullPage() {
        return isFullPage;
    }

    public void setIsFullPage(Boolean isFullPage) {
        this.isFullPage = isFullPage;
    }
}
