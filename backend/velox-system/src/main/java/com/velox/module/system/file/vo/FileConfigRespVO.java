package com.velox.module.system.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "openapi.system.file_config.resp.schema")
public class FileConfigRespVO {

    @Schema(description = "openapi.common.id")
    private String id;

    @Schema(description = "openapi.system.file_config.resp.name")
    private String name;

    @Schema(description = "openapi.system.file_config.resp.storage")
    private Integer storage;

    @Schema(description = "openapi.system.file_config.resp.config")
    private String config;

    @Schema(description = "openapi.system.file_config.resp.master")
    private Boolean master;

    @Schema(description = "openapi.common.remark")
    private String remark;

    @Schema(description = "openapi.common.enabled")
    private Integer enabled;

    @Schema(description = "openapi.common.audit.create_time")
    private String createTime;

    @Schema(description = "openapi.common.audit.update_time")
    private String updateTime;

    @Schema(description = "openapi.common.audit.create_by")
    private String createBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStorage() {
        return storage;
    }

    public void setStorage(Integer storage) {
        this.storage = storage;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public Boolean getMaster() {
        return master;
    }

    public void setMaster(Boolean master) {
        this.master = master;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }
}
