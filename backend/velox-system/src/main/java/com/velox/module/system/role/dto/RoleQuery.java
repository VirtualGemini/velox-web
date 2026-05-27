package com.velox.module.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "openapi.system.role.query.schema")
public class RoleQuery {

    @Schema(description = "openapi.common.page.current", example = "1")
    private Long current = 1L;

    @Schema(description = "openapi.common.page.size", example = "20")
    private Long size = 20L;

    @Schema(description = "openapi.system.role.query.role_name")
    private String roleName;

    @Schema(description = "openapi.system.role.query.role_code")
    private String roleCode;

    @Schema(description = "openapi.system.role.query.description")
    private String description;

    @Schema(description = "openapi.system.role.query.type")
    private Integer type;

    @Schema(description = "openapi.system.role.query.enabled")
    private Boolean enabled;

    @Schema(description = "openapi.system.role.query.start_time", example = "2025-05-01")
    private String startTime;

    @Schema(description = "openapi.system.role.query.end_time", example = "2025-05-31")
    private String endTime;

    @Schema(description = "openapi.common.audit.create_time_start", example = "2026-05-22 10:00:00")
    private String createTimeStart;

    @Schema(description = "openapi.common.audit.create_time_end", example = "2026-05-22 18:00:00")
    private String createTimeEnd;

    @Schema(description = "openapi.common.audit.update_time_start", example = "2026-05-22 10:00:00")
    private String updateTimeStart;

    @Schema(description = "openapi.common.audit.update_time_end", example = "2026-05-22 18:00:00")
    private String updateTimeEnd;

    public Long getCurrent() {
        return current;
    }

    public void setCurrent(Long current) {
        this.current = current;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getCreateTimeStart() {
        return createTimeStart;
    }

    public void setCreateTimeStart(String createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public String getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setCreateTimeEnd(String createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public String getUpdateTimeStart() {
        return updateTimeStart;
    }

    public void setUpdateTimeStart(String updateTimeStart) {
        this.updateTimeStart = updateTimeStart;
    }

    public String getUpdateTimeEnd() {
        return updateTimeEnd;
    }

    public void setUpdateTimeEnd(String updateTimeEnd) {
        this.updateTimeEnd = updateTimeEnd;
    }
}
