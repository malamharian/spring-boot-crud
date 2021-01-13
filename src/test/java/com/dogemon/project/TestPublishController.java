package com.dogemon.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dogemon.project.cache.MessageCacheData;
import com.dogemon.project.cache.MessageCacheKey;
import com.dogemon.project.cache.MessageCacheService;
import com.dogemon.project.controller.publisher.publish.PublishRequest;
import com.dogemon.project.controller.publisher.publish.PublishResponse;
import com.dogemon.project.repository.message.MessageRepository;
import com.dogemon.project.repository.subscription.Subscription;
import com.dogemon.project.repository.subscription.SubscriptionRepository;
import com.dogemon.project.repository.topic.Topic;
import com.dogemon.project.repository.topic.TopicRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TestPublishController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageCacheService messageCacheService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testPublish() throws Exception {
        PublishRequest publishRequest = PublishRequest.builder()
                .topic("topic1")
                .message("message1")
                .publisherId("publisher1").build();

        // Test topic not found, should fail
        this.mockMvc
                .perform(
                        post("/message/publish")
                                .content(objectMapper.writeValueAsString(publishRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(PublishResponse.getTopicNotFoundResponse())));

        // Test publishing to an existing topic as a different publisher, should fail
        topicRepository.save(new Topic("topic1", "publisher2"));
        this.mockMvc
                .perform(
                        post("/message/publish")
                                .content(objectMapper.writeValueAsString(publishRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(PublishResponse.getUnauthorizedResponse())));

        // Test publishing to an existing topic as an authorized publisher, should succeed
        topicRepository.deleteById("topic1");
        topicRepository.save(new Topic("topic1", "publisher1"));
        topicRepository.save(new Topic("topic2", "publisher1"));
        subscriptionRepository.save(new Subscription("topic1", "subscriber1"));
        subscriptionRepository.save(new Subscription("topic1", "subscriber2"));
        subscriptionRepository.save(new Subscription("topic2", "subscriber3"));

        // Message shouldn't exist yet for all subscribers
        assertThat(messageRepository.findTopByTopicAndSubscriberIdOrderByTimestampAsc(
                "topic1", "subscriber1").isEmpty()).isTrue();
        assertThat(messageRepository.findTopByTopicAndSubscriberIdOrderByTimestampAsc(
                "topic1", "subscriber2").isEmpty()).isTrue();
        assertThat(messageRepository.findTopByTopicAndSubscriberIdOrderByTimestampAsc(
                "topic1", "subscriber3").isEmpty()).isTrue();

        // Testing cache invalidation
        MessageCacheData emptyCache = new MessageCacheData(null, false);
        MessageCacheKey cacheKey1 = new MessageCacheKey("topic1", "subscriber1");
        messageCacheService.put(cacheKey1, emptyCache);
        MessageCacheKey cacheKey2 = new MessageCacheKey("topic1", "subscriber2");
        messageCacheService.put(cacheKey2, emptyCache);
        MessageCacheKey cacheKey3 = new MessageCacheKey("topic1", "subscriber3");
        messageCacheService.put(cacheKey3, emptyCache);

        assertThat(messageCacheService.getMessageFromCache(cacheKey1)).isNotNull();
        assertThat(messageCacheService.getMessageFromCache(cacheKey2)).isNotNull();
        assertThat(messageCacheService.getMessageFromCache(cacheKey3)).isNotNull();

        this.mockMvc
                .perform(
                        post("/message/publish")
                                .content(objectMapper.writeValueAsString(publishRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(PublishResponse.getSuccessResponse())));

        // Message should exist for subscriber 1 & 2, but not for subscriber 3
        // because subscriber 3 is not subscribed to topic1
        assertThat(messageRepository.findTopByTopicAndSubscriberIdOrderByTimestampAsc(
                "topic1", "subscriber1").isPresent()).isTrue();
        assertThat(messageRepository.findTopByTopicAndSubscriberIdOrderByTimestampAsc(
                "topic1", "subscriber2").isPresent()).isTrue();
        assertThat(messageRepository.findTopByTopicAndSubscriberIdOrderByTimestampAsc(
                "topic1", "subscriber3").isEmpty()).isTrue();

        // These cache keys should be invalidated
        assertThat(messageCacheService.getMessageFromCache(cacheKey1)).isNull();
        assertThat(messageCacheService.getMessageFromCache(cacheKey2)).isNull();
        // This one should not be invalidated as it's in a different topic
        assertThat(messageCacheService.getMessageFromCache(cacheKey3)).isNotNull();
    }
}
