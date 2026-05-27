package com.velox.module.system.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "openapi.system.file.resp.schema")
public class FileRespVO {

    @Schema(description = "openapi.system.file.resp.id")
    private String id;

    @Schema(description = "openapi.system.file.resp.config_id")
    private String configId;

    @Schema(description = "openapi.system.file.resp.name")
    private String name;

    @Schema(description = "openapi.system.file.resp.path")
    private String path;

    @Schema(description = "openapi.system.file.resp.url")
    private String url;

    @Schema(description = "openapi.system.file.resp.type")
    private String type;

    @Schema(description = "openapi.system.file.resp.size")
    private Long size;

    @Schema(description = "openapi.system.file.resp.upload_time")
    private String uploadTime;

    @Schema(description = "openapi.system.file.resp.create_by")
    private String createBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }
}
