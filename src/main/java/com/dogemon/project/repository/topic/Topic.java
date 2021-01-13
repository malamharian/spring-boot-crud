package com.dogemon.project.repository.topic;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Value
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class Topic {
    @Id String topic;
    String publisherId;
}
