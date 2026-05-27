package com.velox.framework.web.properties;

import com.velox.framework.web.common.prefix.VeloxWebPropertyPrefixes;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = VeloxWebPropertyPrefixes.VELOX)
public class VeloxProperties extends VeloxWebProperties {
}
