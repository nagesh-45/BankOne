import { Component, Input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [
    MatCardModule,
    MatIconModule
  ],
  templateUrl: './stat-card.html',
  styleUrl: './stat-card.scss'
})
export class StatCard {

  @Input()
  title = '';

  private _value = '';

  @Input()
  set value(value: string) {
    console.log('StatCard received value:', value);
    this._value = value;
  }

  get value(): string {
    return this._value;
  }

  @Input()
  icon = '';

  @Input()
  color = '#1565C0';

}
