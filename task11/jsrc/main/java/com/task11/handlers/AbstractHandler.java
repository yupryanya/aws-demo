package com.task11.handlers;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

public abstract class AbstractHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    protected static final Gson gson = new GsonBuilder().create();

    protected APIGatewayProxyResponseEvent createErrorResponse(String errorMessage) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withBody(gson.toJson(Map.of("message", errorMessage)));
    }

    protected APIGatewayProxyResponseEvent createOkResponse(String responseBody) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(responseBody);
    }

    protected APIGatewayProxyResponseEvent createNonImplementedResponse() {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(501)
                .withBody(gson.toJson(Map.of("message", "Endpoint is not implemented")));
    }
}