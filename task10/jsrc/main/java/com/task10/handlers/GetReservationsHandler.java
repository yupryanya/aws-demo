package com.task10.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task10.service.ReservationsDbService;
import com.task10.models.Reservation;
import com.task10.models.ReservationsModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetReservationsHandler extends AbstractHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final ReservationsDbService dynamoDbService;

    public GetReservationsHandler() {
        this.dynamoDbService = new ReservationsDbService();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            List<ReservationsModel> items = dynamoDbService.scanTable();
            List<Reservation> reservations = items.stream()
                    .map(ReservationsModel::getReservation)
                    .collect(Collectors.toList());
            return createOkResponse(gson.toJson(Map.of("reservations", reservations)));
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
}
