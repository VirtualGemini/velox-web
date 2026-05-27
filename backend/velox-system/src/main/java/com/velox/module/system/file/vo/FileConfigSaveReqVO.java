package com.velox.module.system.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "openapi.system.file_config.save_req.schema")
public class FileConfigSaveReqVO {

    @Schema(description = "openapi.common.id")
    private String id;

    @Schema(description = "openapi.system.file_config.save_req.name", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{validation.system.file_config.name.not_empty}")
    private String name;

    @Schema(description = "openapi.system.file_config.save_req.storage", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{validation.system.file_config.storage.not_null}")
    private Integer storage;

    @Schema(description = "openapi.system.file_config.save_req.config", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{validation.system.file_config.config.not_empty}")
    private String config;

    @Schema(description = "openapi.system.file_config.save_req.master")
    private Boolean master;

    @Schema(description = "openapi.common.enabled")
    private Integer enabled;

    @Schema(description = "openapi.common.remark")
    private String remark;

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

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
