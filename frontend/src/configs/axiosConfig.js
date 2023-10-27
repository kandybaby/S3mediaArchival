import axios from 'axios';
import { refreshTokenLogic } from '@/services/authService';  // Adjust the import path accordingly

axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['token'] = token;
    }
    return config;
  },
  error => Promise.reject(error)
);

axios.interceptors.response.use(
  response => response,
  async error => {
    // If a 401 response is received and the request has not been retried
    if (error.response.status === 401 && !error.config._retry) {
      error.config._retry = true;

      const newToken = await refreshTokenLogic();

      if (newToken) {
        error.config.headers['token'] = newToken;
        return axios(error.config);
      } else {
        // Clear the token from local storage and potentially redirect to login page
        localStorage.removeItem('token');
        window.location.href = '/LoginPage'; // Redirect to login page
        return Promise.reject(error);
      }
    }
    return Promise.reject(error);
  }
);
