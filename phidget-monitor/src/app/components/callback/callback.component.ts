import {Component, Inject, OnInit, PLATFORM_ID} from '@angular/core';
import {CommonModule, isPlatformBrowser} from '@angular/common';
import {Router} from '@angular/router';

@Component({
  selector: 'app-callback',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="callback-container">
      <div class="callback-content">
        <div class="spinner"></div>
        <h2>Authenticating...</h2>
        <p>Please wait while we log you in</p>
      </div>
    </div>
  `,
  styles: [`
    .callback-container {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
    }

    .callback-content {
      text-align: center;
      color: #ffffff;
    }

    .spinner {
      width: 60px;
      height: 60px;
      border: 4px solid rgba(255, 255, 255, 0.1);
      border-top-color: #e94560;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin: 0 auto 2rem;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    h2 {
      font-size: 1.5rem;
      font-weight: 600;
      margin: 0 0 0.5rem;
    }

    p {
      color: rgba(255, 255, 255, 0.6);
      font-size: 1rem;
    }
  `]
})
export class CallbackComponent implements OnInit {
  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private router: Router
  ) {}

  ngOnInit() {
    // Only run in browser, not during SSR
    if (isPlatformBrowser(this.platformId)) {
      // Auth0 SDK handles the callback automatically
      // If callback takes too long, redirect to home
      setTimeout(() => {
        this.router.navigate(['/']);
      }, 10000); // 10 second timeout
    }
  }
}
