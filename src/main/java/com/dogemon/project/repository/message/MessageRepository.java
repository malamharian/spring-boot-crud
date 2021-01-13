package com.dogemon.project.repository.message;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findTopByTopicAndSubscriberIdOrderByTimestampAsc(String topic, String subscriberId);
}
