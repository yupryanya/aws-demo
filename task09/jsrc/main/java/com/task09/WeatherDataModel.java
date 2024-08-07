package com.task09;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.UUID;

@Data
@ToString
@DynamoDbBean
@AllArgsConstructor
public class WeatherDataModel {
    private String id;
    private ForecastModel forecastModel;

    public WeatherDataModel() {
        this.id = UUID.randomUUID().toString();
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
}