package com.velox.module.system.file.domain.model;

import com.velox.domain.model.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Objects;

@TableName(value = "sys_file_config")
public class FileConfig extends BaseEntity {

    private String name;

    private Integer storage;

    private String config;

    private Boolean master;

    private String remark;

    private Integer enabled;

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

    public FileConfig setMasterReturnThis(Boolean master) {
        this.master = master;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FileConfig that = (FileConfig) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(storage, that.storage) &&
                Objects.equals(config, that.config) &&
                Objects.equals(master, that.master) &&
                Objects.equals(remark, that.remark) &&
                Objects.equals(enabled, that.enabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, storage, config, master, remark, enabled);
    }
}
