package com.velox.module.system.auth.session;

import java.util.Collection;
import java.util.Map;

public interface UserSessionService {

    String STATUS_ONLINE = "1";
    String STATUS_OFFLINE = "2";

    void recordLogin(String userId, String sessionId, String tokenValue);

    void recordRequestActivity(String userId, String tokenValue);

    void recordLogout(String userId, String tokenValue);

    boolean isOnline(String userId);

    Map<String, String> resolveStatuses(Collection<String> userIds);
}
