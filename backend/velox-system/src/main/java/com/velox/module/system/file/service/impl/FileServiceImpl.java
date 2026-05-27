package com.velox.module.system.file.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.common.result.PageResult;
import com.velox.framework.file.common.error.FileErrorCode;
import com.velox.framework.file.exception.FileClientException;
import com.velox.module.system.file.domain.model.File;
import com.velox.framework.file.api.client.FileClient;
import com.velox.framework.file.api.util.FileTypeUtils;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.module.system.file.persistence.FileMapper;
import com.velox.framework.web.RequestDateTimeFormatter;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.file.service.FileConfigService;
import com.velox.module.system.file.service.FileService;
import com.velox.module.system.file.vo.FileCreateReqVO;
import com.velox.module.system.file.vo.FilePageReqVO;
import com.velox.module.system.file.vo.FilePresignedUrlRespVO;
import com.velox.module.system.file.vo.FileRespVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    private static final String ROOT_DIRECTORY = "uploads";
    private static final String DIRECTORY_AVATAR = "avatar";
    private static final String DIRECTORY_FILE = "file";

    static boolean PATH_SUFFIX_TIMESTAMP_ENABLE = true;

    private final FileConfigService fileConfigService;

    private final FileMapper fileMapper;

    private final SystemEntityIdGenerator entityIdGenerator;

    private final SecuritySessionService securitySessionService;

    public FileServiceImpl(FileConfigService fileConfigService,
                           FileMapper fileMapper,
                           SystemEntityIdGenerator entityIdGenerator,
                           SecuritySessionService securitySessionService) {
        this.fileConfigService = fileConfigService;
        this.fileMapper = fileMapper;
        this.entityIdGenerator = entityIdGenerator;
        this.securitySessionService = securitySessionService;
    }

    @Override
    public PageResult<FileRespVO> getFilePage(FilePageReqVO pageReqVO) {
        String name = StrUtil.trim(pageReqVO.getName());
        String type = StrUtil.trim(pageReqVO.getType());
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<File>()
                .like(StrUtil.isNotBlank(name), File::getName, name)
                .like(StrUtil.isNotEmpty(pageReqVO.getPath()), File::getPath, pageReqVO.getPath())
                .eq(StrUtil.isNotBlank(type), File::getType, type)
                .ge(pageReqVO.getSizeMinBytes() != null, File::getSize, pageReqVO.getSizeMinBytes())
                .le(pageReqVO.getSizeMaxBytes() != null, File::getSize, pageReqVO.getSizeMaxBytes())
                .ge(StrUtil.isNotBlank(pageReqVO.getUploadTimeStart()), File::getUploadTime,
                        RequestDateTimeFormatter.parseToUtc(pageReqVO.getUploadTimeStart()))
                .le(StrUtil.isNotBlank(pageReqVO.getUploadTimeEnd()), File::getUploadTime,
                        RequestDateTimeFormatter.parseToUtc(pageReqVO.getUploadTimeEnd()))
                .orderByDesc(File::getUploadTime);
        Page<File> page = fileMapper.selectPage(
                new Page<>(pageReqVO.getPage(), pageReqVO.getSize()), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getRecords().stream().map(this::toFileRespVO).toList());
    }

    @Override
    public List<String> getFileTypes() {
        return fileMapper.selectList(new LambdaQueryWrapper<File>()
                        .select(File::getType)
                        .isNotNull(File::getType)
                        .groupBy(File::getType)
                        .orderByAsc(File::getType))
                .stream()
                .map(File::getType)
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createFile(byte[] content, String name, String directory, String type) {
        if (StrUtil.isEmpty(type)) {
            type = FileTypeUtils.getMineType(content, name);
        }
        if (StrUtil.isEmpty(name)) {
            name = DigestUtil.sha256Hex(content);
        }
        if (StrUtil.isEmpty(FileUtil.extName(name))) {
            String extension = FileTypeUtils.getExtension(type);
            if (StrUtil.isNotEmpty(extension)) {
                name = name + extension;
            }
        }

        String path = generateUploadPath(name, directory);
        FileClient client = fileConfigService.getMasterFileClient();
        String url = client.upload(content, path, type);

        try {
            File fileDO = new File();
            fileDO.setId(entityIdGenerator.nextId(File.class));
            fileDO.setConfigId(client.getId());
            fileDO.setName(name);
            fileDO.setPath(path);
            fileDO.setUrl(url);
            fileDO.setType(type);
            fileDO.setSize((long) content.length);
            fillCreateAuditFields(fileDO);
            fileMapper.insert(fileDO);
            return url;
        } catch (Exception ex) {
            deleteUploadedFileQuietly(client, path, ex);
            throw ex;
        }
    }

    String generateUploadPath(String name, String directory) {
        LocalDateTime now = LocalDateTimeUtil.now();
        String year = LocalDateTimeUtil.format(now, "yyyy");
        String month = LocalDateTimeUtil.format(now, "MM");
        String day = LocalDateTimeUtil.format(now, "dd");
        String businessDirectory = normalizeBusinessDirectory(directory);

        String suffix = null;
        if (PATH_SUFFIX_TIMESTAMP_ENABLE) {
            suffix = String.valueOf(System.currentTimeMillis());
        }

        if (StrUtil.isNotEmpty(suffix)) {
            String ext = FileUtil.extName(name);
            if (StrUtil.isNotEmpty(ext)) {
                name = FileUtil.mainName(name) + StrUtil.C_UNDERLINE + suffix + StrUtil.DOT + ext;
            } else {
                name = name + StrUtil.C_UNDERLINE + suffix;
            }
        }
        return StrUtil.join(StrUtil.SLASH,
                ROOT_DIRECTORY,
                year,
                month,
                businessDirectory,
                day,
                name);
    }

    @Override
    public FilePresignedUrlRespVO presignPutUrl(String name, String directory) {
        String path = generateUploadPath(name, directory);
        FileClient fileClient = fileConfigService.getMasterFileClient();
        String uploadUrl = fileClient.presignPutUrl(path);
        FilePresignedUrlRespVO respVO = new FilePresignedUrlRespVO();
        respVO.setConfigId(fileClient.getId());
        respVO.setPath(path);
        respVO.setUploadUrl(uploadUrl);
        return respVO;
    }

    @Override
    public String presignGetUrl(String configId, String url, Integer expirationSeconds) {
        FileClient fileClient = StrUtil.isNotBlank(configId)
                ? fileConfigService.getFileClient(configId)
                : fileConfigService.getMasterFileClient();
        try {
            return fileClient.presignGetUrl(url, expirationSeconds);
        } catch (FileClientException exception) {
            if (FileErrorCode.OPERATION_NOT_SUPPORTED.code().equals(exception.getCode()) &&
                    StrUtil.isNotBlank(url)) {
                return url;
            }
            throw exception;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createFile(FileCreateReqVO createReqVO) {
        File fileDO = new File();
        fileDO.setId(entityIdGenerator.nextId(File.class));
        fileDO.setConfigId(createReqVO.getConfigId());
        fileDO.setName(FileUtil.getName(createReqVO.getPath()));
        fileDO.setPath(createReqVO.getPath());
        fileDO.setUrl(createReqVO.getUrl());
        fillCreateAuditFields(fileDO);
        fileMapper.insert(fileDO);
        return fileDO.getId();
    }

    @Override
    public FileRespVO getFile(String id) {
        return toFileRespVO(validateFileExists(id));
    }

    @Override
    public void deleteFile(String id) {
        File fileDO = validateFileExists(id);
        FileClient client = fileConfigService.getFileClient(fileDO.getConfigId());
        client.delete(fileDO.getPath());
        fileMapper.deleteById(id);
    }

    @Override
    public void deleteFileList(List<String> ids) {
        List<File> files = fileMapper.selectByIds(ids);
        for (File fileDO : files) {
            FileClient client = fileConfigService.getFileClient(fileDO.getConfigId());
            client.delete(fileDO.getPath());
        }
        fileMapper.deleteByIds(ids);
    }

    private File validateFileExists(String id) {
        File fileDO = fileMapper.selectById(id);
        if (fileDO == null) {
            throw new ApiException(BusinessErrorCode.FILE_NOT_FOUND);
        }
        return fileDO;
    }

    @Override
    public byte[] getFileContent(String configId, String path) {
        FileClient client = fileConfigService.getFileClient(configId);
        return client.getContent(path);
    }

    private void deleteUploadedFileQuietly(FileClient client, String path, Exception cause) {
        try {
            client.delete(path);
        } catch (Exception cleanupEx) {
            cause.addSuppressed(cleanupEx);
            log.warn("[createFile][上传记录写入失败后清理文件失败，configId({}) path({})]",
                    client.getId(), path, cleanupEx);
        }
    }

    private void fillCreateAuditFields(File fileDO) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String currentUserId = StrUtil.trim(securitySessionService.currentLoginIdOrNull());
        fileDO.setUploadTime(now);
        fileDO.setCreateBy(currentUserId);
        fileDO.setUpdateBy(currentUserId);
        fileDO.setDeleted(0);
    }

    private FileRespVO toFileRespVO(File file) {
        FileRespVO respVO = new FileRespVO();
        respVO.setId(file.getId());
        respVO.setConfigId(file.getConfigId());
        respVO.setName(file.getName());
        respVO.setPath(file.getPath());
        respVO.setUrl(file.getUrl());
        respVO.setType(file.getType());
        respVO.setSize(file.getSize());
        respVO.setCreateBy(file.getCreateBy());
        respVO.setUploadTime(RequestDateTimeFormatter.format(file.getUploadTime()));
        return respVO;
    }

    private String normalizeBusinessDirectory(String directory) {
        if (StrUtil.equalsIgnoreCase(directory, DIRECTORY_AVATAR)) {
            return DIRECTORY_AVATAR;
        }
        return DIRECTORY_FILE;
    }
}
