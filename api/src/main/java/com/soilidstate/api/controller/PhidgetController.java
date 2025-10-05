package com.soilidstate.api.controller;

import com.phidget22.PhidgetException;
import com.soilidstate.api.dto.*;
import com.soilidstate.api.service.PhidgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/phidget")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PhidgetController {

    private final PhidgetService phidgetService;

    @PostMapping("/connect")
    public ResponseEntity<?> connect(@Valid @RequestBody ConnectionRequest request) {
        try {
            ConnectionStatusResponse response = phidgetService.connect(request);
            return ResponseEntity.ok(response);
        } catch (PhidgetException e) {
            log.error("Connection failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("CONNECTION_FAILED", e.getMessage()));
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect() {
        try {
            phidgetService.disconnect();
            return ResponseEntity.ok(Map.of("message", "Disconnected successfully"));
        } catch (PhidgetException e) {
            log.error("Disconnect failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("DISCONNECT_FAILED", e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<ConnectionStatusResponse> getStatus() {
        return ResponseEntity.ok(phidgetService.getConnectionStatus());
    }

    @PostMapping("/sensors/register")
    public ResponseEntity<?> registerSensor(@Valid @RequestBody SensorRegistrationRequest request) {
        try {
            SensorStatusResponse response = phidgetService.registerSensor(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("NOT_CONNECTED", e.getMessage()));
        } catch (PhidgetException e) {
            log.error("Sensor registration failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("REGISTRATION_FAILED", e.getMessage()));
        }
    }

    @DeleteMapping("/sensors/{sensorId}")
    public ResponseEntity<?> unregisterSensor(@PathVariable String sensorId) {
        try {
            phidgetService.unregisterSensor(sensorId);
            return ResponseEntity.ok(Map.of("message", "Sensor unregistered successfully"));
        } catch (PhidgetException e) {
            log.error("Sensor unregistration failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("UNREGISTRATION_FAILED", e.getMessage()));
        }
    }

    @GetMapping("/sensors")
    public ResponseEntity<?> getAllSensors() {
        try {
            Map<String, SensorStatusResponse> sensors = phidgetService.getAllSensors();
            return ResponseEntity.ok(sensors);
        } catch (PhidgetException e) {
            log.error("Failed to get sensors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("GET_SENSORS_FAILED", e.getMessage()));
        }
    }
}
