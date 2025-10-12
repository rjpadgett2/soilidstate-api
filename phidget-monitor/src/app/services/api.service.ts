// src/app/services/api.service.ts - Updated with sensor registration
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {
  ConnectionRequest,
  ConnectionStatus,
  SensorStatus,
  Sensor,
  SensorRegistrationRequest,
  SensorRegistrationResponse
} from '../models/sensor.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = '';

  constructor(private http: HttpClient) {}

  setBaseUrl(serverAddress: string, port: number): void {
    let cleanUrl = serverAddress.trim();
    if (!cleanUrl.startsWith('http://') && !cleanUrl.startsWith('https://')) {
      cleanUrl = 'http://' + cleanUrl;
    }
    this.baseUrl = 'http://localhost:8080/api/phidget';
  }

  connect(request: ConnectionRequest): Observable<ConnectionStatus> {
    this.setBaseUrl(request.serverAddress, request.port);

    const phidgetRequest = {
      serverAddress: request.serverAddress
        .replace('http://', '')
        .replace('https://', ''),
      port: 5661,
      password: request.password || null
    };

    return this.http.post<ConnectionStatus>(
      `${this.baseUrl}/connect`,
      phidgetRequest
    ).pipe(
      catchError(this.handleError)
    );
  }

  disconnect(): Observable<any> {
    return this.http.post(`${this.baseUrl}/disconnect`, {}).pipe(
      catchError(this.handleError)
    );
  }

  getConnectionStatus(): Observable<ConnectionStatus> {
    return this.http.get<ConnectionStatus>(`${this.baseUrl}/status`).pipe(
      catchError(this.handleError)
    );
  }

  getSensors(): Observable<{ [key: string]: SensorStatus }> {
    return this.http.get<{ [key: string]: SensorStatus }>(
      `${this.baseUrl}/sensors`
    ).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get latest sensor data for all sensors
   */
  getAllSensorData(): Observable<{ [key: string]: Sensor }> {
    return this.http.get<{ [key: string]: Sensor }>(
      `${this.baseUrl}/sensors/data`
    ).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get latest data for a specific sensor
   */
  getSensorData(sensorId: string): Observable<Sensor> {
    return this.http.get<Sensor>(
      `${this.baseUrl}/sensors/${sensorId}/data`
    ).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * NEW: Register a new sensor
   */
  registerSensor(request: SensorRegistrationRequest): Observable<SensorRegistrationResponse> {
    return this.http.post<SensorRegistrationResponse>(
      `${this.baseUrl}/sensors/register`,
      request
    ).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * NEW: Unregister a sensor
   */
  unregisterSensor(sensorId: string): Observable<any> {
    return this.http.delete(
      `${this.baseUrl}/sensors/${sensorId}`
    ).pipe(
      catchError(this.handleError)
    );
  }

  getBaseUrl(): string {
    return this.baseUrl;
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An error occurred';

    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      if (error.error?.message) {
        errorMessage = error.error.message;
      } else if (error.status === 0) {
        errorMessage = 'Unable to connect to server. Please check the address and ensure the server is running.';
      } else {
        errorMessage = `Server returned code ${error.status}: ${error.message}`;
      }
    }

    return throwError(() => new Error(errorMessage));
  }
}
