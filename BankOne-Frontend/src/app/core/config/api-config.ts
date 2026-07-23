/**
 * API base URL for this machine's Open Liberty.
 *
 * Use the Mac's LAN IP so other devices on the same Wi‑Fi/LAN can call the API.
 * If your IP changes (ipconfig getifaddr en0), update this and restart ng serve.
 * Then redeploy Liberty if you also change CORS in SecurityConfig.
 */
export const API_BASE_URL = 'http://192.168.0.4:9080';
