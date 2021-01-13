package com.dogemon.project.controller.subscriber.subscribe;

import com.dogemon.project.repository.subscription.Subscription;
import com.dogemon.project.repository.subscription.SubscriptionRepository;
import com.dogemon.project.repository.topic.TopicRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class SubscribeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscribeController.class);

    private final TopicRepository topicRepository;
    private final SubscriptionRepository subscriptionRepository;

    @PutMapping("/topic/subscribe")
    ResponseEntity<SubscribeResponse> subscribe(@RequestBody SubscribeRequest request) {
        try {
            return getResponse(request);
        } catch (Exception e) {
            LOGGER.error("Subscription failed for request {}", request, e);
            return ResponseEntity.status(500).body(new SubscribeResponse(
                    false, "Internal Error"
            ));
        }
    }

    private ResponseEntity<SubscribeResponse> getResponse(SubscribeRequest request) {
        ResponseEntity<SubscribeResponse> validationError = getValidationError(request);
        if (validationError != null) return validationError;

        subscriptionRepository.save(new Subscription(
                request.getTopic(),
                request.getSubscriberId()
        ));
        return ResponseEntity.ok(SubscribeResponse.getSuccessResponse());
    }

    private ResponseEntity<SubscribeResponse> getValidationError(SubscribeRequest request) {
        if (!topicRepository.existsById(request.getTopic())) {
            return ResponseEntity.badRequest().body(SubscribeResponse.getTopicNotFoundResponse());
        }
        if (subscriptionRepository.existsById(new Subscription.SubscriptionId(
                request.getTopic(), request.getSubscriberId()
        ))) {
            return ResponseEntity.badRequest().body(SubscribeResponse.getAlreadySubscribedResponse());
        }

        return null;
    }
}
