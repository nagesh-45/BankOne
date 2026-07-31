import { APP_VERSION } from './version.generated';

export const environment = {
  production: true,
  apiBaseUrl: 'https://bankone-api-123.onrender.com',
  /** From git at build time (see scripts/generate-app-version.mjs). */
  appVersion: APP_VERSION
};
