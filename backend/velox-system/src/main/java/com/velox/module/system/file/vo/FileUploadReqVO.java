package com.velox.module.system.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "openapi.system.file.upload_req.schema")
public class FileUploadReqVO {

    @Schema(description = "openapi.system.file.upload_req.file", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{validation.system.file.upload.file.not_null}")
    private MultipartFile file;

    @Schema(description = "openapi.system.file.upload_req.directory", example = "upload")
    private String directory;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }
}
