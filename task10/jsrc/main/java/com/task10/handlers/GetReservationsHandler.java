package com.task10.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task10.service.ReservationsDbService;
import com.task10.models.Reservation;

import java.util.List;
import java.util.Map;

public class GetReservationsHandler extends AbstractHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final ReservationsDbService reservationDbService;

    public GetReservationsHandler() {
        this.reservationDbService = new ReservationsDbService();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            List<Reservation> reservations = reservationDbService.getAllReservations();
            return createOkResponse(gson.toJson(Map.of("reservations", reservations)));
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
}
