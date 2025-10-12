import { Component, output, input, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SENSOR_TYPES } from '../../models/sensor.model';

@Component({
  selector: 'app-sensor-registration-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sensor-registration-modal.component.html',
  styleUrls: ['./sensor-registration-modal.component.css']
})
export class SensorRegistrationModalComponent {
  isLoading = input<boolean>(false);
  register = output<{
    sensorType: string;
    hubPort: number;
    channel: number;
    serialNumber?: number;
    sensorName: string;
  }>();
  close = output<void>();

  // Form state using signals
  sensorType = signal('TEMPERATURE');
  sensorName = signal('');
  hubPort = signal('0');
  channel = signal('0');
  serialNumber = signal('');

  // Available sensor types
  sensorTypes = SENSOR_TYPES;

  // Computed property for selected sensor info
  selectedSensor = computed(() =>
    this.sensorTypes.find(s => s.value === this.sensorType())
  );

  // Computed property for button state
  canRegister = computed(() =>
    this.sensorName() &&
    this.hubPort()  &&
    this.channel()
  );

  onSubmit(): void {
    if (this.canRegister()) {
      this.register.emit({
        sensorType: this.sensorType(),
        sensorName: this.sensorName(),
        hubPort: parseInt(this.hubPort(), 10),
        channel: parseInt(this.channel(), 10),
        serialNumber: this.serialNumber() ? parseInt(this.serialNumber(), 10) : undefined
      });
    }
  }

  onClose(): void {
    this.close.emit();
  }

  onSensorTypeChange(value: string): void {
    this.sensorType.set(value);
    // Auto-generate name if empty
    if (!this.sensorName()) {
      const sensor = this.sensorTypes.find(s => s.value === value);
      if (sensor) {
        this.sensorName.set(sensor.label);
      }
    }
  }
}
