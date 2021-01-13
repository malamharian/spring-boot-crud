package com.dogemon.project.cache;

import com.dogemon.project.repository.message.Message;
import lombok.Value;

@Value
public class MessageCacheData {
    Message message;
    boolean hasMessage;

    public static MessageCacheData getEmptyMessage() {
        return new MessageCacheData(null, false);
    }
}
