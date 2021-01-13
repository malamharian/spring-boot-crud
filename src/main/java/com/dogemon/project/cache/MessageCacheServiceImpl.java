package com.dogemon.project.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MessageCacheServiceImpl implements MessageCacheService {

    private static final int CAPACITY = 1000;

    private final Cache<MessageCacheKey, MessageCacheData> cache;

    public MessageCacheServiceImpl() {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(CAPACITY)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public MessageCacheData getMessageFromCache(MessageCacheKey messageCacheKey) {
        return cache.getIfPresent(messageCacheKey);
    }

    @Override
    public void deleteCache(MessageCacheKey messageCacheKey) {
        cache.invalidate(messageCacheKey);
    }

    @Override
    public void put(MessageCacheKey messageCacheKey, MessageCacheData messageCacheData) {
        cache.put(messageCacheKey, messageCacheData);
    }
}
