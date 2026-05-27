package com.velox.framework.id.common.type;

import java.util.Locale;

public final class IdDatabaseInitModes {

    public static final String NONE = "none";
    public static final String UPDATE = "update";

    private IdDatabaseInitModes() {
    }

    public static String normalize(String initMode) {
        if (initMode == null) {
            return NONE;
        }
        return initMode.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isSupported(String initMode) {
        String normalized = normalize(initMode);
        return NONE.equals(normalized) || UPDATE.equals(normalized);
    }
}
