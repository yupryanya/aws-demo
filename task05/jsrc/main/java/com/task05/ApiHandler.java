package com.task05;

import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import lombok.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
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
import java.util.HashMap;
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

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        try {
            DynamoDbClient ddb = DynamoDbClient.builder()
                    .region(Region.of(System.getenv("region")))
                    .build();
            DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(ddb)
                    .build();

            String requestBody = requestEvent.getBody();
            Request request = GSON.fromJson(requestBody, Request.class);

            Event event = Event.builder()
                    .id(UUID.randomUUID().toString())
                    .principalId(request.getPrincipalId())
                    .createdAt(Instant.now().toString())
                    .body(request.getContent())
                    .build();

            String tableName = System.getenv("table");
            DynamoDbTable<Event> eventTable = enhancedClient.table(tableName, TableSchema.fromBean(Event.class));

            eventTable.putItem(event);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("statusCode", SC_OK);
            responseBody.put("event", event);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_OK)
                    .withHeaders(Map.of("Content-Type", "application/json"))
                    .withBody(GSON.toJson(responseBody));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("Internal Server Error");
        }
    }

    @Data
    @NoArgsConstructor
    private class Request {
        private int principalId;
        private Map<String, String> content;
    }
}
