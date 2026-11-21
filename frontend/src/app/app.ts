import { IconComponent } from './shared/components/icon/icon.component';
import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { WebSocketService } from './core/services/websocket.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [IconComponent, CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrls: ['./app.scss'],
})
export class App {
  readonly authService = inject(AuthService);
  readonly wsService = inject(WebSocketService);
  private readonly router = inject(Router);

  readonly footerMessage = signal<string | null>(null);

  /** Footer sign-up: an email continues to Register, a phone number gets the SMS route. */
  joinRegister(contact: string): void {
    const value = contact.trim();
    if (/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
      this.footerMessage.set(null);
      this.router.navigate(['/register'], { queryParams: { email: value } });
    } else if (/^\+?[\d\s-]{9,15}$/.test(value)) {
      this.footerMessage.set(
        'To join by SMS, text START to 20880 from this number. Or register on the web with your email.'
      );
    } else {
      this.footerMessage.set('Enter a valid email address or phone number.');
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
