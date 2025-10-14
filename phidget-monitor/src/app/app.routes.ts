import { Routes } from '@angular/router';
import { CallbackComponent } from './components/callback/callback.component';

export const routes: Routes = [
  {
    path: 'callback',
    component: CallbackComponent
  },
  {
    path: '',
    redirectTo: '',
    pathMatch: 'full'
  }
];
