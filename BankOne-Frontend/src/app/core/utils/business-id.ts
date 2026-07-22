const MIN_DIGITS = 5;

function formatCode(prefix: string, id: number | null | undefined): string {
  if (id == null || id <= 0) {
    return '';
  }

  // Keep a 5-digit floor (C00001). Past 99999, width expands (C100000).
  const digits = String(id);
  return prefix + (digits.length < MIN_DIGITS ? digits.padStart(MIN_DIGITS, '0') : digits);
}

export function formatCustomerCode(customerId: number | null | undefined): string {
  return formatCode('C', customerId);
}

export function formatEmployeeCode(userId: number | null | undefined): string {
  return formatCode('E', userId);
}
