package com.velox.module.system.autoconfigure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "com.velox.module.system")
@MapperScan({
        "com.velox.module.system.persistence",
        "com.velox.module.system.file.persistence"
})
public class SystemModulePackageScan {
}
