package com.dogemon.project.controller.subscriber.message;

import com.dogemon.project.cache.MessageCacheData;
import com.dogemon.project.cache.MessageCacheKey;
import com.dogemon.project.cache.MessageCacheServiceImpl;
import com.dogemon.project.controller.subscriber.message.ack.AckMessageRequest;
import com.dogemon.project.controller.subscriber.message.ack.AckMessageResponse;
import com.dogemon.project.controller.subscriber.message.get.GetMessageRequest;
import com.dogemon.project.controller.subscriber.message.get.GetMessageResponse;
import com.dogemon.project.repository.message.Message;
import com.dogemon.project.repository.message.MessageRepository;
import com.dogemon.project.repository.subscription.Subscription;
import com.dogemon.project.repository.subscription.SubscriptionRepository;
import com.dogemon.project.repository.topic.TopicRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@AllArgsConstructor
@RestController
public class MessageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageController.class);

    private final TopicRepository topicRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MessageRepository messageRepository;

    private final MessageCacheServiceImpl messageCacheService;

    @PostMapping("/message/get")
    ResponseEntity<GetMessageResponse> getMessage(@RequestBody GetMessageRequest request) {
        try {
            return getGetResponse(request);
        } catch (Exception e) {
            LOGGER.error("Failed getting message for request {}", request, e);
            return ResponseEntity.status(500).body(new GetMessageResponse(
                    null,
                    null,
                    false,
                    false,
                    "Internal error"
            ));
        }
    }

    private ResponseEntity<GetMessageResponse> getGetResponse(GetMessageRequest request) {
        ResponseEntity<GetMessageResponse> validationError = getGetMessageValidationError(request);
        if (validationError != null) return validationError;

        Optional<Message> message = getFromCache(request);
        if (message.isEmpty()) {
            return ResponseEntity.ok(GetMessageResponse.getNoMessageAvailableResponse());
        }

        return ResponseEntity.ok(GetMessageResponse.getSuccessResponse(message.get()));
    }

    private Optional<Message> getFromCache(GetMessageRequest request) {
        MessageCacheKey cacheKey = new MessageCacheKey(request.getTopic(), request.getSubscriberId());
        MessageCacheData messageFromCache = messageCacheService.getMessageFromCache(cacheKey);
        Optional<Message> message;
        if (messageFromCache != null) {
            message = Optional.ofNullable(messageFromCache.getMessage());
        } else {
            message = messageRepository.findTopByTopicAndSubscriberIdOrderByTimestampAsc(
                    request.getTopic(),
                    request.getSubscriberId()
            );
            if (message.isPresent()) {
                messageCacheService.put(cacheKey, new MessageCacheData(
                        message.get(),
                        true
                ));
            } else {
                messageCacheService.put(cacheKey, MessageCacheData.getEmptyMessage());
            }
        }
        return message;
    }

    private ResponseEntity<GetMessageResponse> getGetMessageValidationError(GetMessageRequest request) {
        if (!topicRepository.existsById(request.getTopic())) {
            return ResponseEntity.badRequest().body(GetMessageResponse.getTopicNotFoundResponse());
        }
        if (!subscriptionRepository.existsById(new Subscription.SubscriptionId(
                request.getTopic(), request.getSubscriberId()
        ))) {
            return ResponseEntity.badRequest().body(GetMessageResponse.getUnsubscribedResponse());
        }
        return null;
    }

    @PostMapping("/message/ack")
    ResponseEntity<AckMessageResponse> ackMessage(@RequestBody AckMessageRequest request) {
        try {
            return getAckResponse(request);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new AckMessageResponse(
                    false,
                    "Internal error"
            ));
        }
    }

    private ResponseEntity<AckMessageResponse> getAckResponse(AckMessageRequest request) {
        Optional<Message> message = messageRepository.findById(request.getMessageId());
        ResponseEntity<AckMessageResponse> validationError = getAckMessageValidationError(request, message);
        if (validationError != null) {
            return validationError;
        }

        messageRepository.deleteById(request.getMessageId());

        deleteCacheForDeletedMessage(message);

        return ResponseEntity.ok(AckMessageResponse.getSuccessResponse());
    }

    private void deleteCacheForDeletedMessage(Optional<Message> message) {
        message.ifPresent(value -> messageCacheService.deleteCache(new MessageCacheKey(
                value.getTopic(),
                value.getSubscriberId()
        )));
    }

    private ResponseEntity<AckMessageResponse> getAckMessageValidationError(AckMessageRequest request,
                                                                            Optional<Message> message) {
        if (message.isEmpty() || !message.get().getSubscriberId().equals(request.getSubscriberId())) {
            return ResponseEntity.badRequest().body(AckMessageResponse.getMessageNotFoundResponse());
        }
        return null;
    }
}
