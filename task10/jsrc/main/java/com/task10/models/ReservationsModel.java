package com.task10.models;

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
    String reservationId;
    Reservation reservation;

    public ReservationsModel() {
        this.reservationId = UUID.randomUUID().toString();
    }

    @DynamoDbPartitionKey
    public String getReservationId() {
        return reservationId;
    }
}