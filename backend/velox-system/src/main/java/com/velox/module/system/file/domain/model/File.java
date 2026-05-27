package com.velox.module.system.file.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Objects;

@TableName(value = "sys_file")
public class File {

    @TableId(type = IdType.INPUT)
    private String id;

    private String configId;

    private String name;

    private String path;

    private String url;

    private String type;

    private Long size;

    @TableField("upload_time")
    private LocalDateTime uploadTime;

    private String createBy;

    private String updateBy;

    private Integer deleted;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = normalizeIdentifier(id);
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = normalizeIdentifier(configId);
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

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = normalizeIdentifier(createBy);
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = normalizeIdentifier(updateBy);
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    private static String normalizeIdentifier(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        File file = (File) o;
        return Objects.equals(id, file.id) &&
                Objects.equals(configId, file.configId) &&
                Objects.equals(name, file.name) &&
                Objects.equals(path, file.path) &&
                Objects.equals(url, file.url) &&
                Objects.equals(type, file.type) &&
                Objects.equals(size, file.size) &&
                Objects.equals(uploadTime, file.uploadTime) &&
                Objects.equals(createBy, file.createBy) &&
                Objects.equals(updateBy, file.updateBy) &&
                Objects.equals(deleted, file.deleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, configId, name, path, url, type, size, uploadTime, createBy, updateBy, deleted);
    }
}
