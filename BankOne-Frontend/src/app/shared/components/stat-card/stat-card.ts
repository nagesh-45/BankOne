import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [
    MatCardModule,
    MatIconModule,
    RouterLink
  ],
  templateUrl: './stat-card.html',
  styleUrl: './stat-card.scss'
})
export class StatCard {

  @Input()
  title = '';

  @Input()
  value = '';

  @Input()
  icon = '';

  @Input()
  color = '#1565C0';

  @Input()
  link: string | null = null;

}
