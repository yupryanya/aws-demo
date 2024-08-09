package com.task10.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task10.service.TablesDbService;
import com.task10.models.TablesModel;

import java.util.Map;

public class PostTablesHandler extends AbstractHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final TablesDbService tablesDbService;

    public PostTablesHandler() {
        this.tablesDbService = new TablesDbService();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            TablesModel newTable = gson.fromJson(request.getBody(), TablesModel.class);
            tablesDbService.addTable(newTable);
            return createOkResponse(gson.toJson(Map.of("id", newTable.getId())));
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
}
