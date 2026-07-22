import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-list-pagination',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './list-pagination.html',
  styleUrl: './list-pagination.scss'
})
export class ListPagination {
  @Input() pageIndex = 0;
  @Input() pageSize = 10;
  @Input() totalElements = 0;
  @Input() totalPages = 0;
  @Input() pageSizeOptions: number[] = [10, 25, 50];

  @Output() readonly pageIndexChange = new EventEmitter<number>();
  @Output() readonly pageSizeChange = new EventEmitter<number>();

  get from(): number {
    if (this.totalElements === 0) {
      return 0;
    }
    return this.pageIndex * this.pageSize + 1;
  }

  get to(): number {
    return Math.min((this.pageIndex + 1) * this.pageSize, this.totalElements);
  }

  get showControls(): boolean {
    return this.totalElements > 0;
  }

  previous(): void {
    if (this.pageIndex > 0) {
      this.pageIndexChange.emit(this.pageIndex - 1);
    }
  }

  next(): void {
    if (this.pageIndex + 1 < this.totalPages) {
      this.pageIndexChange.emit(this.pageIndex + 1);
    }
  }

  onPageSizeChange(size: number | string): void {
    this.pageSizeChange.emit(Number(size));
  }
}
