package com.velox.module.system.file.persistence;

import com.velox.framework.persistence.api.mapper.BaseMapperExt;
import com.velox.module.system.file.domain.model.File;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileMapper extends BaseMapperExt<File> {

    default List<File> selectListByConfigIdAndPath(String configId, String path) {
        return selectList(new LambdaQueryWrapper<File>()
                .eq(File::getConfigId, configId)
                .eq(File::getPath, path));
    }

    default void deleteByConfigIdAndPath(String configId, String path) {
        delete(new LambdaUpdateWrapper<File>()
                .eq(File::getConfigId, configId)
                .eq(File::getPath, path));
    }
}
