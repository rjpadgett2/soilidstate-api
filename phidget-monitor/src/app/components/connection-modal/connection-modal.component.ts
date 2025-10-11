
import { Component, output, input, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-connection-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './connection-modal.component.html',
  styleUrls: ['./connection-modal.component.css']
})
export class ConnectionModalComponent {
  // Angular 20: New input/output signals
  isLoading = input<boolean>(false);
  connect = output<{ serverAddress: string; port: number; password?: string }>();
  close = output<void>();

  // Form state using signals
  serverAddress = signal('');
  port = signal('8080');
  password = signal('');

  // Computed property for button state
  canConnect = computed(() =>
    this.serverAddress().trim() !== '' && this.port() !== ''
  );

  onSubmit(): void {
    if (this.canConnect()) {
      this.connect.emit({
        serverAddress: this.serverAddress(),
        port: parseInt(this.port(), 10),
        password: this.password() || undefined
      });
    }
  }

  onClose(): void {
    this.close.emit();
  }
}

