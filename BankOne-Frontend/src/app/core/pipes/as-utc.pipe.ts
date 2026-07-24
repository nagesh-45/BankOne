import { Pipe, PipeTransform } from '@angular/core';

/**
 * Ensures API datetimes are treated as UTC when the backend omitted an offset
 * (legacy LocalDateTime JSON). Instant values already end with Z and pass through.
 */
@Pipe({
  name: 'asUtc',
  standalone: true
})
export class AsUtcPipe implements PipeTransform {
  transform(value: string | null | undefined): string | null {
    if (value == null || value === '') {
      return null;
    }
    if (/[zZ]$|[+-]\d{2}:?\d{2}$/.test(value)) {
      return value;
    }
    return `${value}Z`;
  }
}
