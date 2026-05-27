package com.velox.module.system.id.database.diagnostic;

public record DatabaseIdPlanDiagnostic(
        DatabaseIdDiagnosticSeverity severity,
        String code,
        String location,
        String message
) {

    public String render() {
        return severity.name() + " [" + code + "] " + location + " - " + message;
    }
}
