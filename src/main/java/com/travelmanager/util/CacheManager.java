package com.travelmanager.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory cache manager with TTL support
 */
public class CacheManager {
    
    private static CacheManager instance;
    private Map<String, CacheEntry> cache;
    
    private CacheManager() {
        this.cache = new ConcurrentHashMap<>();
    }
    
    public static synchronized CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }
        return instance;
    }
    
    /**
     * Store value in cache with TTL
     * @param key Cache key
     * @param value Value to cache
     * @param ttlSeconds Time to live in seconds
     */
    public void put(String key, Object value, int ttlSeconds) {
        long expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000L);
        cache.put(key, new CacheEntry(value, expiryTime));
    }
    
    /**
     * Get value from cache
     * @param key Cache key
     * @return Cached value or null if expired/not found
     */
    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }
        return entry.value;
    }
    
    /**
     * Invalidate specific cache entry
     */
    public void invalidate(String key) {
        cache.remove(key);
    }
    
    /**
     * Invalidate all cache entries matching prefix
     */
    public void invalidatePrefix(String prefix) {
        cache.keySet().removeIf(key -> key.startsWith(prefix));
    }
    
    /**
     * Clear all cache
     */
    public void invalidateAll() {
        cache.clear();
    }
    
    /**
     * Get cache size
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * Internal cache entry with expiry
     */
    private static class CacheEntry {
        Object value;
        long expiryTime;
        
        CacheEntry(Object value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
}
