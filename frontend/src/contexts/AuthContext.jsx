import React, { createContext, useState, useContext, useEffect } from 'react';
import { authAPI, userAPI } from '../api';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // 사용자 정보 가져오기
  const fetchUser = async () => {
    try {
      const userData = await userAPI.getMe();
      setUser(userData);
      setError(null);
    } catch (err) {
      setUser(null);
      // 401 에러는 로그인하지 않은 상태이므로 에러로 처리하지 않음
      if (err.response?.status !== 401) {
        setError(err.message);
      }
    }
  };

  // 컴포넌트 마운트 시 사용자 정보 확인
  useEffect(() => {
    const checkAuth = async () => {
      setLoading(true);
      await fetchUser();
      setLoading(false);
    };
    checkAuth();
  }, []);

  // 로그인
  const login = async (loginData) => {
    try {
      const userData = await authAPI.login(loginData);
      setUser(userData);
      setError(null);
      return { success: true };
    } catch (err) {
      setError(err.response?.data?.message || '로그인에 실패했습니다.');
      return {
        success: false,
        error: err.response?.data?.message || '로그인에 실패했습니다.'
      };
    }
  };

  // 회원가입
  const signup = async (signupData) => {
    try {
      const userData = await authAPI.signup(signupData);
      setUser(userData);
      setError(null);
      return { success: true };
    } catch (err) {
      setError(err.response?.data?.message || '회원가입에 실패했습니다.');
      return {
        success: false,
        error: err.response?.data?.message || '회원가입에 실패했습니다.'
      };
    }
  };

  // 로그아웃
  const logout = async () => {
    try {
      await authAPI.logout();
      setUser(null);
      setError(null);
      return { success: true };
    } catch (err) {
      setError(err.response?.data?.message || '로그아웃에 실패했습니다.');
      return {
        success: false,
        error: err.response?.data?.message || '로그아웃에 실패했습니다.'
      };
    }
  };

  const value = {
    user,
    loading,
    error,
    login,
    signup,
    logout,
    fetchUser,
    isAuthenticated: !!user,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};