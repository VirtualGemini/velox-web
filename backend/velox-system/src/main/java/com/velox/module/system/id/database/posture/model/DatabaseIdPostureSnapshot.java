package com.velox.module.system.id.database.posture.model;

public record DatabaseIdPostureSnapshot(
        DatabaseIdPosture posture,
        String sampleTable,
        String sampleValue
) {

    public boolean hasDetectedData() {
        return posture != DatabaseIdPosture.EMPTY;
    }

    public String describe() {
        if (sampleTable == null || sampleValue == null) {
            return posture.name().toLowerCase();
        }
        return posture.name().toLowerCase() + " (" + sampleTable + ".id=" + sampleValue + ")";
    }
}
