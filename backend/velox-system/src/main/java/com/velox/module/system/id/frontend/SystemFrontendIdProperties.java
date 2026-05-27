package com.velox.module.system.id.frontend;

import com.velox.framework.id.common.prefix.IdPropertyPrefixes;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = IdPropertyPrefixes.ID_SNOWFLAKE)
public class SystemFrontendIdProperties {

    private boolean base62 = true;

    public boolean isBase62() {
        return base62;
    }

    public void setBase62(boolean base62) {
        this.base62 = base62;
    }
}
