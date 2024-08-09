package com.task11.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task11.service.TablesDbService;
import com.task11.models.TablesModel;

import java.util.List;
import java.util.Map;

public class GetTablesHandler extends AbstractHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final TablesDbService tablesDbService;

    public GetTablesHandler() {
        this.tablesDbService = new TablesDbService();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            List<TablesModel> items = tablesDbService.getAllTables();
            return createOkResponse(gson.toJson(Map.of("tables", items)));
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
}
