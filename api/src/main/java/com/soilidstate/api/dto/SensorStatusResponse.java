package com.soilidstate.api.dto;

import lombok.Data;

@Data
public class SensorStatusResponse {
    private String sensorId;
    private String sensorType;
    private String sensorName;
    private Integer hubPort;
    private Double value;
    private String unit;
    private Integer channel;
    private boolean attached;
    private String status;
}
