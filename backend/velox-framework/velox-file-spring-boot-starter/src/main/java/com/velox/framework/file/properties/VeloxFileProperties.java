package com.velox.framework.file.properties;

import com.velox.framework.file.common.prefix.FilePropertyPrefixes;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = FilePropertyPrefixes.FILE)
public class VeloxFileProperties {

    /**
     * Whether the file capability starter is enabled.
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
