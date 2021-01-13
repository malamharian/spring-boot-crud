package com.dogemon.project.controller.publisher.publish;

import com.dogemon.project.cache.MessageCacheData;
import com.dogemon.project.cache.MessageCacheKey;
import com.dogemon.project.cache.MessageCacheService;
import com.dogemon.project.repository.message.Message;
import com.dogemon.project.repository.message.MessageRepository;
import com.dogemon.project.repository.subscription.Subscription;
import com.dogemon.project.repository.subscription.SubscriptionRepository;
import com.dogemon.project.repository.topic.Topic;
import com.dogemon.project.repository.topic.TopicRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@RestController
public class PublishController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishController.class);

    private final TopicRepository topicRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MessageRepository messageRepository;
    private final MessageCacheService messageCacheService;

    @PostMapping("/message/publish")
    ResponseEntity<PublishResponse> publish(@Valid @RequestBody PublishRequest request,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return getResponseFromBindingResultErrors(bindingResult);
        }
        try {
            return getResponse(request);
        } catch (Exception e) {
            LOGGER.error("Failed when publishing message for request {}", request, e);
            return ResponseEntity.status(500).body(new PublishResponse(
                    false,
                    "Internal error"
            ));
        }
    }

    private ResponseEntity<PublishResponse> getResponseFromBindingResultErrors(BindingResult bindingResult) {
        StringBuilder sb = new StringBuilder();
        bindingResult.getFieldErrors().forEach(fe -> {
            sb.append(fe.getField()).append(": ")
                    .append(fe.getDefaultMessage())
                    .append(" | ");
        });
        return ResponseEntity.badRequest().body(
                new PublishResponse(
                        false,
                        sb.toString()
                )
        );
    }

    private ResponseEntity<PublishResponse> getResponse(@Valid PublishRequest request) {
        ResponseEntity<PublishResponse> validationError = getValidationError(request);
        if (validationError != null)
            return validationError;

        publishMessageToSubscribers(request);

        return ResponseEntity.ok(PublishResponse.getSuccessResponse());
    }

    private ResponseEntity<PublishResponse> getValidationError(PublishRequest request) {
        Optional<Topic> topic = topicRepository.findById(request.getTopic());
        if (topic.isEmpty()) {
            return ResponseEntity.badRequest().body(PublishResponse.getTopicNotFoundResponse());
        }
        if (!topic.get().getPublisherId().equals(request.getPublisherId())) {
            return ResponseEntity.badRequest().body(PublishResponse.getUnauthorizedResponse());
        }
        return null;
    }

    private void publishMessageToSubscribers(PublishRequest request) {
        Subscription subscriptionExample = Subscription.builder()
                .topic(request.getTopic()).build();
        List<Subscription> subscriptions = subscriptionRepository.findAll(
                Example.of(subscriptionExample)
        );

        long timestamp = System.currentTimeMillis();
        subscriptions.forEach(subscription -> {
            messageRepository.save(Message.builder()
                    .message(request.getMessage())
                    .subscriberId(subscription.getSubscriberId())
                    .topic(request.getTopic())
                    .timestamp(timestamp)
                    .build()
            );

            // Invalidate cache if required
            MessageCacheKey cacheKey = new MessageCacheKey(
                    subscription.getTopic(),
                    subscription.getSubscriberId()
            );
            MessageCacheData cachedData = messageCacheService.getMessageFromCache(cacheKey);
            if (cachedData != null && !cachedData.isHasMessage()) {
                messageCacheService.deleteCache(cacheKey);
            }
        });
    }
}
