import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '@auth0/auth0-angular';
import { switchMap, take } from 'rxjs/operators';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  // Check if request is to our API
  if (!req.url.includes('/api/')) {
    return next(req);
  }

  // Get access token and add to request
  return authService.getAccessTokenSilently().pipe(
    take(1),
    switchMap(token => {
      const clonedRequest = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      return next(clonedRequest);
    })
  );
};
