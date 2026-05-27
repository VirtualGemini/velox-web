package com.velox.module.system.file.provider.db;

import com.velox.framework.file.spi.client.FileClientTypeRegistration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DbFileClientTypeRegistrationConfiguration {

    @Bean
    public FileClientTypeRegistration dbFileClientTypeRegistration(ApplicationContext applicationContext) {
        return new FileClientTypeRegistration(
                1,
                DbFileClientConfig.class,
                (configId, config) -> new DbFileClient(configId, (DbFileClientConfig) config, applicationContext)
        );
    }
}
