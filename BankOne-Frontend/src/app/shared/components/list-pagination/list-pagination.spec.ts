import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { ListPagination } from './list-pagination';

describe('ListPagination', () => {
  let fixture: ComponentFixture<ListPagination>;
  let component: ListPagination;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListPagination]
    }).compileComponents();

    fixture = TestBed.createComponent(ListPagination);
    component = fixture.componentInstance;
    component.pageIndex = 2;
    component.pageSize = 10;
    component.totalElements = 10000;
    component.totalPages = 1000;
    fixture.detectChanges();
  });

  it('emits 0 on first()', () => {
    const spy = vi.spyOn(component.pageIndexChange, 'emit');
    component.first();
    expect(spy).toHaveBeenCalledWith(0);
  });

  it('emits last 0-based index on last()', () => {
    const spy = vi.spyOn(component.pageIndexChange, 'emit');
    component.last();
    expect(spy).toHaveBeenCalledWith(999);
  });

  it('goToPage converts 1-based UI page to 0-based index', () => {
    const spy = vi.spyOn(component.pageIndexChange, 'emit');
    component.goToPage(1000);
    expect(spy).toHaveBeenCalledWith(999);
  });

  it('goToPage clamps out-of-range values', () => {
    const spy = vi.spyOn(component.pageIndexChange, 'emit');
    component.goToPage(0);
    expect(spy).toHaveBeenCalledWith(0);
    spy.mockClear();
    component.goToPage(5000);
    expect(spy).toHaveBeenCalledWith(999);
  });

  it('does not emit when already on that page', () => {
    const spy = vi.spyOn(component.pageIndexChange, 'emit');
    component.goToPage(3); // pageIndex is already 2
    expect(spy).not.toHaveBeenCalled();
  });
});
