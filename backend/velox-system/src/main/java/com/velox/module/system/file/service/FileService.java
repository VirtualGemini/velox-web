package com.velox.module.system.file.service;

import com.velox.common.result.PageResult;
import com.velox.module.system.file.vo.FileCreateReqVO;
import com.velox.module.system.file.vo.FilePageReqVO;
import com.velox.module.system.file.vo.FilePresignedUrlRespVO;
import com.velox.module.system.file.vo.FileRespVO;

import java.util.List;

public interface FileService {

    PageResult<FileRespVO> getFilePage(FilePageReqVO pageReqVO);

    List<String> getFileTypes();

    String createFile(byte[] content, String name, String directory, String type);

    FilePresignedUrlRespVO presignPutUrl(String name, String directory);

    String presignGetUrl(String configId, String url, Integer expirationSeconds);

    String createFile(FileCreateReqVO createReqVO);

    FileRespVO getFile(String id);

    void deleteFile(String id);

    void deleteFileList(List<String> ids);

    byte[] getFileContent(String configId, String path);
}
