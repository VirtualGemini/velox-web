package com.velox.module.system.file.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.common.result.PageResult;
import com.velox.module.system.file.domain.model.File;
import com.velox.framework.file.api.client.FileClient;
import com.velox.framework.file.api.util.FileTypeUtils;
import com.velox.framework.id.BusinessIdGenerator;
import com.velox.module.system.file.persistence.FileMapper;
import com.velox.framework.web.RequestDateTimeFormatter;
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
import java.util.List;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    private static final String ROOT_DIRECTORY = "uploads";
    private static final String DIRECTORY_AVATAR = "avatar";
    private static final String DIRECTORY_FILE = "file";

    static boolean PATH_SUFFIX_TIMESTAMP_ENABLE = true;

    private final FileConfigService fileConfigService;

    private final FileMapper fileMapper;

    private final BusinessIdGenerator businessIdGenerator;

    public FileServiceImpl(FileConfigService fileConfigService,
                           FileMapper fileMapper,
                           BusinessIdGenerator businessIdGenerator) {
        this.fileConfigService = fileConfigService;
        this.fileMapper = fileMapper;
        this.businessIdGenerator = businessIdGenerator;
    }

    @Override
    public PageResult<FileRespVO> getFilePage(FilePageReqVO pageReqVO) {
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<File>()
                .like(StrUtil.isNotEmpty(pageReqVO.getPath()), File::getPath, pageReqVO.getPath())
                .like(StrUtil.isNotEmpty(pageReqVO.getType()), File::getType, pageReqVO.getType())
                .orderByDesc(File::getCreateTime)
                .orderByDesc(File::getUpdateTime);
        Page<File> page = fileMapper.selectPage(
                new Page<>(pageReqVO.getPage(), pageReqVO.getSize()), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getRecords().stream().map(this::toFileRespVO).toList());
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
            fileDO.setId(businessIdGenerator.nextFileId());
            fileDO.setConfigId(client.getId());
            fileDO.setName(name);
            fileDO.setPath(path);
            fileDO.setUrl(url);
            fileDO.setType(type);
            fileDO.setSize((long) content.length);
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
    public String presignGetUrl(String url, Integer expirationSeconds) {
        FileClient fileClient = fileConfigService.getMasterFileClient();
        return fileClient.presignGetUrl(url, expirationSeconds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createFile(FileCreateReqVO createReqVO) {
        File fileDO = new File();
        fileDO.setId(businessIdGenerator.nextFileId());
        fileDO.setConfigId(createReqVO.getConfigId());
        fileDO.setPath(createReqVO.getPath());
        fileDO.setUrl(createReqVO.getUrl());
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
        respVO.setCreateTime(RequestDateTimeFormatter.format(file.getCreateTime()));
        respVO.setUpdateTime(RequestDateTimeFormatter.format(file.getUpdateTime()));
        return respVO;
    }

    private String normalizeBusinessDirectory(String directory) {
        if (StrUtil.equalsIgnoreCase(directory, DIRECTORY_AVATAR)) {
            return DIRECTORY_AVATAR;
        }
        return DIRECTORY_FILE;
    }
}
