package com.velox.module.system.file.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.common.result.PageResult;
import com.velox.common.result.Result;
import com.velox.framework.file.api.util.FileTypeUtils;
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

@Tag(name = "管理后台 - 文件管理")
@RestController
@RequestMapping("/file")
@Validated
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "模式一：后端上传文件")
    @SaCheckPermission("system:file:upload")
    public Result<String> uploadFile(@Valid FileUploadReqVO uploadReqVO) throws java.io.IOException {
        MultipartFile file = uploadReqVO.getFile();
        byte[] content = IoUtil.readBytes(file.getInputStream());
        return Result.ok(fileService.createFile(content, file.getOriginalFilename(),
                uploadReqVO.getDirectory(), file.getContentType()));
    }

    @GetMapping("/presigned-url")
    @Operation(summary = "获取文件预签名地址", description = "模式二：前端上传文件")
    public Result<FilePresignedUrlRespVO> presignPutUrl(
            @RequestParam("name") String name,
            @RequestParam(value = "directory", required = false) String directory) {
        return Result.ok(fileService.presignPutUrl(name, directory));
    }

    @PostMapping("/create")
    @Operation(summary = "创建文件", description = "模式二：前端上传文件后，创建文件记录")
    @SaCheckPermission("system:file:create")
    public Result<String> createFile(@Valid @RequestBody FileCreateReqVO createReqVO) {
        return Result.ok(fileService.createFile(createReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文件")
    @Parameter(name = "id", description = "编号", required = true)
    @SaCheckPermission("system:file:delete")
    public Result<Boolean> deleteFile(@RequestParam("id") String id) {
        fileService.deleteFile(id);
        return Result.ok(true);
    }

    @DeleteMapping("/delete-batch")
    @Operation(summary = "批量删除文件")
    @SaCheckPermission("system:file:delete")
    public Result<Boolean> deleteFileList(@RequestParam("ids") List<String> ids) {
        fileService.deleteFileList(ids);
        return Result.ok(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文件")
    @Parameter(name = "id", description = "编号", required = true)
    @SaCheckPermission("system:file:query")
    public Result<FileRespVO> getFile(@RequestParam("id") String id) {
        return Result.ok(fileService.getFile(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得文件分页")
    @SaCheckPermission("system:file:query")
    public Result<PageResult<FileRespVO>> getFilePage(FilePageReqVO pageReqVO) {
        return Result.ok(fileService.getFilePage(pageReqVO));
    }

    @GetMapping("/{configId}/get/**")
    @Operation(summary = "下载文件")
    @Parameter(name = "configId", description = "配置编号", required = true)
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

    @GetMapping("/presigned-get-url")
    @Operation(summary = "获取文件预签名读取地址")
    public Result<String> presignGetUrl(
            @RequestParam("url") String url,
            @RequestParam(value = "expirationSeconds", required = false) Integer expirationSeconds) {
        return Result.ok(fileService.presignGetUrl(url, expirationSeconds));
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileController.class);
}
