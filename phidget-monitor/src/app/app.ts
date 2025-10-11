
import { Component, OnInit, OnDestroy, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';

import {interval, Subject, switchMap} from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ApiService } from './services/api.service';
import { ConnectionModalComponent } from './components/connection-modal/connection-modal.component';
import { SensorCardComponent } from './components/sensor-card/sensor-card.component';
import { Sensor} from './models/sensor.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    ConnectionModalComponent,
    SensorCardComponent
  ],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent implements OnInit, OnDestroy {
  // Angular 20: Using Signals for reactive state management
  sensors = signal<Sensor[]>([]);
  isConnected = signal(false);
  serverAddress = signal('');
  showConnectionModal = signal(false);
  isLoading = signal(false);
  isCheckingConnection = signal(true);

  // Computed signal for sensor count
  sensorCount = computed(() => this.sensors().length);

  private destroy$ = new Subject<void>();
  private pollingInterval = 1000; // Poll every 1 second for real-time feel
  private readonly STORAGE_KEY = 'phidget_connection';

  constructor(
    private apiService: ApiService
  ) {
    // Angular 20: Effect to react to connection changes
    effect(() => {
      if (this.isConnected()) {
        console.log('Connected to server:', this.serverAddress());
        this.saveConnectionState();
      }
    });
  }

  ngOnInit(): void {
    // Check for existing connection on startup
    this.checkExistingConnection();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Check if there's an existing Phidget connection on the backend
   */
  private checkExistingConnection(): void {
    const savedConnection = this.loadConnectionState();

    if (savedConnection) {
      this.apiService.setBaseUrl(savedConnection.serverAddress, savedConnection.port);

      // Check if backend still has active connection
      this.apiService.getConnectionStatus().subscribe({
        next: (status) => {
          if (status.connected) {
            console.log('Restored existing connection to:', status.serverAddress);
            this.isConnected.set(true);
            this.serverAddress.set(savedConnection.serverAddress);
            this.isCheckingConnection.set(false);

            // Start polling for sensors and data
            this.startPolling();
          } else {
            console.log('No active backend connection found');
            this.clearConnectionState();
            this.isCheckingConnection.set(false);
          }
        },
        error: (error) => {
          console.log('Could not connect to saved server:', error.message);
          this.clearConnectionState();
          this.isCheckingConnection.set(false);
        }
      });
    } else {
      this.isCheckingConnection.set(false);
    }
  }

  onConnect(connectionData: { serverAddress: string; port: number; password?: string }): void {
    this.isLoading.set(true);

    this.apiService.connect(connectionData).subscribe({
      next: (status) => {
        this.isConnected.set(true);
        this.serverAddress.set(connectionData.serverAddress);
        this.showConnectionModal.set(false);
        this.isLoading.set(false);

        // Save connection info
        this.saveConnectionState();

        // Start polling for sensors
        this.startPolling();
      },
      error: (error) => {
        console.error('Connection failed:', error);
        this.isLoading.set(false);
        alert('Connection failed: ' + error.message);
      }
    });
  }

  disconnect(): void {
    this.apiService.disconnect().subscribe({
      next: () => {
        this.cleanup();
      },
      error: (error) => {
        console.error('Disconnect error:', error);
        this.cleanup();
      }
    });
  }

  private cleanup(): void {
    this.isConnected.set(false);
    this.sensors.set([]);
    this.serverAddress.set('');
    this.clearConnectionState();
    this.destroy$.next();
  }

  openConnectionModal(): void {
    this.showConnectionModal.set(true);
  }

  closeConnectionModal(): void {
    this.showConnectionModal.set(false);
  }

  private startPolling(): void {
    // Initial fetch
    this.fetchSensorsAndData();

    // Poll every second for real-time updates
    interval(this.pollingInterval)
      .pipe(
        takeUntil(this.destroy$),
        switchMap(() => this.apiService.getAllSensorData())
      )
      .subscribe({
        next: (sensorDataDict) => {
          this.updateSensorsFromData(sensorDataDict);
        },
        error: (error) => {
          console.error('Error fetching sensor data:', error);
          // If we get persistent errors, check connection status
          this.verifyConnection();
        }
      });
  }

  private fetchSensorsAndData(): void {
    // First, get the sensor list
    this.apiService.getSensors().subscribe({
      next: (sensorsDict) => {
        const sensorArray = Object.values(sensorsDict);
        const currentSensors = this.sensors();

        // Add or update sensors from the list
        sensorArray.forEach(sensorStatus => {
          const existingSensor = currentSensors.find(s => s.sensorId === sensorStatus.sensorId);

          if (!existingSensor) {
            // Add new sensor with no value yet
            const newSensor: Sensor = {
              sensorId: sensorStatus.sensorId,
              sensorType: sensorStatus.sensorType,
              sensorName: sensorStatus.sensorName,
              hubPort: sensorStatus.hubPort,
              channel: sensorStatus.channel,
              value: null,
              unit: this.getUnit(sensorStatus.sensorType),
              timestamp: Date.now(),
              attached: sensorStatus.attached
            };

            this.sensors.update(sensors => [...sensors, newSensor]);
          }
        });

        // Remove sensors that no longer exist
        const currentSensorIds = sensorArray.map(s => s.sensorId);
        this.sensors.update(sensors =>
          sensors.filter(s => currentSensorIds.includes(s.sensorId))
        );

        // Now fetch the actual data
        this.apiService.getAllSensorData().subscribe({
          next: (sensorDataDict) => {
            this.updateSensorsFromData(sensorDataDict);
          },
          error: (error) => {
            console.error('Error fetching initial sensor data:', error);
          }
        });
      },
      error: (error) => {
        console.error('Error fetching sensors:', error);
      }
    });
  }

  private updateSensorsFromData(sensorDataDict: { [key: string]: Sensor }): void {
    const sensorDataArray = Object.values(sensorDataDict);

    sensorDataArray.forEach(sensorData => {
      const currentSensors = this.sensors();
      const index = currentSensors.findIndex(s => s.sensorId === sensorData.sensorId);

      if (index !== -1) {
        // Update existing sensor with new data
        this.sensors.update(sensors =>
          sensors.map((s, i) => i === index ? { ...sensorData } : s)
        );
      } else {
        // Add new sensor if not in list yet
        this.sensors.update(sensors =>
          [...sensors, sensorData].sort((a, b) => a.sensorName.localeCompare(b.sensorName))
        );
      }
    });

    // Sort sensors by name
    this.sensors.update(sensors =>
      [...sensors].sort((a, b) => a.sensorName.localeCompare(b.sensorName))
    );
  }

  private verifyConnection(): void {
    this.apiService.getConnectionStatus().subscribe({
      next: (status) => {
        if (!status.connected) {
          console.log('Backend connection lost');
          this.cleanup();
        }
      },
      error: () => {
        console.log('Cannot reach server');
        this.cleanup();
      }
    });
  }

  private saveConnectionState(): void {
    const savedConnection = this.loadConnectionState();
    const connectionState = {
      serverAddress: this.serverAddress(),
      port: savedConnection?.port || 8080,
      timestamp: Date.now()
    };
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(connectionState));
  }

  private loadConnectionState(): { serverAddress: string; port: number } | null {
    const stored = localStorage.getItem(this.STORAGE_KEY);
    if (stored) {
      try {
        const parsed = JSON.parse(stored);
        // Check if connection info is not too old (24 hours)
        if (Date.now() - parsed.timestamp < 24 * 60 * 60 * 1000) {
          return parsed;
        }
      } catch (e) {
        console.error('Error parsing stored connection:', e);
      }
    }
    return null;
  }

  private clearConnectionState(): void {
    localStorage.removeItem(this.STORAGE_KEY);
  }

  private getUnit(sensorType: string): string {
    const units: { [key: string]: string } = {
      'TEMPERATURE': '°C',
      'HUMIDITY': '%',
      'VOLTAGE': 'V',
      'VOLTAGERATIO': 'V/V',
      'DIGITALINPUT': '',
      'DIGITALOUTPUT': '',
      'DISTANCESENSOR': 'mm',
      'LIGHTSENSOR': 'lux',
      'SOUNDSENSOR': 'dB',
      'PRESSURESENSOR': 'kPa'
    };
    return units[sensorType.toUpperCase()] || '';
  }
}
