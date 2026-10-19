const CACHE_NAME = 'adv-clerk-pwa-v7-1365';
const ASSETS_TO_CACHE = ['./', './index.html'];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(ASSETS_TO_CACHE)).then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key)));
    }).then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') return;
  event.respondWith(
    fetch(event.request).then((response) => {
      return caches.open(CACHE_NAME).then((cache) => {
        if (event.request.url.startsWith(self.location.origin)) {
          cache.put(event.request, response.clone());
        }
        return response;
      });
    }).catch(() => caches.match(event.request).then((cached) => cached || caches.match('./index.html')))
  );
});
