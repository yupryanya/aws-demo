package com.task10.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.task10.service.CognitoService;
import com.task10.models.SignUp;

import java.util.Map;

public class PostSignUpHandler extends AbstractHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    protected static final Gson gson = new GsonBuilder().create();
    private final CognitoService cognitoService;

    public PostSignUpHandler() {
        this.cognitoService = new CognitoService();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            SignUp signUp = gson.fromJson(request.getBody(), SignUp.class);
            cognitoService.cognitoSignUp(signUp);
            String idToken = cognitoService.confirmSignUp(signUp)
                    .authenticationResult()
                    .idToken();
            return createOkResponse(gson.toJson(Map.of("accessToken", idToken)));
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
}