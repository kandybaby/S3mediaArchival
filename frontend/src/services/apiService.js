// src/utils/api.js
import axios from 'axios';

const api = axios.create({
  baseURL: '/api'
});

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });

  failedQueue = [];
};

api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['token'] = token;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);


api.interceptors.response.use(
  response => response,
  async error => {
    if ((error.response.status === 401 || error.response.status === 403) && !error.config._retry) {
      error.config._retry = true;

      if (error.config.url === '/users/refresh-token') {
        localStorage.removeItem('token');
        window.location.href = '/login';
        return Promise.reject(error);
      }

      if (!isRefreshing) {
        isRefreshing = true;

        api.post('/users/refresh-token')
          .then(({ data }) => {
            isRefreshing = false;
            localStorage.setItem('token', data.JWT);
            error.config.headers['token'] = data.JWT;
            processQueue(null, data.JWT);

            return api(error.config);
          })
          .catch(refreshError => {
            processQueue(refreshError, null);
            isRefreshing = false;
            localStorage.removeItem('token');
            window.location.href = '/login';
            return Promise.reject(refreshError);
          });
      }

      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      }).then(token => {
        error.config.headers['token'] = token;
        return api(error.config);
      }).catch(error => {
        return Promise.reject(error);
      });

    }

    return Promise.reject(error);
  }
);



export default api;
