package com.dogemon.project.cache;

import lombok.Value;

@Value
public class MessageCacheKey {
    String topic;
    String subscriberId;
}
