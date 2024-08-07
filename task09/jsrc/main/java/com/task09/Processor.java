package com.task09;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import software.amazon.awssdk.regions.Region;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "processor",
        roleName = "processor-role",
        isPublishVersion = false,
        tracingMode = TracingMode.Active,
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
        authType = AuthType.NONE
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "table", value = "Weather")
})
public class Processor implements RequestHandler<Object, String> {
    LambdaLogger logger;
    private static final Region REGION = Region.of(System.getenv("region"));
    private static final String WEATHER_TABLE = System.getenv("table");

    public String handleRequest(Object request, Context context) {
        logger = context.getLogger();

        DynamoDbClient dbc = createDynamoDbClient(REGION);
        OpenMeteoApi client = new OpenMeteoApi();
        String forecast = client.getWeatherForecast();
        Map<String, AttributeValue> weatherItem = createForecastItem(dbc, WEATHER_TABLE, forecast);
        putItem(dbc, WEATHER_TABLE, weatherItem);

        return "OK";
    }

    private Map<String, AttributeValue> createForecastItem(DynamoDbClient dbc, String table, String forecast) {
        String uuid = UUID.randomUUID().toString();
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(uuid).build());
        item.put("forecast", AttributeValue.builder().s(forecast).build());

        return item;
    }

    private static DynamoDbClient createDynamoDbClient(Region region) {
        return DynamoDbClient.builder()
                .region(region)
                .build();
    }

    private static void putItem(DynamoDbClient ddbc, String tableName, Map<String, AttributeValue> item) {
        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        ddbc.putItem(request);
    }
}
