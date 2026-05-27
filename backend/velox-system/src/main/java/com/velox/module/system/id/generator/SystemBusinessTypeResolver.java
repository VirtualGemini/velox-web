package com.velox.module.system.id.generator;

import com.baomidou.mybatisplus.annotation.TableName;
import com.velox.framework.id.exception.VeloxIdGeneratorException;
import com.velox.module.system.id.database.SystemDatabaseIdGovernanceProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.velox.module.system.id.database.internal.DatabaseIdInternals.normalizeName;

@Component
public class SystemBusinessTypeResolver {

    private final Map<String, String> businessTypesByTable;

    public SystemBusinessTypeResolver(SystemDatabaseIdGovernanceProperties governanceProperties) {
        Map<String, String> mappings = new LinkedHashMap<>();
        for (SystemDatabaseIdGovernanceProperties.DomainDeclaration declaration : governanceProperties.getDomains()) {
            if (!declaration.isEnabled()) {
                continue;
            }
            String tableName = normalizeName(declaration.getTable());
            if (tableName.isBlank()) {
                continue;
            }
            String businessType = normalizeBusinessType(declaration);
            if (businessType.isBlank()) {
                continue;
            }
            mappings.put(tableName, businessType);
        }
        this.businessTypesByTable = Map.copyOf(mappings);
    }

    public String resolveEntityBusinessType(Class<?> entityClass) {
        TableName tableName = entityClass.getAnnotation(TableName.class);
        if (tableName == null || !StringUtils.hasText(tableName.value())) {
            throw new VeloxIdGeneratorException(
                    "Entity " + entityClass.getName() + " must declare @TableName to participate in id governance"
            );
        }
        return resolveTableBusinessType(tableName.value());
    }

    public String resolveTableBusinessType(String tableName) {
        String businessType = businessTypesByTable.get(normalizeName(tableName));
        if (StringUtils.hasText(businessType)) {
            return businessType;
        }
        throw new VeloxIdGeneratorException(
                "Table " + tableName + " is not declared under velox.id.database.governance.domains"
        );
    }

    private String normalizeBusinessType(SystemDatabaseIdGovernanceProperties.DomainDeclaration declaration) {
        if (StringUtils.hasText(declaration.getBusinessType())) {
            return normalizeName(declaration.getBusinessType());
        }
        return normalizeName(declaration.getTable());
    }
}
