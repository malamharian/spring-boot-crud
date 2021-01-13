package com.dogemon.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dogemon.project.cache.MessageCacheKey;
import com.dogemon.project.cache.MessageCacheService;
import com.dogemon.project.controller.subscriber.message.ack.AckMessageRequest;
import com.dogemon.project.controller.subscriber.message.ack.AckMessageResponse;
import com.dogemon.project.controller.subscriber.message.get.GetMessageRequest;
import com.dogemon.project.controller.subscriber.message.get.GetMessageResponse;
import com.dogemon.project.repository.message.Message;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TestMessageController {

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
    ObjectMapper objectMapper;

    @Test
    public void testGetAndAck() throws Exception {
        GetMessageRequest getMessageRequest = GetMessageRequest.builder()
                .topic("topic1")
                .subscriberId("subscriber1").build();

        // Test topic not found, should fail
        this.mockMvc
                .perform(
                        post("/message/get")
                                .content(objectMapper.writeValueAsString(getMessageRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(GetMessageResponse.getTopicNotFoundResponse())));

        topicRepository.save(new Topic("topic1", "publisher1"));

        // Test subscriber not subscribed to topic, should fail
        this.mockMvc
                .perform(
                        post("/message/get")
                                .content(objectMapper.writeValueAsString(getMessageRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(GetMessageResponse.getUnsubscribedResponse())));

        subscriptionRepository.save(new Subscription("topic1", "subscriber1"));

        // Test valid subscription, but no more message inside
        this.mockMvc
                .perform(
                        post("/message/get")
                                .content(objectMapper.writeValueAsString(getMessageRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(GetMessageResponse.getNoMessageAvailableResponse())));

        Message savedMessage = Message.builder()
                .id(1)
                .message("message1")
                .subscriberId("subscriber1")
                .timestamp(System.currentTimeMillis())
                .topic("topic1").build();
        this.messageRepository.save(
                savedMessage
        );

        messageCacheService.deleteCache(new MessageCacheKey("topic1", "subscriber1"));
        // Test valid subscription with a message inside, should succeed returning message1
        this.mockMvc
                .perform(
                        post("/message/get")
                                .content(objectMapper.writeValueAsString(getMessageRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(GetMessageResponse.getSuccessResponse(
                                savedMessage
                        ))));

        subscriptionRepository.save(new Subscription("topic1", "subscriber2"));

        // Test valid subscription with a message inside the topic, but isn't readable for this subscriber
        getMessageRequest = GetMessageRequest.builder()
                .topic("topic1")
                .subscriberId("subscriber2").build();

        this.mockMvc
                .perform(
                        post("/message/get")
                                .content(objectMapper.writeValueAsString(getMessageRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(GetMessageResponse.getNoMessageAvailableResponse())));

        AckMessageRequest ackMessageRequest = AckMessageRequest.builder()
                .messageId(2)
                .subscriberId("subscriber1")
                .build();
        // Test ack message that doesn't exist, should fail
        this.mockMvc
                .perform(
                        post("/message/ack")
                                .content(objectMapper.writeValueAsString(ackMessageRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(AckMessageResponse.getMessageNotFoundResponse())));

        // Test ack a valid message that belongs to another subscriber
        // shouldn't be allowed and should fail
        ackMessageRequest = AckMessageRequest.builder()
                .messageId(1)
                .subscriberId("subscriber2")
                .build();
        this.mockMvc
                .perform(
                        post("/message/ack")
                                .content(objectMapper.writeValueAsString(ackMessageRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(AckMessageResponse.getMessageNotFoundResponse())));

        // Test ack a valid message belonging to the requesting subscriber, should succeed
        ackMessageRequest = AckMessageRequest.builder()
                .messageId(1)
                .subscriberId("subscriber1")
                .build();
        this.mockMvc
                .perform(
                        post("/message/ack")
                                .content(objectMapper.writeValueAsString(ackMessageRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(AckMessageResponse.getSuccessResponse())));

        // Test valid subscription with a message inside that has been ack'd, message should be gone
        this.mockMvc
                .perform(
                        post("/message/get")
                                .content(objectMapper.writeValueAsString(getMessageRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(GetMessageResponse.getNoMessageAvailableResponse())));

        testGetAndAckMultipleMessage();
    }

    private void testGetAndAckMultipleMessage() throws Exception {
        GetMessageRequest getMessageRequest;
        AckMessageRequest ackMessageRequest;
        // Test a subscriber having multiple message, after ack it should move on to the next message
        long currentTime = System.currentTimeMillis();
        Message message1 = Message.builder()
                .id(3)
                .message("message1")
                .subscriberId("subscriber1")
                .timestamp(currentTime)
                .topic("topic1").build();
        Message message2 = Message.builder()
                .id(2)
                .message("message2")
                .subscriberId("subscriber1")
                .timestamp(currentTime + 1)
                .topic("topic1").build();
        this.messageRepository.save(
                message2 // id should be 2
        );
        this.messageRepository.save(
                message1 // id should be 3, but should be on top according to timestamp rule
        );
        messageCacheService.deleteCache(new MessageCacheKey("topic1", "subscriber1"));

        getMessageRequest = GetMessageRequest.builder()
                .topic("topic1")
                .subscriberId("subscriber1").build();

        // Getting message with id 3
        this.mockMvc
                .perform(
                        post("/message/get")
                                .content(objectMapper.writeValueAsString(getMessageRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(GetMessageResponse.getSuccessResponse(message1))));

        // Ack message with id 2
        ackMessageRequest = AckMessageRequest.builder()
                .messageId(3)
                .subscriberId("subscriber1")
                .build();

        this.mockMvc
                .perform(
                        post("/message/ack")
                                .content(objectMapper.writeValueAsString(ackMessageRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(AckMessageResponse.getSuccessResponse())));

        // Should get next message with id 2
        this.mockMvc
                .perform(
                        post("/message/get")
                                .content(objectMapper.writeValueAsString(getMessageRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(GetMessageResponse.getSuccessResponse(message2))));
    }
}
