package com.task09;

import com.google.gson.GsonBuilder;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

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
    private static final Region REGION = Region.of(System.getenv("region"));
    private static final String WEATHER_TABLE = System.getenv("table");

    @Override
    public String handleRequest(Object request, Context context) {
        OpenMeteoApi openMeteoApi = new OpenMeteoApi();
        String apiData = openMeteoApi.getWeatherForecast();

        DynamoDbTable<WeatherData> weatherTable = getTable(WEATHER_TABLE, WeatherData.class);

        Forecast forecast = new GsonBuilder().create().fromJson(apiData, Forecast.class);

        WeatherData weatherData = new WeatherData();
        weatherData.setForecast(forecast);

        weatherTable.putItem(weatherData);
        return "OK";
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
}
