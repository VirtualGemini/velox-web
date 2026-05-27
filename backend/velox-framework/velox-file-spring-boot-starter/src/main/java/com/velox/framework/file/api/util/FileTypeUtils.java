package com.velox.framework.file.api.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.velox.framework.file.common.message.FileCommonMessages;
import com.velox.framework.file.common.web.FileWebConstants;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class FileTypeUtils {

    private static final Logger log = LoggerFactory.getLogger(FileTypeUtils.class);

    private static final Tika TIKA = new Tika();

    private FileTypeUtils() {
    }

    public static String getMineType(byte[] data) {
        return TIKA.detect(data);
    }

    public static String getMineType(String name) {
        return TIKA.detect(name);
    }

    public static String getMineType(byte[] data, String name) {
        return TIKA.detect(data, name);
    }

    public static String getExtension(String mineType) {
        try {
            return MimeTypes.getDefaultMimeTypes().forName(mineType).getExtension();
        } catch (MimeTypeException e) {
            log.warn(FileCommonMessages.FILE_EXTENSION_RESOLVE_FAILED, mineType, e);
            return null;
        }
    }

    public static void writeAttachment(HttpServletResponse response, String filename, byte[] content) throws IOException {
        String mineType = getMineType(content, filename);
        response.setContentType(mineType);
        if (isImage(mineType)) {
            response.setHeader(FileWebConstants.CONTENT_DISPOSITION, FileWebConstants.INLINE_DISPOSITION + encodeUtf8(filename));
        } else {
            response.setHeader(FileWebConstants.CONTENT_DISPOSITION, FileWebConstants.ATTACHMENT_DISPOSITION + encodeUtf8(filename));
        }
        if (StrUtil.containsIgnoreCase(mineType, FileWebConstants.VIDEO_TOKEN)) {
            response.setHeader(FileWebConstants.ACCEPT_RANGES, FileWebConstants.BYTES);
            response.setHeader(FileWebConstants.CONTENT_LENGTH, String.valueOf(content.length));
        }
        IoUtil.write(response.getOutputStream(), false, content);
    }

    public static boolean isImage(String mineType) {
        return StrUtil.startWith(mineType, FileWebConstants.IMAGE_PREFIX);
    }

    private static String encodeUtf8(String str) {
        try {
            return URLEncoder.encode(str, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return str;
        }
    }
}
