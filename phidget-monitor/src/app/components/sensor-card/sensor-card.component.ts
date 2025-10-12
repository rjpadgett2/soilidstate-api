import { Component, input, computed, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Sensor, SENSOR_COLORS, SENSOR_ICONS } from '../../models/sensor.model';

@Component({
  selector: 'app-sensor-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sensor-card.component.html',
  styleUrls: ['./sensor-card.component.css']
})
export class SensorCardComponent {
  // Angular 20: Using input signals (new required input syntax)
  sensor = input.required<Sensor>();

  // Angular 20: Output for delete action
  delete = output<void>();

  // Computed signals for derived values
  sensorColor = computed(() =>
    SENSOR_COLORS[this.sensor().sensorType.toUpperCase()] || '#95a5a6'
  );

  sensorIcon = computed(() =>
    SENSOR_ICONS[this.sensor().sensorType.toUpperCase()] || 'sensors'
  );

  formattedValue = computed(() => {
    const value = this.sensor().value;
    if (value === null || value === undefined) {
      return '--';
    }
    return value.toFixed(2);
  });

  timeAgo = computed(() => {
    const now = Date.now();
    const diff = now - this.sensor().timestamp;
    const seconds = Math.floor(diff / 1000);

    if (seconds < 60) return 'Just now';
    if (seconds < 3600) return `${Math.floor(seconds / 60)}m ago`;
    if (seconds < 86400) return `${Math.floor(seconds / 3600)}h ago`;
    return `${Math.floor(seconds / 86400)}d ago`;
  });

  onDelete(): void {
    this.delete.emit();
  }
}
