import { environment } from '../../environments/environment';

/**
 * API base URL.
 * - Local/dev: src/environments/environment.ts
 * - Production build: environment.prod.ts (file replacement)
 * - Docker web image: can overwrite environment.prod.ts at build with --build-arg
 */
export const API_BASE_URL = environment.apiBaseUrl;
