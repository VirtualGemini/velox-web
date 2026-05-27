package com.velox.framework.web.spi.path;

import com.velox.framework.web.properties.VeloxWebProperties;

public interface ApiPathPrefixResolver {

    String resolve(VeloxWebProperties properties);
}
