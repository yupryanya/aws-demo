package com.task05;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.Map;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@DynamoDbBean
public class Event {
    private String id;
    private String principalId;
    private String createdAt;
    private Map<String, String> body;

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
}