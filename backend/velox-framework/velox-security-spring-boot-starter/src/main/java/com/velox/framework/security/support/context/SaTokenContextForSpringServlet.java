package com.velox.framework.security.support.context;

import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.context.model.SaStorage;
import cn.dev33.satoken.servlet.model.SaRequestForServlet;
import cn.dev33.satoken.servlet.model.SaResponseForServlet;
import cn.dev33.satoken.servlet.model.SaStorageForServlet;
import cn.dev33.satoken.spring.pathmatch.SaPathPatternParserUtil;

public class SaTokenContextForSpringServlet implements SaTokenContext {

    @Override
    public SaRequest getRequest() {
        return new SaRequestForServlet(SecuritySpringMvcUtil.getRequest());
    }

    @Override
    public SaResponse getResponse() {
        return new SaResponseForServlet(SecuritySpringMvcUtil.getResponse());
    }

    @Override
    public SaStorage getStorage() {
        return new SaStorageForServlet(SecuritySpringMvcUtil.getRequest());
    }

    @Override
    public boolean matchPath(String pattern, String path) {
        return SaPathPatternParserUtil.match(pattern, path);
    }

    @Override
    public boolean isValid() {
        return SecuritySpringMvcUtil.isWeb();
    }
}
