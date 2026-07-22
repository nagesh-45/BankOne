import { Component, Input } from '@angular/core';

export type BrandLogoTheme = 'light' | 'dark' | 'on-primary';
export type BrandLogoSize = 'sm' | 'md' | 'lg' | 'xl';

let brandLogoSeq = 0;

@Component({
  selector: 'app-brand-logo',
  standalone: true,
  templateUrl: './brand-logo.html',
  styleUrl: './brand-logo.scss'
})
export class BrandLogo {
  @Input() theme: BrandLogoTheme = 'light';
  @Input() size: BrandLogoSize = 'md';
  @Input() showWordmark = true;
  @Input() tagline = '';

  readonly gradientId = `boShield-${++brandLogoSeq}`;
}
