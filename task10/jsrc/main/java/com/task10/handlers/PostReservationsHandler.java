package com.task10.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task10.service.ReservationsDbService;
import com.task10.models.Reservation;
import com.task10.models.ReservationsModel;

import java.util.Map;

public class PostReservationsHandler extends AbstractHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final ReservationsDbService dynamoDbService;

    public PostReservationsHandler() {
        this.dynamoDbService = new ReservationsDbService();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            ReservationsModel newReservation = new ReservationsModel();
            Reservation reservationData = gson.fromJson(request.getBody(), Reservation.class);
            newReservation.setReservation(reservationData);
            dynamoDbService.addItem(newReservation);
            return createOkResponse(gson.toJson(Map.of("reservationId", newReservation.getReservationId())));
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
}