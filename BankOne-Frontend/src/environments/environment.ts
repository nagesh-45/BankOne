import { APP_VERSION } from './version.generated';

export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:9080',
  /** From git at build/serve time (see scripts/generate-app-version.mjs). */
  appVersion: APP_VERSION
};
