package com.velox.module.system.file.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.velox.domain.model.BaseEntity;

import java.util.Objects;

@TableName("sys_file_content")
public class FileContent extends BaseEntity {

    private String configId;
    private String path;
    private byte[] content;

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = normalizeIdentifier(configId);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FileContent that = (FileContent) o;
        return Objects.equals(configId, that.configId) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), configId, path);
    }
}
