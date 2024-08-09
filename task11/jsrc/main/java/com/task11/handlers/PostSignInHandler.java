package com.task11.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.task11.models.SignIn;
import com.task11.service.CognitoService;

import java.util.Map;

public class PostSignInHandler extends AbstractHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final CognitoService cognitoService;
    protected static final Gson gson = new GsonBuilder().create();

    public PostSignInHandler() {
        this.cognitoService = new CognitoService();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            SignIn signIn = gson.fromJson(request.getBody(), SignIn.class);
            String accessToken = cognitoService.cognitoSignIn(signIn.getEmail(), signIn.getPassword())
                    .authenticationResult()
                    .idToken();
            return createOkResponse(gson.toJson(Map.of("accessToken", accessToken)));
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
}