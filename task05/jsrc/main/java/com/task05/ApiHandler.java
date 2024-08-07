package com.task05;

import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import lombok.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
        roleName = "api_handler-role",
        isPublishVersion = false,
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
        authType = AuthType.NONE
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "table", value = "Events")
})
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final int SC_OK = 201;
    private static final Gson GSON = new GsonBuilder().create();
    private static final Region REGION = Region.of(System.getenv("region"));
    private static final String TABLE_NAME = System.getenv("table");

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {

        DynamoDbTable<EventModel> eventTable = getTable(TABLE_NAME, EventModel.class);

        String requestBody = requestEvent.getBody();
        Request request = GSON.fromJson(requestBody, Request.class);

        EventModel event = EventModel.builder()
                .id(UUID.randomUUID().toString())
                .principalId(request.getPrincipalId())
                .createdAt(Instant.now().toString())
                .body(request.getContent())
                .build();

        eventTable.putItem(event);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(SC_OK)
                .withHeaders(Map.of("Content-Type", "application/json"))
                .withBody(GSON.toJson(Map.of(
                        "statusCode", SC_OK,
                        "event", event
                )));
    }

    private <T> DynamoDbTable<T> getTable(String tableName, Class<T> clazz) {
        DynamoDbClient ddb = DynamoDbClient.builder()
                .region(REGION)
                .build();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();
        return enhancedClient.table(tableName, TableSchema.fromBean(clazz));
    }

    @Data
    @NoArgsConstructor
    private class Request {
        private int principalId;
        private Map<String, String> content;
    }
}
