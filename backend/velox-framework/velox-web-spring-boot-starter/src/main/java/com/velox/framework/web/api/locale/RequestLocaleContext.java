package com.velox.framework.web.api.locale;

import org.springframework.util.StringUtils;

import java.util.Locale;

public final class RequestLocaleContext {

    public static final String HEADER_NAME = "Accept-Language";

    private static final Locale DEFAULT_LOCALE = Locale.SIMPLIFIED_CHINESE;

    private static final ThreadLocal<Locale> CONTEXT = new ThreadLocal<>();

    private RequestLocaleContext() {
    }

    public static Locale getCurrentLocale() {
        Locale locale = CONTEXT.get();
        return locale == null ? DEFAULT_LOCALE : locale;
    }

    public static Locale getDefaultLocale() {
        return DEFAULT_LOCALE;
    }

    public static void setCurrentLocale(String languageTag) {
        CONTEXT.set(resolveLocale(languageTag));
    }

    public static void setCurrentLocale(Locale locale) {
        CONTEXT.set(locale == null ? DEFAULT_LOCALE : locale);
    }

    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 将 Accept-Language 中的语言标签映射为受支持的 Locale。
     * 支持以 zh / en 为前缀的常见标签；未识别时返回默认 Locale。
     */
    public static Locale resolveLocale(String languageTag) {
        if (!StringUtils.hasText(languageTag)) {
            return DEFAULT_LOCALE;
        }
        // 处理形如 "zh-CN,zh;q=0.9,en;q=0.8" 的复合头，仅取第一段
        String primary = languageTag.split(",")[0].trim();
        if (primary.isEmpty()) {
            return DEFAULT_LOCALE;
        }
        String lower = primary.toLowerCase(Locale.ROOT);
        if (lower.startsWith("zh")) {
            return Locale.SIMPLIFIED_CHINESE;
        }
        if (lower.startsWith("en")) {
            return Locale.ENGLISH;
        }
        return DEFAULT_LOCALE;
    }
}
