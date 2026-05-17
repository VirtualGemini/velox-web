package com.velox.module.system.file.service;

import com.velox.common.result.PageResult;
import com.velox.framework.file.api.client.FileClient;
import com.velox.module.system.file.vo.FileConfigPageReqVO;
import com.velox.module.system.file.vo.FileConfigRespVO;
import com.velox.module.system.file.vo.FileConfigSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

public interface FileConfigService {

    String createFileConfig(@Valid FileConfigSaveReqVO createReqVO);

    void updateFileConfig(@Valid FileConfigSaveReqVO updateReqVO);

    void updateFileConfigMaster(String id);

    void updateFileConfigEnabled(String id, Integer enabled);

    void deleteFileConfig(String id);

    void deleteFileConfigList(List<String> ids);

    FileConfigRespVO getFileConfig(String id);

    PageResult<FileConfigRespVO> getFileConfigPage(FileConfigPageReqVO pageReqVO);

    List<Integer> getSupportedStorageTypes();

    String testFileConfig(String id);

    FileClient getFileClient(String id);

    FileClient getMasterFileClient();
}
