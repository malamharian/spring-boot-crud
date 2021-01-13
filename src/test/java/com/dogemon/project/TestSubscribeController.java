package com.dogemon.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dogemon.project.controller.subscriber.subscribe.SubscribeRequest;
import com.dogemon.project.controller.subscriber.subscribe.SubscribeResponse;
import com.dogemon.project.repository.topic.Topic;
import com.dogemon.project.repository.topic.TopicRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TestSubscribeController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void testSubscribe() throws Exception {
        SubscribeRequest subscribeRequest = SubscribeRequest.builder()
                .topic("topic1")
                .subscriberId("subscriber1")
                .build();

        // Test subscribing to a topic that doesn't exist, should fail
        this.mockMvc
                .perform(
                        put("/topic/subscribe")
                                .content(objectMapper.writeValueAsString(subscribeRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(SubscribeResponse.getTopicNotFoundResponse())));

        topicRepository.save(new Topic("topic1", "publisher1"));

        // Test subscribing to a valid topic, should succeed
        this.mockMvc
                .perform(
                        put("/topic/subscribe")
                                .content(objectMapper.writeValueAsString(subscribeRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(SubscribeResponse.getSuccessResponse())));

        // Test subscribing to an already subscribed topic, should fail
        this.mockMvc
                .perform(
                        put("/topic/subscribe")
                                .content(objectMapper.writeValueAsString(subscribeRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(SubscribeResponse.getAlreadySubscribedResponse())));

        subscribeRequest = SubscribeRequest.builder()
                .topic("topic1")
                .subscriberId("subscriber2")
                .build();

        // Test subscribing to an existing topic that already has subscribers as a new subscriber, should succeed
        this.mockMvc
                .perform(
                        put("/topic/subscribe")
                                .content(objectMapper.writeValueAsString(subscribeRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(SubscribeResponse.getSuccessResponse())));
    }
}
