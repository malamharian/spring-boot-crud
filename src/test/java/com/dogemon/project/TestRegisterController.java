package com.dogemon.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dogemon.project.controller.publisher.register.RegisterRequest;
import com.dogemon.project.controller.publisher.register.RegisterResponse;
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
public class TestRegisterController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void testRegister() throws Exception {
        // Test registering the same topic twice, first should be successful, second should fail
        RegisterRequest registerRequest = RegisterRequest.builder().publisherId("publisher1").topic("topic1").build();
        this.mockMvc
                .perform(
                        put("/topic/register")
                                .content(objectMapper.writeValueAsString(registerRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(RegisterResponse.getSuccessResponse())));
        this.mockMvc.perform(
                put("/topic/register")
                        .content(objectMapper.writeValueAsString(registerRequest))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(RegisterResponse.getTopicAlreadyExistsResponse())));

        // Test registering a new topic, should be successful
        registerRequest = RegisterRequest.builder().publisherId("publisher1").topic("topic2").build();
        this.mockMvc.perform(
                put("/topic/register")
                        .content(objectMapper.writeValueAsString(registerRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(RegisterResponse.getSuccessResponse())));
    }
}
