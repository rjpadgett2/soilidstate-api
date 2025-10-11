import { ApplicationConfig, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    // Angular 20: Zoneless change detection for better performance
    provideZonelessChangeDetection(),
    provideRouter(routes),
    // Angular 20: New fetch API integration
    provideHttpClient(withFetch()),
    provideAnimations()
  ]
};
