package com.velox.module.system.id.database.diagnostic;

import java.util.List;
import com.velox.module.system.id.database.posture.model.DatabaseIdPosture;

public record DatabaseIdPlanReport(
        DatabaseIdPosture targetPosture,
        int managedTableCount,
        int identityDomainCount,
        int migrationCount,
        List<DatabaseIdPlanDiagnostic> diagnostics
) {

    public long warningCount() {
        return diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() == DatabaseIdDiagnosticSeverity.WARNING)
                .count();
    }

    public long errorCount() {
        return diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() == DatabaseIdDiagnosticSeverity.ERROR)
                .count();
    }
}
