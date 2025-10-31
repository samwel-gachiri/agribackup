/* eslint-disable sonarjs/no-duplicate-string,no-param-reassign */
/* eslint no-unused-vars:0 */
/* eslint no-undef:0 */
import axios from 'axios';
import jwtDecode from 'jwt-decode';
import NProgress from 'nprogress';

export default {
  init(router, store) {
    axios.interceptors.request.use(async (config) => {
      NProgress.configure({ showSpinner: false });
      NProgress.start();
      if (store.getters['auth/isAuthenticated']) {
        const token = store.getters['auth/accessToken'];
        const { exp } = jwtDecode(token);

        if (Date.now() > exp * 1000) {
          try {
            await store.dispatch('auth/refresh').then(() => {
              config.headers.Authorization = `Bearer ${store.getters['auth/accessToken']}`;
            });
          } catch (e) {
            await store.dispatch('auth/signOut')
              .then(() => {
                router.push({ name: 'SignIn', query: { r: btoa(window.location.href) } });
              });
          }
        } else {
          config.headers.Authorization = `Bearer ${token}`;
        }
      }
      // if (process.env.NODE_ENV === 'production') config.baseURL = '/api';
      config.baseURL = process.env.VUE_APP_API_BASE_URL;
      return config;
    });

    axios.interceptors.response.use((response) => {
      NProgress.done();
      // if (typeof response.data !== 'string' && ('success' in response.data && !response.data.success && response.config.method === 'get' && !response.config.url.includes('notification') && !response.config.url.includes('clientQuotes') && !response.config.url.includes('query-gis'))) {
      //   router.push({ name: 'NotFound' });
      // }
      return response;
    });

    axios.interceptors.response.use(null, async (error) => {
      if (error.response) {
        /*
        * The request was made and the server responded with a
        * status code that falls out of the range of 2xx
        */
        if (error.response.status === 401) {
          const config = { retryAttempts: 1, ...error.config };

          try {
            // attempt to refresh access token using refresh token
            await store.dispatch('auth/refresh');
            // re-run the initial request using the new request config after a successful refresh
            // this response will be returned to the initial calling method
            return resolve(axios(config));
          } catch (e) {
            // catch any error while refreshing the token
            await store.dispatch('auth/signOut')
              .then(() => {
                router.push({ name: 'SignIn', query: { r: btoa(window.location.href) } });
              });
          }
        }
        if (error.response.status === 500 || error.response.status === 502 || error.response.status === 504 || error.response.status === 403) {
          NProgress.done();
          // router.push({ name: 'ServerError' });
        }
      } else if (error.request) {
        /*
        * The request was made but no response was received, `error.request`
        * is an instance of XMLHttpRequest in the browser and an instance
        * of http.ClientRequest in Node.js
        */
        // router.push({ name: 'NoInternet' });
        NProgress.done();
      } else {
        // * Something happened in setting up the request and triggered an Error
        // router.push({ name: 'NoInternet' });
        NProgress.done();
      }
      return Promise.reject(error);
    });

    router.beforeResolve((to, from, next) => {
      if (to.path) {
        NProgress.configure({ showSpinner: false });
        NProgress.start();
      }
      next();
    });

    router.afterEach((to, from) => {
      NProgress.done();
    });
  },
};
