package com.velox.module.system.id.database;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "velox.id.database.governance")
public class SystemDatabaseIdGovernanceProperties {

    private boolean reportEnabled = true;
    private final List<DomainDeclaration> domains = new ArrayList<>();
    private final List<TableReferenceDeclaration> references = new ArrayList<>();
    private final List<String> ignoredTables = new ArrayList<>();

    public boolean isReportEnabled() {
        return reportEnabled;
    }

    public void setReportEnabled(boolean reportEnabled) {
        this.reportEnabled = reportEnabled;
    }

    public List<DomainDeclaration> getDomains() {
        return domains;
    }

    public List<TableReferenceDeclaration> getReferences() {
        return references;
    }

    public List<String> getIgnoredTables() {
        return ignoredTables;
    }

    public static class DomainDeclaration {

        private String table;
        private String idColumn;
        private String businessType;
        private boolean enabled = true;

        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }

        public String getIdColumn() {
            return idColumn;
        }

        public void setIdColumn(String idColumn) {
            this.idColumn = idColumn;
        }

        public String getBusinessType() {
            return businessType;
        }

        public void setBusinessType(String businessType) {
            this.businessType = businessType;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class TableReferenceDeclaration {

        private String table;
        private boolean enabled = true;
        private final List<ReferenceMappingDeclaration> mappings = new ArrayList<>();

        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<ReferenceMappingDeclaration> getMappings() {
            return mappings;
        }
    }

    public static class ReferenceMappingDeclaration {

        private String column;
        private String targetBusinessType;
        private String kind = "explicit";
        private boolean enabled = true;

        public String getColumn() {
            return column;
        }

        public void setColumn(String column) {
            this.column = column;
        }

        public String getTargetBusinessType() {
            return targetBusinessType;
        }

        public void setTargetBusinessType(String targetBusinessType) {
            this.targetBusinessType = targetBusinessType;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
