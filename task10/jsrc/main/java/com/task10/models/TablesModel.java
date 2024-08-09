package com.task10.models;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.concurrent.atomic.AtomicInteger;

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

//    public TablesModel() {
//        this.id = IdGenerator.getNextId();
//    }

    @DynamoDbPartitionKey
    public int getId() {
        return id;
    }

//    private static class IdGenerator {
//        private static final AtomicInteger counter = new AtomicInteger(0);
//
//        public static int getNextId() {
//            return counter.incrementAndGet();
//        }
//    }
}