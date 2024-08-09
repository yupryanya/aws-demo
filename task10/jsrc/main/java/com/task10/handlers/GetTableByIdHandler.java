package com.task10.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task10.service.TablesDbService;
import com.task10.models.TablesModel;

public class GetTableByIdHandler extends AbstractHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final TablesDbService tablesDbService;

    public GetTableByIdHandler() {
        this.tablesDbService = new TablesDbService();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            String tableId = request.getPathParameters().get("tableId");
            TablesModel tableItem = tablesDbService.getItemById(Integer.parseInt(tableId));
            if (tableItem != null) {
                return createOkResponse(gson.toJson(tableItem));
            } else {
                return createErrorResponse("Table not found");
            }
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
}
