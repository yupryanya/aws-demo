package com.task11.models;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamoDbBean
@ToString
public class Reservation {
    private int tableNumber;
    private String clientName;
    private String phoneNumber;
    private String date;
    private String slotTimeStart;
    private String slotTimeEnd;
}
