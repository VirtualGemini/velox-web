package com.velox.module.system.file.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.velox.common.result.PageResult;
import com.velox.common.result.Result;
import com.velox.module.system.file.service.FileConfigService;
import com.velox.module.system.file.vo.FileConfigPageReqVO;
import com.velox.module.system.file.vo.FileConfigRespVO;
import com.velox.module.system.file.vo.FileConfigSaveReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理后台 - 文件配置")
@RestController
@RequestMapping("/file-config")
@Validated
public class FileConfigController {

    private final FileConfigService fileConfigService;

    public FileConfigController(FileConfigService fileConfigService) {
        this.fileConfigService = fileConfigService;
    }

    @PostMapping("/create")
    @Operation(summary = "创建文件配置")
    @SaCheckPermission("system:file-config:create")
    public Result<String> createFileConfig(@Valid @RequestBody FileConfigSaveReqVO createReqVO) {
        return Result.ok(fileConfigService.createFileConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新文件配置")
    @SaCheckPermission("system:file-config:update")
    public Result<Boolean> updateFileConfig(@Valid @RequestBody FileConfigSaveReqVO updateReqVO) {
        fileConfigService.updateFileConfig(updateReqVO);
        return Result.ok(true);
    }

    @PutMapping("/update-master")
    @Operation(summary = "更新文件配置为 Master")
    @Parameter(name = "id", description = "编号", required = true)
    @SaCheckPermission("system:file-config:update")
    public Result<Boolean> updateFileConfigMaster(@RequestParam("id") String id) {
        fileConfigService.updateFileConfigMaster(id);
        return Result.ok(true);
    }

    @PutMapping("/update-enabled")
    @Operation(summary = "更新文件配置启用状态")
    @Parameter(name = "id", description = "编号", required = true)
    @Parameter(name = "enabled", description = "是否启用", required = true)
    @SaCheckPermission("system:file-config:update")
    public Result<Boolean> updateFileConfigEnabled(@RequestParam("id") String id,
                                                    @RequestParam("enabled") Integer enabled) {
        fileConfigService.updateFileConfigEnabled(id, enabled);
        return Result.ok(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文件配置")
    @Parameter(name = "id", description = "编号", required = true)
    @SaCheckPermission("system:file-config:delete")
    public Result<Boolean> deleteFileConfig(@RequestParam("id") String id) {
        fileConfigService.deleteFileConfig(id);
        return Result.ok(true);
    }

    @DeleteMapping("/delete-batch")
    @Operation(summary = "批量删除文件配置")
    @SaCheckPermission("system:file-config:delete")
    public Result<Boolean> deleteFileConfigList(@RequestParam("ids") List<String> ids) {
        fileConfigService.deleteFileConfigList(ids);
        return Result.ok(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文件配置")
    @Parameter(name = "id", description = "编号", required = true)
    @SaCheckPermission("system:file-config:query")
    public Result<FileConfigRespVO> getFileConfig(@RequestParam("id") String id) {
        return Result.ok(fileConfigService.getFileConfig(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得文件配置分页")
    @SaCheckPermission("system:file-config:query")
    public Result<PageResult<FileConfigRespVO>> getFileConfigPage(FileConfigPageReqVO pageReqVO) {
        return Result.ok(fileConfigService.getFileConfigPage(pageReqVO));
    }

    @GetMapping("/supported-storages")
    @Operation(summary = "获得当前环境支持的文件存储类型")
    @SaCheckPermission("system:file-config:query")
    public Result<List<Integer>> getSupportedStorageTypes() {
        return Result.ok(fileConfigService.getSupportedStorageTypes());
    }

    @GetMapping("/test")
    @Operation(summary = "测试文件配置是否正确")
    @Parameter(name = "id", description = "编号", required = true)
    @SaCheckPermission("system:file-config:query")
    public Result<String> testFileConfig(@RequestParam("id") String id) {
        return Result.ok(fileConfigService.testFileConfig(id));
    }
}
