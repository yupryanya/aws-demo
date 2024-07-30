package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;

import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;

import java.util.LinkedHashMap;
import java.util.Map;


@LambdaHandler(lambdaName = "hello_world",
        roleName = "hello_world-role",
        isPublishVersion = false,
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)

@LambdaUrlConfig(
        authType = AuthType.NONE
)

public class HelloWorld implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final int SC_OK = 200;
    private static final int SC_BAD_REQUEST = 400;

    private final Gson gson = new GsonBuilder().create();

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
        String path = requestEvent.getRequestContext().getHttp().getPath();
        String method = requestEvent.getRequestContext().getHttp().getMethod();
        Map<String, Object> responseBody = new LinkedHashMap<>();

        if ("/hello".equals(path) && "GET".equals(method)) {
            responseBody.put("statusCode", SC_OK);
            responseBody.put("message", "Hello from Lambda");
        } else {
            String errorMessage = String.format(
                    "Bad request syntax or unsupported method. Request path: %s. HTTP method: %s",
                    path, method
            );
            responseBody.put("statusCode", SC_BAD_REQUEST);
            responseBody.put("message", errorMessage);
        }

        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode((int) responseBody.get("statusCode"))
                .withBody(gson.toJson(responseBody))
                .build();
    }
}
