import apiClient from './axios';

const userAPI = {
  // 내 정보 조회
  getMe: async () => {
    const response = await apiClient.get('/users/me');
    return response.data;
  },
};

export default userAPI;