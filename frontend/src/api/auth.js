import apiClient from './axios';

const authAPI = {
  // 회원가입
  signup: async (signupData) => {
    const response = await apiClient.post('/auth/signup', signupData);
    return response.data;
  },

  // 로그인
  login: async (loginData) => {
    const response = await apiClient.post('/auth/login', loginData);
    return response.data;
  },

  // 로그아웃
  logout: async () => {
    const response = await apiClient.post('/auth/logout');
    return response.data;
  },
};

export default authAPI;