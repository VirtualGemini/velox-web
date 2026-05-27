package com.velox.module.system.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "openapi.system.file.page_req.schema")
public class FilePageReqVO {

    @Schema(description = "openapi.system.file.page_req.path", example = "upload/2024/01")
    private String path;

    @Schema(description = "openapi.system.file.page_req.name", example = "report.md")
    private String name;

    @Schema(description = "openapi.system.file.page_req.type", example = "image/jpeg")
    private String type;

    @Schema(description = "openapi.system.file.page_req.size_min_bytes", example = "1024")
    private Long sizeMinBytes;

    @Schema(description = "openapi.system.file.page_req.size_max_bytes", example = "1048576")
    private Long sizeMaxBytes;

    @Schema(description = "openapi.system.file.page_req.upload_time_start", example = "2026-05-22 10:00:00")
    private String uploadTimeStart;

    @Schema(description = "openapi.system.file.page_req.upload_time_end", example = "2026-05-22 18:00:00")
    private String uploadTimeEnd;

    @Schema(description = "openapi.common.page.page", example = "1")
    private Integer page = 1;

    @Schema(description = "openapi.common.page.size", example = "10")
    private Integer size = 10;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSizeMinBytes() {
        return sizeMinBytes;
    }

    public void setSizeMinBytes(Long sizeMinBytes) {
        this.sizeMinBytes = sizeMinBytes;
    }

    public Long getSizeMaxBytes() {
        return sizeMaxBytes;
    }

    public void setSizeMaxBytes(Long sizeMaxBytes) {
        this.sizeMaxBytes = sizeMaxBytes;
    }

    public String getUploadTimeStart() {
        return uploadTimeStart;
    }

    public void setUploadTimeStart(String uploadTimeStart) {
        this.uploadTimeStart = uploadTimeStart;
    }

    public String getUploadTimeEnd() {
        return uploadTimeEnd;
    }

    public void setUploadTimeEnd(String uploadTimeEnd) {
        this.uploadTimeEnd = uploadTimeEnd;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
