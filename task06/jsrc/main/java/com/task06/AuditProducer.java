package com.task06;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.amazonaws.services.lambda.runtime.events.DynamodbEvent.*;

@LambdaHandler(lambdaName = "audit_producer",
        roleName = "audit_producer-role",
        isPublishVersion = false,
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DynamoDbTriggerEventSource(
        targetTable = "Configuration",
        batchSize = 10
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "table", value = "Audit")
})
public class AuditProducer implements RequestHandler<DynamodbEvent, String> {
    LambdaLogger logger;
    private final Region REGION = Region.of(System.getenv("region"));

    @Override
    public String handleRequest(DynamodbEvent event, Context context) {
        logger = context.getLogger();
        String auditTableName = System.getenv("table");
        DynamoDbClient dynamoDbClient = createDynamoDbClient(REGION);
        for (DynamodbStreamRecord record : event.getRecords()) {
            handleRecord(record, dynamoDbClient, auditTableName);
        }
        dynamoDbClient.close();
        return "OK";
    }

    private void handleRecord(DynamodbStreamRecord record, DynamoDbClient dynamoDbClient, String auditTableName) {
        String eventName = record.getEventName();
        if (eventName.equals("INSERT") || eventName.equals("MODIFY")) {
            if (record.getDynamodb().getOldImage() != null) {
                handleModify(record, dynamoDbClient, auditTableName);
            } else {
                handleInsert(record, dynamoDbClient, auditTableName);
            }
        }
    }

    private void handleInsert(DynamodbStreamRecord record, DynamoDbClient dynamoDbClient, String auditTableName) {
        Map<String, AttributeValue> item = createDefaultItem(record);

        String key = record.getDynamodb().getNewImage().get("key").getS();
        String value = record.getDynamodb().getNewImage().get("value").getN();

        Map<String, AttributeValue> newValue = new HashMap<>();
        newValue.put("key", AttributeValue.builder().s(key).build());
        newValue.put("value", AttributeValue.builder().n(value).build());
        item.put("newValue", AttributeValue.builder().m(newValue).build());

        putItem(dynamoDbClient, auditTableName, item);
    }

    private void handleModify(DynamodbStreamRecord record, DynamoDbClient dynamoDbClient, String auditTableName) {
        String oldValue = record.getDynamodb().getOldImage().get("value").getN();
        String newValue = record.getDynamodb().getNewImage().get("value").getN();

        Map<String, AttributeValue> item = createDefaultItem(record);
        item.put("updatedAttribute", AttributeValue.builder().s("value").build());
        item.put("oldValue", AttributeValue.builder().n(oldValue).build());
        item.put("newValue", AttributeValue.builder().n(newValue).build());
        putItem(dynamoDbClient, auditTableName, item);
    }


    private Map<String, AttributeValue> createDefaultItem(DynamodbStreamRecord record) {
        String uuid = UUID.randomUUID().toString();
        String creationTime = Instant.now().toString();
        String key = record.getDynamodb().getNewImage().get("key").getS();

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(uuid).build());
        item.put("itemKey", AttributeValue.builder().s(key).build());
        item.put("modificationTime", AttributeValue.builder().s(creationTime).build());

        return item;
    }

    private static DynamoDbClient createDynamoDbClient(Region region) {
        return DynamoDbClient.builder()
                .region(region)
                .build();
    }

    private static void putItem(DynamoDbClient dynamoDbClient, String tableName, Map<String, AttributeValue> item) {
        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
    }
}
