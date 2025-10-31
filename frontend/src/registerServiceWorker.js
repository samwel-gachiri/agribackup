/* eslint-disable no-console */
import { register } from 'register-service-worker';

if (process.env.NODE_ENV === 'production') {
  register(`${process.env.BASE_URL}service-worker.js`, {
    ready() {
      //   'App is being served from cache by a service worker.\n'
      //   + 'For more details, visit https://goo.gl/AFskqB',
      // );
    },
    registered() {
      // Service worker registered
    },
    cached() {
      // Content cached
    },
    updatefound() {
      // New content available
    },
    updated() {
      // Show a non-blocking banner to prompt user to refresh
      if (!document.getElementById('sw-update-banner')) {
        const banner = document.createElement('div');
        banner.id = 'sw-update-banner';
        banner.textContent = 'A new version is available. Click here to refresh.';
        banner.style.position = 'fixed';
        banner.style.bottom = '0';
        banner.style.left = '0';
        banner.style.width = '100%';
        banner.style.backgroundColor = '#2563eb'; // tw-blue-600
        banner.style.color = '#fff';
        banner.style.padding = '10px';
        banner.style.textAlign = 'center';
        banner.style.cursor = 'pointer';
        banner.style.zIndex = '9999';
        banner.onclick = () => window.location.reload();
        // document.body.appendChild(banner);
      }
    },
    offline() {
      // You can also notify the user that they are offline:
      const offlineBanner = document.createElement('div');
      offlineBanner.textContent = 'You are offline. Some features may not be available.';
      offlineBanner.style.position = 'fixed';
      offlineBanner.style.top = '0';
      offlineBanner.style.left = '0';
      offlineBanner.style.width = '100%';
      offlineBanner.style.backgroundColor = 'red';
      offlineBanner.style.color = 'white';
      offlineBanner.style.padding = '10px';
      offlineBanner.style.textAlign = 'center';
      document.body.appendChild(offlineBanner);
    },
    // eslint-disable-next-line no-unused-vars
    error(error) {
      // Service worker error
    },
  });
}
