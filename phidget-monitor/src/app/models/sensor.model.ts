
export interface Sensor {
  sensorId: string;
  sensorType: string;
  sensorName: string;
  hubPort: number;
  channel: number;
  value: number | null;
  unit: string;
  timestamp: number;
  attached: boolean;
}

export interface ConnectionStatus {
  connected: boolean;
  serverAddress: string | null;
  port: number | null;
  message: string;
  connectedAt: number | null;
}

export interface ConnectionRequest {
  serverAddress: string;
  port: number;
  password?: string;
}

export interface SensorStatus {
  sensorId: string;
  sensorType: string;
  sensorName: string;
  hubPort: number;
  value: number | null;
  unit: string;
  channel: number;
  attached: boolean;
  status: string;
}

export interface ApiError {
  error: string;
  message: string;
  timestamp: number;
}

export const SENSOR_COLORS: { [key: string]: string } = {
  'TEMPERATURE': '#ff6b6b',
  'HUMIDITY': '#4ecdc4',
  'VOLTAGE': '#ffe66d',
  'VOLTAGERATIO': '#ffd93d',
  'DIGITALINPUT': '#a8dadc',
  'DIGITALOUTPUT': '#457b9d',
  'DISTANCESENSOR': '#f1c40f',
  'LIGHTSENSOR': '#f39c12',
  'SOUNDSENSOR': '#9b59b6',
  'PRESSURESENSOR': '#3498db'
};

export const SENSOR_ICONS: { [key: string]: string } = {
  'TEMPERATURE': 'thermostat',
  'HUMIDITY': 'water_drop',
  'VOLTAGE': 'bolt',
  'VOLTAGERATIO': 'electric_bolt',
  'DIGITALINPUT': 'toggle_on',
  'DIGITALOUTPUT': 'power_settings_new',
  'DISTANCESENSOR': 'straighten',
  'LIGHTSENSOR': 'wb_sunny',
  'SOUNDSENSOR': 'graphic_eq',
  'PRESSURESENSOR': 'speed'
};
