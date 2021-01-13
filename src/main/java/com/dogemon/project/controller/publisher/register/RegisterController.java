package com.dogemon.project.controller.publisher.register;

import com.dogemon.project.repository.topic.Topic;
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
public class RegisterController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterController.class);

    private final TopicRepository topicRepository;

    @PutMapping("/topic/register")
    ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        try {
            return getResponse(request);
        } catch (Exception e) {
            LOGGER.error("Registration failed for request {}", request, e);
            return ResponseEntity.status(500).body(new RegisterResponse(
                    false,
                    "Internal error"
            ));
        }
    }

    private ResponseEntity<RegisterResponse> getResponse(RegisterRequest request) {
        if (topicRepository.existsById(request.getTopic())) {
            return ResponseEntity.badRequest().body(RegisterResponse.getTopicAlreadyExistsResponse());
        }
        topicRepository.save(new Topic(request.getTopic(), request.getPublisherId()));
        return ResponseEntity.ok(RegisterResponse.getSuccessResponse());
    }
}
