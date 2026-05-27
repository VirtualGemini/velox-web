package com.velox.common.exception;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * i18n 消息工具类
 */
public class MessageUtils {

    private static MessageSource messageSource;

    public MessageUtils(MessageSource messageSource) {
        MessageUtils.messageSource = messageSource;
    }

    /**
     * 获取 i18n 消息
     *
     * @param code 消息 Key
     * @param args 参数
     * @return 国际化消息，未找到时返回 null
     */
    public static String message(String code, Object... args) {
        if (messageSource == null) {
            return null;
        }
        try {
            return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return null;
        }
    }
}
