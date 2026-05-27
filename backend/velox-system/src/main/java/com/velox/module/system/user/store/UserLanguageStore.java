package com.velox.module.system.user.store;

import java.util.Optional;

/**
 * 用户语言偏好存储。
 * <p>
 * 实现类负责按 userId 持久化用户选择的语言（如 "zh" / "en"）。
 * Redis 实现持久化到 Redis，无 TTL；In-Memory 实现仅在进程内有效。
 */
public interface UserLanguageStore {

    /**
     * 保存指定用户的语言偏好。
     */
    void save(String userId, String language);

    /**
     * 读取指定用户的语言偏好，未设置时返回 empty。
     */
    Optional<String> find(String userId);
}
