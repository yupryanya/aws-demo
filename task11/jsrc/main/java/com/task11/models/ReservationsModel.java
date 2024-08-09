package com.task11.models;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
@DynamoDbBean
@Setter
@ToString
public class ReservationsModel {
    String id;
    Reservation reservation;

    public ReservationsModel() {
        this.id = UUID.randomUUID().toString();
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
}