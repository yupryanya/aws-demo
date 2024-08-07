package com.task09;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDbBean
@Setter
public class ForecastModel {
    private double elevation;
    private double generationtime_ms;
    private Hourly hourly;
    private HourlyUnits hourly_units;
    private double latitude;
    private double longitude;
    private String timezone;
    private String timezone_abbreviation;
    private int utc_offset_seconds;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @DynamoDbBean
    public static class Hourly {
        private List<Double> temperature_2m;
        private List<String> time;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @DynamoDbBean
    public static class HourlyUnits {
        private String temperature_2m;
        private String time;
    }
}
