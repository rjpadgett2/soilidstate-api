Installation & Setup
1. Clone or Create Project
Create a new directory and add all the Java files in the structure above.
2. Install Dependencies
bashmvn clean install
3. Configure PhidgetSBC4
Ensure your PhidgetSBC4:

Is connected to your network
Has Phidget Network Server running (check via Phidget Control Panel)
Note its IP address (e.g., 192.168.1.100)

4. Run the Application
bashmvn spring-boot:run
Or build and run the JAR:
bashmvn clean package
java -jar target/phidget-api-1.0.0.jar
The API will start on http://localhost:8080
Quick Start Guide
Step 1: Connect to PhidgetSBC4
bashcurl -X POST http://localhost:8080/api/phidget/connect \
  -H "Content-Type: application/json" \
  -d '{
    "serverAddress": "192.168.1.100",
    "port": 5661
  }'
Step 2: Register a Sensor
bashcurl -X POST http://localhost:8080/api/phidget/sensors/register \
  -H "Content-Type: application/json" \
  -d '{
    "sensorType": "TEMPERATURE",
    "hubPort": 0,
    "channel": 0,
    "sensorName": "Room Temp"
  }'
Step 3: View All Sensors
bashcurl http://localhost:8080/api/phidget/sensors
Step 4: Connect WebSocket for Real-Time Data
Use the provided HTML client or connect via JavaScript:
javascriptconst socket = new SockJS('http://localhost:8080/ws/phidget');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    stompClient.subscribe('/topic/sensor-data', function(message) {
        console.log('Sensor data:', JSON.parse(message.body));
    });
});
Supported Sensor Types
Sensor TypeDescriptionUnitTEMPERATURETemperature Sensor°CHUMIDITYHumidity Sensor%VOLTAGEVoltage InputVVOLTAGERATIOVoltage Ratio InputV/VDIGITALINPUTDigital InputbinaryDIGITALOUTPUTDigital OutputbinaryDISTANCESENSORDistance SensormmLIGHTSENSORLight SensorluxSOUNDSENSORSound SensordBPRESSURESENSORPressure SensorkPa
API Endpoints
Connection Management

POST /api/phidget/connect - Connect to PhidgetSBC4
POST /api/phidget/disconnect - Disconnect from PhidgetSBC4
GET /api/phidget/status - Get connection status

Sensor Management

POST /api/phidget/sensors/register - Register a new sensor
GET /api/phidget/sensors - Get all registered sensors
DELETE /api/phidget/sensors/{sensorId} - Unregister a sensor

WebSocket

ws://localhost:8080/ws/phidget - WebSocket endpoint
Topic: /topic/sensor-data - Subscribe for real-time sensor data

Configuration
All configuration is done via API requests - no YAML/properties configuration needed for connection details!
The application.properties only contains server settings:

Server port (default: 8080)
Logging levels
CORS settings
WebSocket configuration

Example Use Cases
Monitor Multiple Temperature Sensors
bash# Register sensor on port 0
curl -X POST http://localhost:8080/api/phidget/sensors/register \
  -H "Content-Type: application/json" \
  -d '{"sensorType": "TEMPERATURE", "hubPort": 0, "channel": 0, "sensorName": "Zone 1"}'

# Register sensor on port 1
curl -X POST http://localhost:8080/api/phidget/sensors/register \
  -H "Content-Type: application/json" \
  -d '{"sensorType": "TEMPERATURE", "hubPort": 1, "channel": 0, "sensorName": "Zone 2"}'
Environmental Monitoring Station
bash# Temperature
curl -X POST http://localhost:8080/api/phidget/sensors/register \
  -H "Content-Type: application/json" \
  -d '{"sensorType": "TEMPERATURE", "hubPort": 0, "channel": 0, "sensorName": "Outdoor Temp"}'

# Humidity
curl -X POST http://localhost:8080/api/phidget/sensors/register \
  -H "Content-Type: application/json" \
  -d '{"sensorType": "HUMIDITY", "hubPort": 1, "channel": 0, "sensorName": "Outdoor Humidity"}'

# Light
curl -X POST http://localhost:8080/api/phidget/sensors/register \
  -H "Content-Type: application/json" \
  -d '{"sensorType": "LIGHTSENSOR", "hubPort": 2, "channel": 0, "sensorName": "Ambient Light"}'
Troubleshooting
Connection Issues
Problem: Cannot connect to PhidgetSBC4

Verify IP address is correct
Check that Phidget Network Server is running on SBC4
Verify port 5661 is not blocked by firewall
Try connecting with Phidget Control Panel first to verify

Sensor Not Attaching
Problem: Sensor registered but shows "Detached"

Verify sensor is physically connected to the correct hub port
Check that the sensor type matches the actual hardware
Ensure the channel number is correct (usually 0 for single-channel sensors)
Try a different hub port

No WebSocket Data
Problem: WebSocket connected but no data received

Verify sensor is showing as "Attached" in GET /sensors
Check browser console for WebSocket errors
Ensure you're subscribed to /topic/sensor-data

Maven Build Failures
Problem: Dependencies not downloading

Check internet connection
Try mvn clean install -U to force update
Verify Maven settings.xml if behind proxy

Development
Adding New Sensor Types
To add support for additional Phidget sensor types:

Add the case to createSensor() in PhidgetService.java
Add event handler in attachDataChangeHandlers()
Add unit mapping in getUnit()

Customizing Data Interval
To change sensor data change intervals:
java// In PhidgetService after sensor.open()
if (sensor instanceof VoltageInput voltageInput) {
    voltageInput.setDataInterval(100); // milliseconds
}
Security Considerations
For production deployment:

Enable authentication on Phidget Network Server
Use HTTPS for REST API
Use WSS for WebSocket connections
Configure CORS to specific origins only
Add Spring Security for API authentication
Use environment variables for sensitive data

License
This project uses the Phidget22 library which is subject to Phidgets Inc. licensing terms.
Support
For issues related to:

API: Check application logs in console
Phidgets: Visit https://www.phidgets.com/docs/
Spring Boot: Visit https://spring.io/projects/spring-boot

Version History

1.0.0 - Initial release with support for 9 sensor types and real-time streaming
