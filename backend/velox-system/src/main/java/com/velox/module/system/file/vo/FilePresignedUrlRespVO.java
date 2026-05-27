package com.velox.module.system.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "openapi.system.file.presigned_url_resp.schema")
public class FilePresignedUrlRespVO {

    @Schema(description = "openapi.system.file.presigned_url_resp.config_id")
    private String configId;

    @Schema(description = "openapi.system.file.presigned_url_resp.upload_url")
    private String uploadUrl;

    @Schema(description = "openapi.system.file.presigned_url_resp.path")
    private String path;

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
