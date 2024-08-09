package com.task11.service;

import com.task11.models.TablesModel;
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

    public void addTable(TablesModel item) {
        tablesTable.putItem(item);
    }

    public TablesModel getTableByNumber(int number) {
        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder().build();
        List<TablesModel> items = tablesTable.scan(scanRequest).items().stream().collect(Collectors.toList());
        return items.stream()
                .filter(item -> item.getNumber() == number)
                .findFirst()
                .orElse(null);
    }

    public TablesModel getTableById(int id) {
        Key key = Key.builder()
                .partitionValue(id)
                .build();
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder()
                .key(key)
                .build();
        return tablesTable.getItem(request);
    }
}
