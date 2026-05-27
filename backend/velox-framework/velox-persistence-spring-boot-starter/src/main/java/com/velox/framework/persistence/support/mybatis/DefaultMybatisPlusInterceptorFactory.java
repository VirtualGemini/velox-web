package com.velox.framework.persistence.support.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.velox.framework.persistence.api.datasource.PersistenceContext;
import com.velox.framework.persistence.common.message.PersistenceCommonMessages;
import com.velox.framework.persistence.exception.PersistenceConfigException;

import java.util.Locale;
import java.util.Map;

public class DefaultMybatisPlusInterceptorFactory {

    private static final String MYSQL_DATABASE_TYPE = "mysql";
    private static final String POSTGRE_SQL_DATABASE_TYPE = "postgresql";
    private static final String DEFAULT_DATABASE_TYPE = "";

    private static final Map<String, DbType> DB_TYPES = Map.of(
            MYSQL_DATABASE_TYPE, DbType.MYSQL,
            POSTGRE_SQL_DATABASE_TYPE, DbType.POSTGRE_SQL
    );

    public MybatisPlusInterceptor create(PersistenceContext context) {
        if (context == null) {
            throw new PersistenceConfigException(PersistenceCommonMessages.PERSISTENCE_CONTEXT_MUST_NOT_BE_NULL);
        }
        DbType dbType = DB_TYPES.get(normalize(context.type()));
        if (dbType == null) {
            throw new PersistenceConfigException(PersistenceCommonMessages.UNSUPPORTED_MYBATIS_DB_TYPE.formatted(context.type()));
        }
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbType));
        return interceptor;
    }

    private String normalize(String type) {
        return type == null ? DEFAULT_DATABASE_TYPE : type.trim().toLowerCase(Locale.ROOT);
    }
}
