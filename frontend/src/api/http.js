import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
});

let isRefreshing = false;
let refreshSubscribers = [];

// 请求拦截器：自动添加token
api.interceptors.request.use(
  (config) => {
    // 从cookie中获取token并添加到请求头
    const token = getCookie('ACCESS_TOKEN');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 工具函数：从cookie获取值
function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop().split(';').shift();
  return null;
}

// 订阅刷新结果：回调接收可选的 error 参数
const subscribeTokenRefresh = (callback) => {
  refreshSubscribers.push(callback);
};

const onRefreshed = () => {
  refreshSubscribers.forEach((callback) => callback());
  refreshSubscribers = [];
};

const onRefreshFailed = (error) => {
  refreshSubscribers.forEach((callback) => callback(error));
  refreshSubscribers = [];
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { response, config } = error;
    if (response?.status === 401) {
      const url = config?.url || '';

      // 对 /auth/* 的 401 不进行刷新尝试，避免无意义的额外请求
      if (url.startsWith('/auth/')) {
        return Promise.reject(error);
      }

      if (!config?._retry) {
        config._retry = true;

        if (!isRefreshing) {
          isRefreshing = true;
          try {
            await api.post('/auth/refresh-token');
            onRefreshed();
          } catch (refreshError) {
            onRefreshFailed(refreshError);
            throw refreshError;
          } finally {
            isRefreshing = false;
          }
        }

        return new Promise((resolve, reject) => {
          subscribeTokenRefresh(async (refreshError) => {
            if (refreshError) {
              // 刷新失败，直接拒绝原请求，避免无限挂起
              return reject(refreshError);
            }
            try {
              resolve(await api(config));
            } catch (err) {
              reject(err);
            }
          });
        });
      }
    }

    return Promise.reject(error);
  }
);

export default api;
