package com.velox.module.system.file.provider.db;

import cn.hutool.core.collection.CollUtil;
import com.velox.framework.file.spi.client.AbstractFileClient;
import com.velox.module.system.file.domain.model.FileContent;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.file.persistence.FileContentMapper;
import org.springframework.context.ApplicationContext;

import java.util.Comparator;
import java.util.List;

public class DbFileClient extends AbstractFileClient<DbFileClientConfig> {

    private final ApplicationContext applicationContext;
    private FileContentMapper fileContentMapper;
    private SystemEntityIdGenerator entityIdGenerator;

    public DbFileClient(String id, DbFileClientConfig config, ApplicationContext applicationContext) {
        super(id, config);
        this.applicationContext = applicationContext;
    }

    @Override
    protected void doInit() {
        this.fileContentMapper = applicationContext.getBean(FileContentMapper.class);
        this.entityIdGenerator = applicationContext.getBean(SystemEntityIdGenerator.class);
    }

    @Override
    protected String doUpload(byte[] content, String path, String type) {
        FileContent entity = new FileContent();
        entity.setId(entityIdGenerator.nextId(FileContent.class));
        entity.setConfigId(getId());
        entity.setPath(path);
        entity.setContent(content);
        fileContentMapper.insert(entity);
        return formatFileUrl(config.getDomain(), path);
    }

    @Override
    protected void doDelete(String path) {
        fileContentMapper.deleteByConfigIdAndPath(getId(), path);
    }

    @Override
    protected byte[] doGetContent(String path) {
        List<FileContent> list = fileContentMapper.selectListByConfigIdAndPath(getId(), path);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        list.sort(Comparator
                .comparing(FileContent::getCreateTime, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(FileContent::getUpdateTime, Comparator.nullsFirst(Comparator.naturalOrder())));
        return CollUtil.getLast(list).getContent();
    }
}
