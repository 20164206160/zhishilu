import axios from 'axios';
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { useRouter } from 'vue-router';

// 创建 axios 实例
const instance: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
});

// 请求拦截器：自动添加 Token
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器：统一处理错误
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  (error) => {
    if (error.response) {
      const status = error.response.status;
      const message = error.response.data?.message || '请求失败';
      
      switch (status) {
        case 401:
          // Token失效，清除并跳转登录
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          // 注意：这里不直接跳转，由组件处理
          break;
        case 403:
          console.error('权限不足');
          break;
        case 404:
          console.error('请求的资源不存在');
          break;
        case 500:
          console.error('服务器内部错误');
          break;
        default:
          console.error(message);
      }
    } else {
      console.error('网络错误，请检查网络连接');
    }
    return Promise.reject(error);
  }
);

export default instance;
