package com.soilidstate.api.dto;

import lombok.Data;

@Data
public class SensorDataResponse {
    private String sensorId;
    private String sensorType;
    private String sensorName;
    private Integer hubPort;
    private Integer channel;
    private Double value;
    private String unit;
    private Long timestamp;
    private boolean attached;
}
