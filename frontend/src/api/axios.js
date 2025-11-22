import axios from 'axios';

const API_ORIGIN = (
  import.meta.env.VITE_API_BASE_URL || 'https://familyqapi.hibiscus.biz'
).replace(/\/$/, '');

// axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: `${API_ORIGIN}/api`,
  withCredentials: true, // 세션 쿠키 포함
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터
apiClient.interceptors.request.use(
  (config) => {
    // 요청 전 처리
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // 401 에러 처리 (인증 실패)
    if (error.response?.status === 401) {
      // 로그인 페이지로 리다이렉트
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
