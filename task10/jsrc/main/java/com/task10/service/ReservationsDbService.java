package com.task10.service;

import com.task10.models.Reservation;
import com.task10.models.ReservationsModel;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;
import java.util.stream.Collectors;

public class ReservationsDbService {
    protected static final Region REGION = Region.of(System.getenv("region"));
    protected static final String RESERVATIONS_TABLE_NAME = System.getenv("reservations");

    private final DynamoDbTable<ReservationsModel> reservationsTable;

    public ReservationsDbService() {
        DynamoDbClient ddb = DynamoDbClient.builder()
                .region(REGION)
                .build();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();
        this.reservationsTable = enhancedClient.table(RESERVATIONS_TABLE_NAME, TableSchema.fromBean(ReservationsModel.class));
    }

    public List<Reservation> scanTable() {
        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder().build();
        List<ReservationsModel> items = reservationsTable.scan(scanRequest).items().stream().collect(Collectors.toList());
        return items.stream()
                .map(ReservationsModel::getReservation)
                .collect(Collectors.toList());
    }

    public void addItem(ReservationsModel item) {
        reservationsTable.putItem(item);
    }

    public boolean noTableOverlapping(Reservation newReservation) {
        List<Reservation> reservations = scanTable();
        for (Reservation reservation : reservations) {
            if (reservation.getTableNumber() == newReservation.getTableNumber()) {
                if (isTimeOverlap(newReservation.getSlotTimeStart(), newReservation.getSlotTimeEnd(),
                        reservation.getSlotTimeStart(), reservation.getSlotTimeEnd())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isTimeOverlap(String start1, String end1, String start2, String end2) {
        return (start1.compareTo(end2) < 0 && end1.compareTo(start2) > 0);
    }
}
