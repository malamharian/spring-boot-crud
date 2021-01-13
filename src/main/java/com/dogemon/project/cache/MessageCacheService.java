package com.dogemon.project.cache;

public interface MessageCacheService {
    MessageCacheData getMessageFromCache(MessageCacheKey messageCacheKey);

    void deleteCache(MessageCacheKey messageCacheKey);

    void put(MessageCacheKey messageCacheKey, MessageCacheData messageCacheData);
}
