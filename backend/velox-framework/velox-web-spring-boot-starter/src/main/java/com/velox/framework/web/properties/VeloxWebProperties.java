package com.velox.framework.web.properties;

import java.util.ArrayList;
import java.util.List;

public class VeloxWebProperties {

    private static final String DEFAULT_APPLICATION_NAME = "velox";
    private static final String DEFAULT_APPLICATION_VERSION = "1.0.0";
    private static final String DEFAULT_API_PREFIX = "/api";
    private static final List<String> DEFAULT_ALLOWED_METHODS =
            List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
    private static final List<String> DEFAULT_ALLOWED_HEADERS = List.of("*");
    private static final List<String> DEFAULT_EXPOSED_HEADERS = List.of("Authorization", "X-Trace-Id");

    private String name = DEFAULT_APPLICATION_NAME;

    private String version = DEFAULT_APPLICATION_VERSION;

    private String apiPrefix = DEFAULT_API_PREFIX;

    private final Web web = new Web();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getApiPrefix() {
        return apiPrefix;
    }

    public void setApiPrefix(String apiPrefix) {
        this.apiPrefix = apiPrefix;
    }

    public Web getWeb() {
        return web;
    }

    public static class Web {

        private final Cors cors = new Cors();

        public Cors getCors() {
            return cors;
        }
    }

    public static class Cors {

        private List<String> allowedOriginPatterns = new ArrayList<>();
        private List<String> allowedMethods = DEFAULT_ALLOWED_METHODS;
        private List<String> allowedHeaders = DEFAULT_ALLOWED_HEADERS;
        private List<String> exposedHeaders = DEFAULT_EXPOSED_HEADERS;
        private boolean allowCredentials = true;
        private long maxAge = 3600;

        public List<String> getAllowedOriginPatterns() {
            return allowedOriginPatterns;
        }

        public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
            this.allowedOriginPatterns = allowedOriginPatterns;
        }

        public List<String> getAllowedMethods() {
            return allowedMethods;
        }

        public void setAllowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        public void setAllowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }

        public List<String> getExposedHeaders() {
            return exposedHeaders;
        }

        public void setExposedHeaders(List<String> exposedHeaders) {
            this.exposedHeaders = exposedHeaders;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }
    }
}
