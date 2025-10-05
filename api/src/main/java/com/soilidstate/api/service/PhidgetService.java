package com.soilidstate.api.service;

import com.phidget22.*;
import com.soilidstate.api.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhidgetService {

    private final SimpMessagingTemplate messagingTemplate;

    private Net net;
    private boolean connected = false;
    private String currentServer;
    private Integer currentPort;
    private Long connectedAt;

    private final Map<String, Phidget> sensors = new ConcurrentHashMap<>();
    private final Map<String, SensorMetadata> sensorMetadata = new ConcurrentHashMap<>();

    public ConnectionStatusResponse connect(ConnectionRequest request) throws PhidgetException {
        if (connected) {
            disconnect();
        }

        net = new Net();

        try {
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                Net.enableServerDiscovery(ServerType.DEVICE_REMOTE);
                net.addServer("", request.getServerAddress(), request.getPort(),
                        request.getPassword(), 0);
            } else {
                Net.enableServerDiscovery(ServerType.DEVICE_REMOTE);
                net.addServer("", request.getServerAddress(), request.getPort(), "", 0);
            }

            connected = true;
            currentServer = request.getServerAddress();
            currentPort = request.getPort();
            connectedAt = System.currentTimeMillis();

            log.info("Connected to Phidget server: {}:{}", request.getServerAddress(), request.getPort());

            ConnectionStatusResponse response = new ConnectionStatusResponse();
            response.setConnected(true);
            response.setServerAddress(currentServer);
            response.setPort(currentPort);
            response.setMessage("Successfully connected to Phidget server");
            response.setConnectedAt(connectedAt);

            return response;

        } catch (PhidgetException e) {
            connected = false;
            log.error("Failed to connect to Phidget server", e);
            throw e;
        }
    }

    public void disconnect() throws PhidgetException {
        for (Map.Entry<String, Phidget> entry : sensors.entrySet()) {
            try {
                entry.getValue().close();
            } catch (PhidgetException e) {
                log.error("Error closing sensor: {}", entry.getKey(), e);
            }
        }
        sensors.clear();
        sensorMetadata.clear();

        if (net != null) {
            Net.disableServerDiscovery(ServerType.DEVICE_REMOTE);
        }

        connected = false;
        currentServer = null;
        currentPort = null;
        connectedAt = null;

        log.info("Disconnected from Phidget server");
    }

    public ConnectionStatusResponse getConnectionStatus() {
        ConnectionStatusResponse response = new ConnectionStatusResponse();
        response.setConnected(connected);
        response.setServerAddress(currentServer);
        response.setPort(currentPort);
        response.setConnectedAt(connectedAt);
        response.setMessage(connected ? "Connected" : "Disconnected");
        return response;
    }

    public SensorStatusResponse registerSensor(SensorRegistrationRequest request) throws PhidgetException {
        if (!connected) {
            throw new IllegalStateException("Not connected to Phidget server");
        }

        String sensorId = UUID.randomUUID().toString();
        Phidget sensor = createSensor(request, sensorId);

        if (request.getSerialNumber() != null) {
            sensor.setDeviceSerialNumber(request.getSerialNumber());
        }
        sensor.setHubPort(request.getHubPort());
        sensor.setChannel(request.getChannel());
        sensor.setIsRemote(true);

        attachEventHandlers(sensor, sensorId, request);

        sensor.open(5000);

        sensors.put(sensorId, sensor);
        sensorMetadata.put(sensorId, new SensorMetadata(
                request.getSensorType(),
                request.getSensorName() != null ? request.getSensorName() : sensorId,
                request.getHubPort(),
                request.getChannel()
        ));

        log.info("Registered sensor: {} on port {} channel {}",
                request.getSensorType(), request.getHubPort(), request.getChannel());

        SensorStatusResponse response = new SensorStatusResponse();
        response.setSensorId(sensorId);
        response.setSensorType(request.getSensorType());
        response.setSensorName(sensorMetadata.get(sensorId).name);
        response.setHubPort(request.getHubPort());
        response.setChannel(request.getChannel());
        response.setAttached(sensor.getAttached());
        response.setStatus("Registered");

        return response;
    }

    private Phidget createSensor(SensorRegistrationRequest request, String sensorId) throws PhidgetException {
        return switch (request.getSensorType().toUpperCase()) {
            case "VOLTAGE" -> new VoltageInput();
            case "VOLTAGERATIO" -> new VoltageRatioInput();
            case "TEMPERATURE" -> new TemperatureSensor();
            case "HUMIDITY" -> new HumiditySensor();
            case "DIGITALINPUT" -> new DigitalInput();
            case "DIGITALOUTPUT" -> new DigitalOutput();
            case "DISTANCESENSOR" -> new DistanceSensor();
            case "LIGHTSENSOR" -> new LightSensor();
            case "SOUNDSENSOR" -> new SoundSensor();
            case "PRESSURESENSOR" -> new PressureSensor();
            default -> throw new IllegalArgumentException("Unsupported sensor type: " + request.getSensorType());
        };
    }

    private void attachEventHandlers(Phidget sensor, String sensorId, SensorRegistrationRequest request) {
        sensor.addAttachListener(event -> {
            log.info("Sensor {} attached", sensorId);
            publishSensorData(sensorId, null, true);
        });

        sensor.addDetachListener(event -> {
            log.warn("Sensor {} detached", sensorId);
            publishSensorData(sensorId, null, false);
        });

        attachDataChangeHandlers(sensor, sensorId, request.getSensorType());
    }

    private void attachDataChangeHandlers(Phidget sensor, String sensorId, String sensorType) {
        switch (sensorType.toUpperCase()) {
            case "VOLTAGE" -> ((VoltageInput) sensor).addVoltageChangeListener(event ->
                    publishSensorData(sensorId, event.getVoltage(), true));

            case "VOLTAGERATIO" -> ((VoltageRatioInput) sensor).addVoltageRatioChangeListener(event ->
                    publishSensorData(sensorId, event.getVoltageRatio(), true));

            case "TEMPERATURE" -> ((TemperatureSensor) sensor).addTemperatureChangeListener(event ->
                    publishSensorData(sensorId, event.getTemperature(), true));

            case "HUMIDITY" -> ((HumiditySensor) sensor).addHumidityChangeListener(event ->
                    publishSensorData(sensorId, event.getHumidity(), true));

            case "DIGITALINPUT" -> ((DigitalInput) sensor).addStateChangeListener(event ->
                    publishSensorData(sensorId, event.getState() ? 1.0 : 0.0, true));

            case "DISTANCESENSOR" -> ((DistanceSensor) sensor).addDistanceChangeListener(event ->
                    publishSensorData(sensorId, (double) event.getDistance(), true));

            case "LIGHTSENSOR" -> ((LightSensor) sensor).addIlluminanceChangeListener(event ->
                    publishSensorData(sensorId, event.getIlluminance(), true));

            case "SOUNDSENSOR" -> ((SoundSensor) sensor).addSPLChangeListener(event ->
                    publishSensorData(sensorId, event.getDB()  , true));

            case "PRESSURESENSOR" -> ((PressureSensor) sensor).addPressureChangeListener(event ->
                    publishSensorData(sensorId, event.getPressure(), true));
        }
    }

    private void publishSensorData(String sensorId, Double value, boolean attached) {
        SensorMetadata metadata = sensorMetadata.get(sensorId);
        if (metadata == null) return;

        SensorDataResponse response = new SensorDataResponse();
        response.setSensorId(sensorId);
        response.setSensorType(metadata.type);
        response.setSensorName(metadata.name);
        response.setHubPort(metadata.hubPort);
        response.setChannel(metadata.channel);
        response.setValue(value);
        response.setUnit(getUnit(metadata.type));
        response.setTimestamp(System.currentTimeMillis());
        response.setAttached(attached);

        messagingTemplate.convertAndSend("/topic/sensor-data", response);
    }

    private String getUnit(String sensorType) {
        return switch (sensorType.toUpperCase()) {
            case "VOLTAGE" -> "V";
            case "VOLTAGERATIO" -> "V/V";
            case "TEMPERATURE" -> "°C";
            case "HUMIDITY" -> "%";
            case "DIGITALINPUT", "DIGITALOUTPUT" -> "binary";
            case "DISTANCESENSOR" -> "mm";
            case "LIGHTSENSOR" -> "lux";
            case "SOUNDSENSOR" -> "dB";
            case "PRESSURESENSOR" -> "kPa";
            default -> "";
        };
    }

    public void unregisterSensor(String sensorId) throws PhidgetException {
        Phidget sensor = sensors.get(sensorId);
        if (sensor != null) {
            sensor.close();
            sensors.remove(sensorId);
            sensorMetadata.remove(sensorId);
            log.info("Unregistered sensor: {}", sensorId);
        }
    }

    public Map<String, SensorStatusResponse> getAllSensors() throws PhidgetException {
        Map<String, SensorStatusResponse> statusMap = new ConcurrentHashMap<>();

        for (Map.Entry<String, Phidget> entry : sensors.entrySet()) {
            String sensorId = entry.getKey();
            Phidget sensor = entry.getValue();
            SensorMetadata metadata = sensorMetadata.get(sensorId);

            SensorStatusResponse status = new SensorStatusResponse();
            status.setSensorId(sensorId);
            status.setSensorType(metadata.type);
            status.setSensorName(metadata.name);
            status.setHubPort(metadata.hubPort);
            status.setChannel(metadata.channel);
            status.setAttached(sensor.getAttached());
            status.setStatus(sensor.getAttached() ? "Attached" : "Detached");

            statusMap.put(sensorId, status);
        }

        return statusMap;
    }

    private record SensorMetadata(String type, String name, Integer hubPort, Integer channel) {}
}
