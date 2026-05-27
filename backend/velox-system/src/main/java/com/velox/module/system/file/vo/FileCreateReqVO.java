package com.velox.module.system.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "openapi.system.file.create_req.schema")
public class FileCreateReqVO {

    @Schema(description = "openapi.system.file.create_req.config_id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{validation.system.file.create.config_id.not_empty}")
    private String configId;

    @Schema(description = "openapi.system.file.create_req.path", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{validation.system.file.create.path.not_empty}")
    private String path;

    @Schema(description = "openapi.system.file.create_req.url")
    private String url;

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
