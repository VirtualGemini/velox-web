package com.velox.module.system.file.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.common.result.PageResult;
import com.velox.common.result.Result;
import com.velox.framework.file.api.util.FileTypeUtils;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.file.service.FileService;
import com.velox.module.system.file.vo.FileCreateReqVO;
import com.velox.module.system.file.vo.FilePageReqVO;
import com.velox.module.system.file.vo.FilePresignedUrlRespVO;
import com.velox.module.system.file.vo.FileRespVO;
import com.velox.module.system.file.vo.FileUploadReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "openapi.system.file.tag.name", description = "openapi.system.file.tag.description")
@RestController
@RequestMapping("/file")
@Validated
public class FileController {

    private final FileService fileService;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public FileController(
            FileService fileService,
            SystemFrontendIdCodecSupport frontendIdCodecSupport
    ) {
        this.fileService = fileService;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @PostMapping("/upload")
    @Operation(summary = "openapi.system.file.upload.summary", description = "openapi.system.file.upload.description")
    @RequirePermission("system:file:upload")
    public Result<String> uploadFile(@Valid FileUploadReqVO uploadReqVO) throws java.io.IOException {
        MultipartFile file = uploadReqVO.getFile();
        byte[] content = IoUtil.readBytes(file.getInputStream());
        return Result.ok(frontendIdCodecSupport.encodeIdentifier(fileService.createFile(
                content,
                file.getOriginalFilename(),
                uploadReqVO.getDirectory(),
                file.getContentType()
        )));
    }

    @GetMapping("/presigned-url")
    @Operation(summary = "openapi.system.file.presign_put_url.summary", description = "openapi.system.file.presign_put_url.description")
    public Result<FilePresignedUrlRespVO> presignPutUrl(
            @RequestParam("name") String name,
            @RequestParam(value = "directory", required = false) String directory) {
        return Result.ok(fileService.presignPutUrl(name, directory));
    }

    @PostMapping("/create")
    @Operation(summary = "openapi.system.file.create.summary", description = "openapi.system.file.create.description")
    @RequirePermission("system:file:create")
    public Result<String> createFile(@Valid @RequestBody FileCreateReqVO createReqVO) {
        return Result.ok(frontendIdCodecSupport.encodeIdentifier(fileService.createFile(createReqVO)));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "openapi.system.file.delete.summary")
    @Parameter(name = "id", description = "openapi.common.id", required = true)
    @RequirePermission("system:file:delete")
    public Result<Boolean> deleteFile(@RequestParam("id") String id) {
        fileService.deleteFile(id);
        return Result.ok(true);
    }

    @DeleteMapping("/delete-batch")
    @Operation(summary = "openapi.system.file.delete_batch.summary")
    @RequirePermission("system:file:delete")
    public Result<Boolean> deleteFileList(@RequestParam("ids") List<String> ids) {
        fileService.deleteFileList(ids);
        return Result.ok(true);
    }

    @GetMapping("/get")
    @Operation(summary = "openapi.system.file.get.summary")
    @Parameter(name = "id", description = "openapi.common.id", required = true)
    @RequirePermission("system:file:query")
    public Result<FileRespVO> getFile(@RequestParam("id") String id) {
        return Result.ok(fileService.getFile(id));
    }

    @GetMapping("/page")
    @Operation(summary = "openapi.system.file.page.summary")
    @RequirePermission("system:file:query")
    public Result<PageResult<FileRespVO>> getFilePage(FilePageReqVO pageReqVO) {
        return Result.ok(fileService.getFilePage(pageReqVO));
    }

    @GetMapping("/types")
    @Operation(summary = "openapi.system.file.types.summary")
    @RequirePermission("system:file:query")
    public Result<List<String>> getFileTypes() {
        return Result.ok(fileService.getFileTypes());
    }

    @GetMapping("/{configId}/get/**")
    @Operation(summary = "openapi.system.file.get_content.summary")
    @Parameter(name = "configId", description = "openapi.system.file.file_config_id", required = true)
    public void getFileContent(HttpServletRequest request,
                               HttpServletResponse response,
                               @PathVariable("configId") String configId) throws java.io.IOException {
        String path = StrUtil.subAfter(request.getRequestURI(), "/get/", false);
        if (StrUtil.isEmpty(path)) {
            throw new ApiException(BusinessErrorCode.FILE_PATH_REQUIRED);
        }
        path = URLDecoder.decode(path, StandardCharsets.UTF_8);

        byte[] content = fileService.getFileContent(configId, path);
        if (content == null) {
            log.warn("[getFileContent][configId({}) path({}) 文件不存在]", configId, path);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        FileTypeUtils.writeAttachment(response, path, content);
    }

    @GetMapping("/download")
    @Operation(summary = "openapi.system.file.download.summary")
    @Parameter(name = "id", description = "openapi.system.file.file_id", required = true)
    @RequirePermission("system:file:download")
    public void downloadFile(@RequestParam("id") String id,
                             HttpServletResponse response) throws java.io.IOException {
        FileRespVO file = fileService.getFile(id);
        byte[] content = fileService.getFileContent(file.getConfigId(), file.getPath());
        if (content == null) {
            log.warn("[downloadFile][id({}) configId({}) path({}) 文件不存在]", id, file.getConfigId(), file.getPath());
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        String filename = StrUtil.isNotBlank(file.getName()) ? file.getName() : file.getPath();
        FileTypeUtils.writeAttachment(response, filename, content);
    }

    @GetMapping("/presigned-get-url")
    @Operation(summary = "openapi.system.file.presign_get_url.summary")
    public Result<String> presignGetUrl(
            @RequestParam(value = "configId", required = false) String configId,
            @RequestParam("url") String url,
            @RequestParam(value = "expirationSeconds", required = false) Integer expirationSeconds) {
        return Result.ok(fileService.presignGetUrl(configId, url, expirationSeconds));
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileController.class);
}
