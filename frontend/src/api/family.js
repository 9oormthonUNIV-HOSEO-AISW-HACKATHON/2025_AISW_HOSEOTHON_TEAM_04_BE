import apiClient from './axios';

const familyAPI = {
  // 가족 생성
  createFamily: async () => {
    const response = await apiClient.post('/families');
    return response.data;
  },

  // 가족 참여
  joinFamily: async (familyCode) => {
    const response = await apiClient.post('/families/join', { familyCode });
    return response.data;
  },

  // 내 가족 정보 조회
  getMyFamily: async () => {
    const response = await apiClient.get('/families/me');
    return response.data;
  },
};

export default familyAPI;