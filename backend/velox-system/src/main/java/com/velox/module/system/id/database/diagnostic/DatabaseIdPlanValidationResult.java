package com.velox.module.system.id.database.diagnostic;

import java.util.List;
import java.util.stream.Collectors;

public record DatabaseIdPlanValidationResult(
        List<DatabaseIdPlanDiagnostic> diagnostics
) {

    public boolean hasErrors() {
        return diagnostics.stream().anyMatch(diagnostic -> diagnostic.severity() == DatabaseIdDiagnosticSeverity.ERROR);
    }

    public List<DatabaseIdPlanDiagnostic> errors() {
        return diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() == DatabaseIdDiagnosticSeverity.ERROR)
                .toList();
    }

    public List<DatabaseIdPlanDiagnostic> warnings() {
        return diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() == DatabaseIdDiagnosticSeverity.WARNING)
                .toList();
    }

    public String renderErrors() {
        return errors().stream()
                .map(DatabaseIdPlanDiagnostic::render)
                .collect(Collectors.joining("; "));
    }
}
