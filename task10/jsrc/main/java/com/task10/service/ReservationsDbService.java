package com.task10.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.task10.models.ReservationsModel;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;
import java.util.stream.Collectors;

public class ReservationsDbService {
    protected static final Region REGION = Region.of(System.getenv("region"));
    protected static final String RESERVATIONS_TABLE_NAME = System.getenv("reservations");

    protected static final Gson GSON = new GsonBuilder().create();

    private final DynamoDbTable<ReservationsModel> reservationsTable;

    public ReservationsDbService() {
        DynamoDbClient ddb = DynamoDbClient.builder()
                .region(REGION)
                .build();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();
        this.reservationsTable = enhancedClient.table(RESERVATIONS_TABLE_NAME, TableSchema.fromBean(ReservationsModel.class));
    }

    public List<ReservationsModel> scanTable() {
        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder().build();
        return reservationsTable.scan(scanRequest).items().stream().collect(Collectors.toList());
    }

    public String addItem(ReservationsModel item) {
        reservationsTable.putItem(item);
        return GSON.toJson(item);
    }

    public ReservationsModel getItemById(int id) {
        Key key = Key.builder()
                .partitionValue(id)
                .build();
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder()
                .key(key)
                .build();
        return reservationsTable.getItem(request);
    }
}
