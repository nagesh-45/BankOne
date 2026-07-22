import { Pipe, PipeTransform } from '@angular/core';

import { formatCustomerCode, formatEmployeeCode } from '../utils/business-id';

@Pipe({
  name: 'businessId',
  standalone: true
})
export class BusinessIdPipe implements PipeTransform {
  transform(id: number | null | undefined, type: 'C' | 'E' = 'C'): string {
    return type === 'E' ? formatEmployeeCode(id) : formatCustomerCode(id);
  }
}
