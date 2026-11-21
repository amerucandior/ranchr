import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';

const ICONS: Record<string, string> = {
  'bolt': '<path d=\'M13 2 3 14h9l-1 8 10-12h-9l1-8z\'/>',
  'calendar': '<rect x=\'3\' y=\'4\' width=\'18\' height=\'18\' rx=\'2\'/><path d=\'M16 2v4M8 2v4M3 10h18\'/>',
  'paw': '<circle cx=\'11\' cy=\'4\' r=\'2\'/><circle cx=\'18\' cy=\'8\' r=\'2\'/><circle cx=\'4\' cy=\'8\' r=\'2\'/><path d=\'M9 10a5 5 0 0 1 5 5v3.5a3.5 3.5 0 0 1-6.84 1.045Q6.52 17.48 4.46 16.84A3.5 3.5 0 0 1 5.5 10Z\'/>',
  'bike': '<circle cx=\'18.5\' cy=\'17.5\' r=\'3.5\'/><circle cx=\'5.5\' cy=\'17.5\' r=\'3.5\'/><circle cx=\'15\' cy=\'5\' r=\'1\'/><path d=\'M12 17.5V14l-3-3 4-3 2 3h2\'/>',
  'handshake': '<path d=\'m11 17 2 2a1 1 0 1 0 3-3\'/><path d=\'m14 14 2.5 2.5a1 1 0 1 0 3-3l-3.88-3.88a3 3 0 0 0-4.24 0l-.88.88a1 1 0 1 1-3-3l2.81-2.81a5.79 5.79 0 0 1 7.06-.87l.47.28a2 2 0 0 0 1.42.25L21 4\'/><path d=\'m21 3 1 11h-2\'/><path d=\'M3 3 2 14l6.5 6.5a1 1 0 1 0 3-3\'/><path d=\'M3 4h8\'/>',
  'lock': '<rect x=\'3\' y=\'11\' width=\'18\' height=\'11\' rx=\'2\'/><path d=\'M7 11V7a5 5 0 0 1 10 0v4\'/>',
  'sparkles': '<path d=\'M12 3l1.9 5.1L19 10l-5.1 1.9L12 17l-1.9-5.1L5 10l5.1-1.9z\'/><path d=\'M19 3v4M17 5h4\'/>',
  'dna': '<path d=\'M2 15c6.667-6 13.333 0 20-6\'/><path d=\'M9 22c1.798-1.998 2.518-3.995 2.807-5.993\'/><path d=\'M15 2c-1.798 1.998-2.518 3.995-2.807 5.993\'/><path d=\'m17 6-2.5-2.5\'/><path d=\'m14 8-1-1\'/><path d=\'m7 18 2.5 2.5\'/><path d=\'m3.5 14.5.5.5\'/><path d=\'m20 9 .5.5\'/><path d=\'m6.5 12.5 1 1\'/><path d=\'m16.5 10.5 1 1\'/><path d=\'m10 16 1.5 1.5\'/>',
  'pin': '<path d=\'M20 10c0 6-8 12-8 12S4 16 4 10a8 8 0 0 1 16 0z\'/><circle cx=\'12\' cy=\'10\' r=\'3\'/>',
  'phone': '<path d=\'M22 16.9v3a2 2 0 0 1-2.2 2 19.8 19.8 0 0 1-8.6-3.1 19.5 19.5 0 0 1-6-6A19.8 19.8 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7c.1 1 .4 1.9.7 2.8a2 2 0 0 1-.5 2.1L8.1 9.9a16 16 0 0 0 6 6l1.3-1.3a2 2 0 0 1 2.1-.4c.9.3 1.8.6 2.8.7a2 2 0 0 1 1.7 2z\'/>',
  'check': '<path d=\'M20 6 9 17l-5-5\'/>',
  'alert': '<path d=\'m21.7 18-8-14a2 2 0 0 0-3.5 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.7-3z\'/><path d=\'M12 9v4M12 17h.01\'/>',
  'star': '<path d=\'m12 2 3.1 6.3 6.9 1-5 4.9 1.2 6.8L12 17.8 5.8 21l1.2-6.8-5-4.9 6.9-1z\'/>',
  'shield': '<path d=\'M20 13c0 5-3.5 7.5-7.7 9a1 1 0 0 1-.6 0C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.2-2.7a1.2 1.2 0 0 1 1.6 0C14.5 3.8 17 5 19 5a1 1 0 0 1 1 1z\'/><path d=\'m9 12 2 2 4-4\'/>',
  'smartphone': '<rect x=\'5\' y=\'2\' width=\'14\' height=\'20\' rx=\'2\'/><path d=\'M12 18h.01\'/>',
  'stethoscope': '<path d=\'M11 2v2M5 2v2M5 3H4a2 2 0 0 0-2 2v4a6 6 0 0 0 12 0V5a2 2 0 0 0-2-2h-1\'/><path d=\'M8 15a6 6 0 0 0 12 0v-3\'/><circle cx=\'20\' cy=\'10\' r=\'2\'/>',
  'search': '<circle cx=\'11\' cy=\'11\' r=\'8\'/><path d=\'m21 21-4.3-4.3\'/>',
  'leaf': '<path d=\'M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.5 19 2c1 2 2 4.2 2 8 0 5.5-4.8 10-10 10z\'/><path d=\'M2 21c0-3 1.9-5.4 5.1-6.9\'/>',
  'heart': '<path d=\'M19 14c1.5-1.5 3-3.2 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.8 0-3 .5-4.5 2-1.5-1.5-2.7-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4 3 5.5l7 7z\'/>',
  'camera': '<path d=\'M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3z\'/><circle cx=\'12\' cy=\'13\' r=\'3\'/>',
  'mail': '<rect x=\'2\' y=\'4\' width=\'20\' height=\'16\' rx=\'2\'/><path d=\'m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7\'/>',
  'inbox': '<path d=\'M22 12h-6l-2 3h-4l-2-3H2\'/><path d=\'M5.5 5.1 2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.5-6.9A2 2 0 0 0 16.7 4H7.3a2 2 0 0 0-1.8 1.1z\'/>',
  'send': '<path d=\'m22 2-7 20-4-9-9-4z\'/><path d=\'M22 2 11 13\'/>',
  'user': '<circle cx=\'12\' cy=\'8\' r=\'4\'/><path d=\'M4 21a8 8 0 0 1 16 0\'/>',
  'syringe': '<path d=\'m18 2 4 4M17 7l3-3M19 9l-8.7 8.7a1 1 0 0 1-1.4 0L6.3 15a1 1 0 0 1 0-1.4L15 5M9 11l4 4M5 19l-3 3M14 4l6 6\'/>',
  'x': '<path d=\'M18 6 6 18M6 6l12 12\'/>',
  'signal': '<path d=\'M2 20h.01M7 20v-4M12 20v-8M17 20V8M22 4v16\'/>',
  'clipboard': '<rect x=\'8\' y=\'2\' width=\'8\' height=\'4\' rx=\'1\'/><path d=\'M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2\'/>'
};

/** Inline SVG icon (stroke style, inherits currentColor and font-size). */
@Component({
  selector: 'app-icon',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<svg
    viewBox="0 0 24 24"
    width="1em"
    height="1em"
    fill="none"
    stroke="currentColor"
    stroke-width="2"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
    [innerHTML]="path()"></svg>`,
  styles: [
    `
      :host {
        display: inline-flex;
        vertical-align: -0.125em;
        line-height: 1;
      }
    `,
  ],
})
export class IconComponent {
  readonly name = input.required<string>();
  private readonly sanitizer = inject(DomSanitizer);
  // Trusted: markup comes only from the static ICONS table above.
  protected readonly path = computed(() =>
    this.sanitizer.bypassSecurityTrustHtml(ICONS[this.name()] ?? ''),
  );
}
