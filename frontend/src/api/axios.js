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
    // 모든 요청에 withCredentials 명시적 설정 (쿠키 포함 보장)
    config.withCredentials = true;
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 쿠키 삭제 헬퍼 함수
const clearSessionCookie = () => {
  // JSESSIONID 쿠키 삭제 (백엔드 설정과 동일하게 SameSite=None 사용)
  // 브라우저가 크로스 사이트 쿠키를 제대로 삭제할 수 있도록 설정
  document.cookie = 'JSESSIONID=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC; SameSite=None; Secure';
  // 다른 가능한 경로들에 대해서도 시도
  document.cookie = 'JSESSIONID=; path=/api; expires=Thu, 01 Jan 1970 00:00:00 UTC; SameSite=None; Secure';
};

// 응답 인터셉터
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // 401 에러 처리 (인증 실패)
    if (error.response?.status === 401) {
      // 유효하지 않은 세션 쿠키 정리
      clearSessionCookie();

      // 로그인 페이지가 아닌 경우에만 리다이렉트
      // (로그인 페이지에서의 401은 정상적인 동작일 수 있음)
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
