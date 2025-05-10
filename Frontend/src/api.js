import axios from 'axios';
import keycloak from './keycloak';

const api = axios.create({
  baseURL: 'http://localhost:8081/api',
});

api.interceptors.request.use(config => {
    const token = keycloak.token;  
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });
export default api;
