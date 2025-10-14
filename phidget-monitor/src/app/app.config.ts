import { ApplicationConfig, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import {provideHttpClient, withFetch, withInterceptors} from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { routes } from './app.routes';
import { provideAuth0 } from '@auth0/auth0-angular';
import { authInterceptor } from './interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {

  providers: [
    // Angular 20: Zoneless change detection for better performance
    provideZonelessChangeDetection(),
    provideRouter(routes),
    provideHttpClient(
      withFetch(),
      withInterceptors([authInterceptor])
    ),

    // Angular 20: New fetch API integration
    provideHttpClient(withFetch()),
    provideAnimations(),
    provideAuth0({
      domain: 'dev-rvtdlwraphwqkmke.us.auth0.com',
      clientId: 'nLWvF7BwDSiYa5O6ztyW8tjXvZ8DJLmY',
      authorizationParams: {
        redirect_uri: typeof window !== 'undefined' ? window.location.origin + '/callback' : 'http://localhost:4200/callback',
        audience: 'https://api.phidget.pasture.com',
        scope: 'openid profile email'
      },
      httpInterceptor: {
        allowedList: [
          {
            uri: 'http://localhost:8080/api/*',
            tokenOptions: {
              authorizationParams: {
                audience: 'https://api.phidget.pasture.com',
                scope: 'openid profile email'
              }
            }
          }
        ]
      },
      // Disable Auth0 during SSR
      skipRedirectCallback: typeof window === 'undefined'
    })
  ]
};
