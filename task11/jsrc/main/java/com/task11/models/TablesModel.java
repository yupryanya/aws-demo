package com.task11.models;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamoDbBean
@Setter
@ToString
public class TablesModel {
    private int id;
    private long number;
    private int places;
    private boolean isVip;
    private Integer minOrder;

    @DynamoDbPartitionKey
    public int getId() {
        return id;
    }
}