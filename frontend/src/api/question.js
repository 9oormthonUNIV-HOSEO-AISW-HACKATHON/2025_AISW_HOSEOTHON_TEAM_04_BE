import apiClient from './axios';

const questionAPI = {
  // 오늘의 질문 조회
  getTodayQuestion: async () => {
    const response = await apiClient.get('/questions/today');
    return response.data;
  },

  // 질문 히스토리 조회
  getHistory: async () => {
    const response = await apiClient.get('/questions/history');
    return response.data;
  },

  // 질문 상세 조회
  getQuestionDetail: async (familyQuestionId) => {
    const response = await apiClient.get(`/questions/${familyQuestionId}`);
    return response.data;
  },

  // 답변 제출
  submitAnswer: async (familyQuestionId, answerData) => {
    const response = await apiClient.post(`/questions/${familyQuestionId}/answers`, answerData);
    return response.data;
  },

  // 관리자용 질문 목록
  getAdminQuestions: async () => {
    const response = await apiClient.get('/admin/questions');
    return response.data;
  },

  // 관리자용 질문 생성
  createAdminQuestion: async (questionData) => {
    const response = await apiClient.post('/admin/questions', questionData);
    return response.data;
  },

  // 관리자용 질문 삭제
  deleteAdminQuestion: async (questionId) => {
    const response = await apiClient.delete(`/admin/questions/${questionId}`);
    return response.data;
  },

  // 디버깅용 - 질문 새로고침
  refreshQuestion: async (userId) => {
    const response = await apiClient.post(`/admin/questions/refresh/${userId}`);
    return response.data;
  },

  // 디버깅용 - 다음 질문으로 건너뛰기
  skipToNextQuestion: async (userId) => {
    const response = await apiClient.post(`/admin/questions/skip-to-next/${userId}`);
    return response.data;
  },
};

export default questionAPI;
