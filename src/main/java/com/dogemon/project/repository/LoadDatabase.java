package com.dogemon.project.repository;

import com.dogemon.project.repository.subscription.SubscriptionRepository;
import com.dogemon.project.repository.topic.TopicRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadDatabase.class);

    // Can be used for initialization if required
    @Bean
    CommandLineRunner initRepository(TopicRepository topicRepository,
                                     SubscriptionRepository subscriptionRepository) {
        return args -> {
//            LOGGER.info("Adding topic " + topicRepository.save(new Topic("topic1", "publisher1")));
//            LOGGER.info("Adding topic " + topicRepository.save(new Topic("topic2", "publisher1")));
//            LOGGER.info("Adding topic " + topicRepository.save(new Topic("topic3", "publisher2")));
//            LOGGER.info("Adding subscription " + subscriptionRepository.save(new Subscription("topic1", "subscriber1")));
//            LOGGER.info("Adding subscription " + subscriptionRepository.save(new Subscription("topic1", "subscriber2")));
//            LOGGER.info("Adding subscription " + subscriptionRepository.save(new Subscription("topic3", "subscriber3")));
        };
    }
}
