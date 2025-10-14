package com.soilidstate.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Registered Sensor Entity
@Entity
@Table(name = "registered_sensors", indexes = {
        @Index(name = "idx_sensor_connection", columnList = "connection_id"),
        @Index(name = "idx_sensor_phidget_id", columnList = "phidget_sensor_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisteredSensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connection_id", nullable = false)
    private PhidgetConnection connection;

    @Column(name = "phidget_sensor_id", nullable = false)
    private String phidgetSensorId; // UUID from Phidget runtime

    @Column(name = "sensor_type", nullable = false)
    private String sensorType;

    @Column(name = "sensor_name", nullable = false)
    private String sensorName;

    @Column(name = "hub_port", nullable = false)
    private Integer hubPort;

    @Column(name = "channel_number", nullable = false)
    private Integer channel;

    @Column(name = "serial_number")
    private Integer serialNumber;

    @Column(name = "is_attached")
    private Boolean isAttached;

    @Column(name = "last_value")
    private Double lastValue;

    @Column(name = "unit")
    private String unit;

    @Column(name = "last_reading_at")
    private LocalDateTime lastReadingAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
