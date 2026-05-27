package com.velox.module.system.domain.model;

import com.velox.domain.model.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_menu")
public class Menu extends BaseEntity {

    private String parentId;
    private String menuType;
    private String name;
    private String title;
    private String path;
    private String component;
    private String redirect;
    private String icon;
    private String authMark;
    private Integer isEnable;
    private Integer sort;
    private Integer keepAlive;
    private Integer isHide;
    private Integer isHideTab;
    private String link;
    private Integer isIframe;
    private Integer showBadge;
    private String showTextBadge;
    private Integer fixedTab;
    private String activePath;
    private Integer isFullPage;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = normalizeIdentifier(parentId);
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

    public Integer getIsEnable() {
        return isEnable;
    }

    public void setIsEnable(Integer isEnable) {
        this.isEnable = isEnable;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Integer keepAlive) {
        this.keepAlive = keepAlive;
    }

    public Integer getIsHide() {
        return isHide;
    }

    public void setIsHide(Integer isHide) {
        this.isHide = isHide;
    }

    public Integer getIsHideTab() {
        return isHideTab;
    }

    public void setIsHideTab(Integer isHideTab) {
        this.isHideTab = isHideTab;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Integer getIsIframe() {
        return isIframe;
    }

    public void setIsIframe(Integer isIframe) {
        this.isIframe = isIframe;
    }

    public Integer getShowBadge() {
        return showBadge;
    }

    public void setShowBadge(Integer showBadge) {
        this.showBadge = showBadge;
    }

    public String getShowTextBadge() {
        return showTextBadge;
    }

    public void setShowTextBadge(String showTextBadge) {
        this.showTextBadge = showTextBadge;
    }

    public Integer getFixedTab() {
        return fixedTab;
    }

    public void setFixedTab(Integer fixedTab) {
        this.fixedTab = fixedTab;
    }

    public String getActivePath() {
        return activePath;
    }

    public void setActivePath(String activePath) {
        this.activePath = activePath;
    }

    public Integer getIsFullPage() {
        return isFullPage;
    }

    public void setIsFullPage(Integer isFullPage) {
        this.isFullPage = isFullPage;
    }
}
