package com.task10.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task10.models.TablesModel;
import com.task10.service.ReservationsDbService;
import com.task10.models.Reservation;
import com.task10.models.ReservationsModel;
import com.task10.service.TablesDbService;

import java.util.Arrays;
import java.util.Map;

public class PostReservationsHandler extends AbstractHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final ReservationsDbService reservationDbService;
    private final TablesDbService tablesDbService;

    public PostReservationsHandler() {
        this.tablesDbService = new TablesDbService();
        this.reservationDbService = new ReservationsDbService();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            Reservation reservationData = gson.fromJson(request.getBody(), Reservation.class);
            TablesModel tableItem = tablesDbService.getItemById(reservationData.getTableNumber());
            if (tableItem == null) {
            //    return createErrorResponse("Table not found");
            }
            if (reservationDbService.noTableOverlapping(reservationData)) {
                ReservationsModel newReservation = new ReservationsModel();
                newReservation.setReservation(reservationData);
                reservationDbService.addItem(newReservation);
                return createOkResponse(gson.toJson(Map.of("reservationId", newReservation.getId())));
            } else {
                return createErrorResponse("Table is already booked");
            }
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }
}