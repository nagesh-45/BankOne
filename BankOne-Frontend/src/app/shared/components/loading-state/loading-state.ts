import { Component, Input } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-loading-state',
  standalone: true,
  imports: [MatProgressSpinnerModule],
  templateUrl: './loading-state.html',
  styleUrl: './loading-state.scss'
})
export class LoadingState {
  /** Optional caption under the spinner */
  @Input() message = '';
  /** Diameter in px */
  @Input() diameter = 48;
}
