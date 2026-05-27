package com.velox.module.system.file.controller;

import com.velox.common.result.PageResult;
import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
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

@Tag(name = "openapi.system.file_config.tag.name", description = "openapi.system.file_config.tag.description")
@RestController
@RequestMapping("/file-config")
@Validated
public class FileConfigController {

    private final FileConfigService fileConfigService;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public FileConfigController(
            FileConfigService fileConfigService,
            SystemFrontendIdCodecSupport frontendIdCodecSupport
    ) {
        this.fileConfigService = fileConfigService;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @PostMapping("/create")
    @Operation(summary = "openapi.system.file_config.create.summary")
    @RequirePermission("system:file-config:create")
    public Result<String> createFileConfig(@Valid @RequestBody FileConfigSaveReqVO createReqVO) {
        return Result.ok(frontendIdCodecSupport.encodeIdentifier(fileConfigService.createFileConfig(createReqVO)));
    }

    @PutMapping("/update")
    @Operation(summary = "openapi.system.file_config.update.summary")
    @RequirePermission("system:file-config:update")
    public Result<Boolean> updateFileConfig(@Valid @RequestBody FileConfigSaveReqVO updateReqVO) {
        fileConfigService.updateFileConfig(updateReqVO);
        return Result.ok(true);
    }

    @PutMapping("/update-master")
    @Operation(summary = "openapi.system.file_config.update_master.summary")
    @Parameter(name = "id", description = "openapi.common.id", required = true)
    @RequirePermission("system:file-config:update")
    public Result<Boolean> updateFileConfigMaster(@RequestParam("id") String id) {
        fileConfigService.updateFileConfigMaster(id);
        return Result.ok(true);
    }

    @PutMapping("/update-enabled")
    @Operation(summary = "openapi.system.file_config.update_enabled.summary")
    @Parameter(name = "id", description = "openapi.common.id", required = true)
    @Parameter(name = "enabled", description = "openapi.common.enabled", required = true)
    @RequirePermission("system:file-config:update")
    public Result<Boolean> updateFileConfigEnabled(@RequestParam("id") String id,
                                                    @RequestParam("enabled") Integer enabled) {
        fileConfigService.updateFileConfigEnabled(id, enabled);
        return Result.ok(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "openapi.system.file_config.delete.summary")
    @Parameter(name = "id", description = "openapi.common.id", required = true)
    @RequirePermission("system:file-config:delete")
    public Result<Boolean> deleteFileConfig(@RequestParam("id") String id) {
        fileConfigService.deleteFileConfig(id);
        return Result.ok(true);
    }

    @DeleteMapping("/delete-batch")
    @Operation(summary = "openapi.system.file_config.delete_batch.summary")
    @RequirePermission("system:file-config:delete")
    public Result<Boolean> deleteFileConfigList(@RequestParam("ids") List<String> ids) {
        fileConfigService.deleteFileConfigList(ids);
        return Result.ok(true);
    }

    @GetMapping("/get")
    @Operation(summary = "openapi.system.file_config.get.summary")
    @Parameter(name = "id", description = "openapi.common.id", required = true)
    @RequirePermission("system:file-config:query")
    public Result<FileConfigRespVO> getFileConfig(@RequestParam("id") String id) {
        return Result.ok(fileConfigService.getFileConfig(id));
    }

    @GetMapping("/page")
    @Operation(summary = "openapi.system.file_config.page.summary")
    @RequirePermission("system:file-config:query")
    public Result<PageResult<FileConfigRespVO>> getFileConfigPage(FileConfigPageReqVO pageReqVO) {
        return Result.ok(fileConfigService.getFileConfigPage(pageReqVO));
    }

    @GetMapping("/supported-storages")
    @Operation(summary = "openapi.system.file_config.supported_storages.summary")
    @RequirePermission("system:file-config:query")
    public Result<List<Integer>> getSupportedStorageTypes() {
        return Result.ok(fileConfigService.getSupportedStorageTypes());
    }

    @GetMapping("/test")
    @Operation(summary = "openapi.system.file_config.test.summary")
    @Parameter(name = "id", description = "openapi.common.id", required = true)
    @RequirePermission("system:file-config:query")
    public Result<String> testFileConfig(@RequestParam("id") String id) {
        return Result.ok(fileConfigService.testFileConfig(id));
    }
}
