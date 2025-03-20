package com.huydevcorn.todo_app.utils;

/**
 * Utility class for Redis operations.
 */
public class RedisUtils {
    /**
     * Concatenates a prefix with a key.
     *
     * @param prefix the prefix to be added
     * @param key the key to be prefixed
     * @return the concatenated string with prefix and key
     */
    public static String withPrefix(String prefix, String key) {
        return prefix + key;
    }
}
