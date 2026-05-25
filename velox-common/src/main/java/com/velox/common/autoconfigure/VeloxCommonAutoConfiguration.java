package com.velox.common.autoconfigure;

import com.velox.common.exception.MessageUtils;
import com.velox.common.web.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;

@AutoConfiguration
public class VeloxCommonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MessageSource.class)
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("i18n/messages");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        // 关闭 useCodeAsDefaultMessage：缺 key 时抛 NoSuchMessageException，
        // 由 MessageUtils 捕获后返回 null，让 ApiException 回落到 ErrorCode.message() 的 Java 默认值，
        // 避免日志/响应中暴露 "BusinessErrorCode.XXX" 这类 key 名。
        messageSource.setUseCodeAsDefaultMessage(false);
        return messageSource;
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageUtils messageUtils(MessageSource messageSource) {
        return new MessageUtils(messageSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
