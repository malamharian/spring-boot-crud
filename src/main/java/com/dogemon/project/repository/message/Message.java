package com.dogemon.project.repository.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import javax.persistence.*;

@Entity
@Value
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Table(indexes = @Index(columnList = "timestamp"))
public class Message {
    @Id
    @GeneratedValue
    long id;
    String topic;
    String subscriberId;
    String message;
    long timestamp;
}
