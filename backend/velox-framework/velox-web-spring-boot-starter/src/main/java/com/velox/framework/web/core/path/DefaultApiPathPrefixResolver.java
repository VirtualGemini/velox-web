package com.velox.framework.web.core.path;

import com.velox.framework.web.api.path.ApiPathPrefixes;
import com.velox.framework.web.common.message.WebCommonMessages;
import com.velox.framework.web.exception.WebConfigException;
import com.velox.framework.web.properties.VeloxWebProperties;
import com.velox.framework.web.spi.path.ApiPathPrefixResolver;

public class DefaultApiPathPrefixResolver implements ApiPathPrefixResolver {

    @Override
    public String resolve(VeloxWebProperties properties) {
        if (properties == null) {
            throw new WebConfigException(WebCommonMessages.WEB_PROPERTIES_MUST_NOT_BE_NULL);
        }
        return ApiPathPrefixes.normalize(properties.getApiPrefix());
    }
}
