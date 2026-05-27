package com.velox.module.system.file.persistence;

import com.velox.framework.persistence.api.mapper.BaseMapperExt;
import com.velox.module.system.file.domain.model.FileConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper
public interface FileConfigMapper extends BaseMapperExt<FileConfig> {

    default FileConfig selectByMaster() {
        return selectOne(new LambdaQueryWrapper<FileConfig>()
                .eq(FileConfig::getMaster, true));
    }

    default List<FileConfig> selectListByMaster(Boolean master) {
        return selectList(new LambdaQueryWrapper<FileConfig>()
                .eq(FileConfig::getMaster, master));
    }

    default void updateNoneMaster() {
        update(new LambdaUpdateWrapper<FileConfig>()
                .set(FileConfig::getMaster, false)
                .set(FileConfig::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC))
                .eq(FileConfig::getMaster, true));
    }
}
