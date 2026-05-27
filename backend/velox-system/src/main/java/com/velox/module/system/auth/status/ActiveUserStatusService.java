package com.velox.module.system.auth.status;

import java.util.Collection;
import java.util.Map;

public interface ActiveUserStatusService {

    String STATUS_ONLINE = "1";
    String STATUS_OFFLINE = "2";

    default void recordRequestActivity(String userId) {
        recordRequestActivity(userId, null);
    }

    void recordRequestActivity(String userId, String tokenValue);

    default void recordLogin(String userId) {
        recordLogin(userId, null, null);
    }

    void recordLogin(String userId, String sessionId, String tokenValue);

    default void recordLogout(String userId) {
        recordLogout(userId, null);
    }

    void recordLogout(String userId, String tokenValue);

    boolean isOnline(String userId);

    Map<String, String> resolveStatuses(Collection<String> userIds);
}
