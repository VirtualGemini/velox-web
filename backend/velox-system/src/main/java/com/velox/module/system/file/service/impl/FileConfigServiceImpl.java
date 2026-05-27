package com.velox.module.system.file.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.common.exception.MessageUtils;
import com.velox.common.result.PageResult;
import com.velox.module.system.file.domain.model.FileConfig;
import com.velox.framework.file.api.client.FileClient;
import com.velox.framework.file.api.diagnostics.FileFailureReason;
import com.velox.framework.file.api.diagnostics.FileFailureReasonResolver;
import com.velox.framework.file.spi.client.FileClientConfig;
import com.velox.framework.file.spi.client.FileClientManager;
import com.velox.module.system.file.persistence.FileConfigMapper;
import com.velox.framework.web.RequestDateTimeFormatter;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.file.service.FileConfigService;
import com.velox.module.system.file.support.FileStorageConfigClassResolver;
import com.velox.module.system.file.vo.FileConfigPageReqVO;
import com.velox.module.system.file.vo.FileConfigRespVO;
import com.velox.module.system.file.vo.FileConfigSaveReqVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ExecutionError;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Validated
public class FileConfigServiceImpl implements FileConfigService {

    private static final String CACHE_MASTER_ID = "master";

    private final LoadingCache<String, FileClient> clientCache = CacheBuilder.newBuilder()
            .refreshAfterWrite(Duration.ofSeconds(10L))
            .build(new CacheLoader<>() {
                @Override
                public FileClient load(String id) {
                    FileConfig config = Objects.equals(CACHE_MASTER_ID, id) ?
                            fileConfigMapper.selectByMaster() : fileConfigMapper.selectById(id);
                    if (config != null) {
                        FileClientConfig clientConfig = parseClientConfig(config.getStorage(), config.getConfig());
                        fileClientManager.createOrUpdateFileClient(config.getId(), config.getStorage(), clientConfig);
                    }
                    return fileClientManager.requireFileClient(null == config ? id : config.getId());
                }
            });

    private final FileClientManager fileClientManager;

    private final FileConfigMapper fileConfigMapper;

    private final Validator validator;

    private final SystemEntityIdGenerator entityIdGenerator;

    private final FileFailureReasonResolver fileFailureReasonResolver;

    public FileConfigServiceImpl(FileClientManager fileClientManager,
                                 FileConfigMapper fileConfigMapper,
                                 Validator validator,
                                 SystemEntityIdGenerator entityIdGenerator,
                                 FileFailureReasonResolver fileFailureReasonResolver) {
        this.fileClientManager = fileClientManager;
        this.fileConfigMapper = fileConfigMapper;
        this.validator = validator;
        this.entityIdGenerator = entityIdGenerator;
        this.fileFailureReasonResolver = fileFailureReasonResolver;
    }

    public LoadingCache<String, FileClient> getClientCache() {
        return clientCache;
    }

    @Override
    public String createFileConfig(FileConfigSaveReqVO createReqVO) {
        validateClientConfig(createReqVO.getStorage(), createReqVO.getConfig());
        FileConfig fileConfig = new FileConfig();
        fileConfig.setId(entityIdGenerator.nextId(FileConfig.class));
        fileConfig.setName(createReqVO.getName());
        fileConfig.setStorage(createReqVO.getStorage());
        fileConfig.setConfig(createReqVO.getConfig());
        fileConfig.setRemark(createReqVO.getRemark());
        fileConfig.setMaster(false);
        fileConfig.setEnabled(createReqVO.getEnabled() != null ? createReqVO.getEnabled() : 1);
        fileConfigMapper.insert(fileConfig);
        return fileConfig.getId();
    }

    @Override
    public void updateFileConfig(FileConfigSaveReqVO updateReqVO) {
        FileConfig config = validateFileConfigExists(updateReqVO.getId());
        validateClientConfig(updateReqVO.getStorage(), updateReqVO.getConfig());
        FileConfig updateObj = new FileConfig();
        updateObj.setId(updateReqVO.getId());
        updateObj.setName(updateReqVO.getName());
        updateObj.setStorage(updateReqVO.getStorage());
        updateObj.setConfig(updateReqVO.getConfig());
        updateObj.setRemark(updateReqVO.getRemark());
        updateObj.setMaster(updateReqVO.getMaster());
        updateObj.setEnabled(updateReqVO.getEnabled());
        fileConfigMapper.updateById(updateObj);
        clearCache(config.getId(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFileConfigMaster(String id) {
        validateFileConfigExists(id);
        fileConfigMapper.updateNoneMaster();
        FileConfig updateObj = new FileConfig();
        updateObj.setId(id);
        updateObj.setMaster(true);
        fileConfigMapper.updateById(updateObj);
        clearCache(null, true);
    }

    @Override
    public void updateFileConfigEnabled(String id, Integer enabled) {
        validateFileConfigExists(id);
        FileConfig updateObj = new FileConfig();
        updateObj.setId(id);
        updateObj.setEnabled(enabled);
        fileConfigMapper.updateById(updateObj);
        clearCache(id, null);
    }

    private void validateClientConfig(Integer storage, String config) {
        FileClientConfig clientConfig = parseClientConfig(storage, config);
        Set<ConstraintViolation<FileClientConfig>> violations = validator.validate(clientConfig);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .distinct()
                    .reduce((left, right) -> left + "; " + right)
                    .orElse("文件配置不合法");
            throw new ApiException(BusinessErrorCode.FILE_CONFIG_INVALID, message);
        }
    }

    private FileClientConfig parseClientConfig(Integer storage, String config) {
        Class<? extends FileClientConfig> configClass = FileStorageConfigClassResolver.resolve(storage);
        if (configClass == null) {
            throw new ApiException(BusinessErrorCode.FILE_STORAGE_TYPE_UNSUPPORTED, storage);
        }
        return JSONUtil.toBean(config, configClass);
    }

    @Override
    public void deleteFileConfig(String id) {
        FileConfig config = validateFileConfigExists(id);
        if (Boolean.TRUE.equals(config.getMaster())) {
            throw new ApiException(BusinessErrorCode.FILE_CONFIG_DELETE_FAIL_MASTER);
        }
        fileConfigMapper.deleteById(id);
        clearCache(id, null);
    }

    @Override
    public void deleteFileConfigList(List<String> ids) {
        List<FileConfig> configs = fileConfigMapper.selectByIds(ids);
        for (FileConfig config : configs) {
            if (Boolean.TRUE.equals(config.getMaster())) {
                throw new ApiException(BusinessErrorCode.FILE_CONFIG_DELETE_FAIL_MASTER);
            }
        }
        fileConfigMapper.deleteByIds(ids);
        ids.forEach(id -> clearCache(id, null));
    }

    private void clearCache(String id, Boolean master) {
        if (id != null) {
            clientCache.invalidate(id);
        }
        if (Boolean.TRUE.equals(master)) {
            clientCache.invalidate(CACHE_MASTER_ID);
        }
    }

    private FileConfig validateFileConfigExists(String id) {
        FileConfig config = fileConfigMapper.selectById(id);
        if (config == null) {
            throw new ApiException(BusinessErrorCode.FILE_CONFIG_NOT_FOUND);
        }
        return config;
    }

    @Override
    public FileConfigRespVO getFileConfig(String id) {
        return toFileConfigRespVO(fileConfigMapper.selectById(id));
    }

    @Override
    public PageResult<FileConfigRespVO> getFileConfigPage(FileConfigPageReqVO pageReqVO) {
        LambdaQueryWrapper<FileConfig> wrapper = new LambdaQueryWrapper<FileConfig>()
                .like(StrUtil.isNotEmpty(pageReqVO.getName()), FileConfig::getName, pageReqVO.getName())
                .eq(pageReqVO.getStorage() != null, FileConfig::getStorage, pageReqVO.getStorage())
                .eq(pageReqVO.getEnabled() != null, FileConfig::getEnabled, pageReqVO.getEnabled())
                .ge(StrUtil.isNotBlank(pageReqVO.getCreateTimeStart()), FileConfig::getCreateTime,
                        RequestDateTimeFormatter.parseToUtc(pageReqVO.getCreateTimeStart()))
                .le(StrUtil.isNotBlank(pageReqVO.getCreateTimeEnd()), FileConfig::getCreateTime,
                        RequestDateTimeFormatter.parseToUtc(pageReqVO.getCreateTimeEnd()))
                .ge(StrUtil.isNotBlank(pageReqVO.getUpdateTimeStart()), FileConfig::getUpdateTime,
                        RequestDateTimeFormatter.parseToUtc(pageReqVO.getUpdateTimeStart()))
                .le(StrUtil.isNotBlank(pageReqVO.getUpdateTimeEnd()), FileConfig::getUpdateTime,
                        RequestDateTimeFormatter.parseToUtc(pageReqVO.getUpdateTimeEnd()))
                .orderByDesc(FileConfig::getCreateTime)
                .orderByDesc(FileConfig::getUpdateTime);
        Page<FileConfig> page = fileConfigMapper.selectPage(
                new Page<>(pageReqVO.getPage(), pageReqVO.getSize()), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getRecords().stream().map(this::toFileConfigRespVO).toList());
    }

    @Override
    public List<Integer> getSupportedStorageTypes() {
        return fileClientManager.getSupportedStorageTypes().stream().sorted().toList();
    }

    @Override
    public String testFileConfig(String id) {
        validateFileConfigExists(id);
        byte[] content = "test".getBytes();
        try {
            return getFileClient(id).upload(content, IdUtil.fastSimpleUUID() + ".txt", "text/plain");
        } catch (ApiException exception) {
            throw exception;
        } catch (ExecutionError exception) {
            throw buildFileConfigTestFailed(exception);
        } catch (Exception exception) {
            throw buildFileConfigTestFailed(exception);
        }
    }

    @Override
    public FileClient getFileClient(String id) {
        return clientCache.getUnchecked(id);
    }

    @Override
    public FileClient getMasterFileClient() {
        return clientCache.getUnchecked(CACHE_MASTER_ID);
    }

    private ApiException buildFileConfigTestFailed(Throwable cause) {
        FileFailureReason reason = fileFailureReasonResolver.resolve(cause);
        return new ApiException(cause, BusinessErrorCode.FILE_CONFIG_TEST_FAILED, localizeTestFailureReason(reason));
    }

    private String localizeTestFailureReason(FileFailureReason reason) {
        String key = "file.config.test.reason." + reason.code();
        String localized = MessageUtils.message(key);
        if (localized != null) {
            return localized;
        }
        return switch (reason.code()) {
            case "capability_unavailable" -> "当前环境未启用该存储能力";
            case "connect_failed" -> "无法连接到目标存储服务";
            case "connect_timeout" -> "连接目标存储服务超时";
            case "access_denied" -> "访问凭证或权限校验失败";
            default -> "连接或初始化失败";
        };
    }

    private FileConfigRespVO toFileConfigRespVO(FileConfig fileConfig) {
        if (fileConfig == null) {
            return null;
        }
        FileConfigRespVO respVO = new FileConfigRespVO();
        respVO.setId(fileConfig.getId());
        respVO.setName(fileConfig.getName());
        respVO.setStorage(fileConfig.getStorage());
        respVO.setConfig(fileConfig.getConfig());
        respVO.setMaster(fileConfig.getMaster());
        respVO.setRemark(fileConfig.getRemark());
        respVO.setEnabled(fileConfig.getEnabled());
        respVO.setCreateBy(fileConfig.getCreateBy());
        respVO.setCreateTime(RequestDateTimeFormatter.format(fileConfig.getCreateTime()));
        respVO.setUpdateTime(RequestDateTimeFormatter.format(fileConfig.getUpdateTime()));
        return respVO;
    }
}
