package com.velox.module.system.file.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.velox.framework.persistence.api.mapper.BaseMapperExt;
import com.velox.module.system.file.domain.model.FileContent;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileContentMapper extends BaseMapperExt<FileContent> {

    default List<FileContent> selectListByConfigIdAndPath(String configId, String path) {
        return selectList(new LambdaQueryWrapper<FileContent>()
                .eq(FileContent::getConfigId, configId)
                .eq(FileContent::getPath, path));
    }

    default void deleteByConfigIdAndPath(String configId, String path) {
        delete(new LambdaUpdateWrapper<FileContent>()
                .eq(FileContent::getConfigId, configId)
                .eq(FileContent::getPath, path));
    }
}
