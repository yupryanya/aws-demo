package com.task10.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.task10.models.TablesModel;
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

public class TablesDbService {
    protected static final Region REGION = Region.of(System.getenv("region"));
    protected static final String TABLES_TABLE_NAME = System.getenv("tables");

    protected static final Gson GSON = new GsonBuilder().create();

    private final DynamoDbTable<TablesModel> tablesTable;

    public TablesDbService() {
        DynamoDbClient ddb = DynamoDbClient.builder()
                .region(REGION)
                .build();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();
        this.tablesTable = enhancedClient.table(TABLES_TABLE_NAME, TableSchema.fromBean(TablesModel.class));
    }

    public List<TablesModel> getAllTables() {
        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder().build();
        return tablesTable.scan(scanRequest).items().stream().collect(Collectors.toList());
    }

    public String addTable(TablesModel item) {
        tablesTable.putItem(item);
        return GSON.toJson(item);
    }

    public TablesModel getTableByNumber(int number) {
        Key key = Key.builder()
                .partitionValue(number)
                .build();
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder()
                .key(key)
                .build();
        return tablesTable.getItem(request);
    }

    public TablesModel getItemById(int id) {
        Key key = Key.builder()
                .partitionValue(id)
                .build();
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder()
                .key(key)
                .build();
        return tablesTable.getItem(request);
    }
}
