package com.velox.framework.web.api.path;

import org.springframework.util.StringUtils;

public final class ApiPathPrefixes {

    private static final String DEFAULT_PREFIX = "";
    private static final String PATH_SEPARATOR = "/";

    private ApiPathPrefixes() {
    }

    public static String normalize(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return DEFAULT_PREFIX;
        }
        String normalized = prefix.trim();
        if (PATH_SEPARATOR.equals(normalized)) {
            return DEFAULT_PREFIX;
        }
        if (!normalized.startsWith(PATH_SEPARATOR)) {
            normalized = PATH_SEPARATOR + normalized;
        }
        if (normalized.endsWith(PATH_SEPARATOR)) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    public static String prepend(String prefix, String path) {
        String normalizedPrefix = normalize(prefix);
        if (!StringUtils.hasText(path)) {
            return normalizedPrefix;
        }
        String normalizedPath = path.startsWith(PATH_SEPARATOR) ? path : PATH_SEPARATOR + path;
        return normalizedPrefix + normalizedPath;
    }
}
