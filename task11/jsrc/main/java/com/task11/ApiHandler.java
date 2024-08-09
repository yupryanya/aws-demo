package com.task11;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.task11.handlers.*;
import com.task11.models.RouteKey;

import java.util.Map;

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
        @EnvironmentVariable(key = "tables", value = "Tables"),
        @EnvironmentVariable(key = "reservations", value = "Reservations"),
        @EnvironmentVariable(key = "booking_userpool", value = "simple-booking-userpool")
})

public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Map<RouteKey, RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>> handlersByRouteKey;
    private final RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> routeNotImplementedHandler;
    private final Map<String, String> headersForCors;

    public ApiHandler() {
        this.handlersByRouteKey = initHandlers();
        this.routeNotImplementedHandler = new RouteNotImplementedHandler();
        this.headersForCors = initHeadersForCors();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        return getHandler(request)
                .handleRequest(request, context)
                .withHeaders(headersForCors);
    }

    private RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> getHandler(APIGatewayProxyRequestEvent requestEvent) {
        return handlersByRouteKey.getOrDefault(getRouteKey(requestEvent), routeNotImplementedHandler);
    }

    private Map<RouteKey, RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>> initHandlers() {
        return Map.of(
                new RouteKey("POST", "/signup"), new PostSignUpHandler(),
                new RouteKey("POST", "/signin"), new PostSignInHandler(),
                new RouteKey("GET", "/tables"), new GetTablesHandler(),
                new RouteKey("POST", "/tables"), new PostTablesHandler(),
                new RouteKey("GET", "/tables/{id}"), new GetTableByIdHandler(),
                new RouteKey("GET", "/reservations"), new GetReservationsHandler(),
                new RouteKey("POST", "/reservations"), new PostReservationsHandler()
        );
    }

    private RouteKey getRouteKey(APIGatewayProxyRequestEvent requestEvent) {
        return new RouteKey(requestEvent.getHttpMethod(), requestEvent.getPath());
    }

    private Map<String, String> initHeadersForCors() {
        return Map.of(
                "Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token",
                "Access-Control-Allow-Origin", "*",
                "Access-Control-Allow-Methods", "*",
                "Accept-Version", "*"
        );
    }
}