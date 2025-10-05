package com.soilidstate.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SensorRegistrationRequest {
    @NotBlank(message = "Sensor type is required")
    private String sensorType; // VOLTAGE, TEMPERATURE, HUMIDITY, etc.

    @NotNull(message = "Hub port is required")
    private Integer hubPort;

    @NotNull(message = "Channel is required")
    private Integer channel;

    private Integer serialNumber;

    private String sensorName;
}