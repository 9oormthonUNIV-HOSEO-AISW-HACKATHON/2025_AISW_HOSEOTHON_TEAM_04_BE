import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import styles from './Login.module.css';

const Login = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [formData, setFormData] = useState({ loginId: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!formData.loginId || !formData.password) {
      setError('아이디와 비밀번호를 입력해주세요.');
      return;
    }
    setLoading(true);
    try {
      const result = await login(formData);
      if (result.success) {
        navigate('/');
      } else {
        setError(result.error || '로그인에 실패했습니다.');
      }
    } catch {
      setError('로그인 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.box}>
        <p className={styles.kicker}>FamilyQ</p>
        <h1 className={styles.title}>가족과 하루 한 번 마음을 나눠요</h1>
        <p className={styles.subtitle}>따뜻한 질문으로 대화를 시작해 보세요.</p>

        <form onSubmit={handleSubmit} className={styles.form}>
          <div className="form-group">
            <label className="form-label" htmlFor="loginId">아이디</label>
            <input
              type="text"
              id="loginId"
              name="loginId"
              className="form-control"
              value={formData.loginId}
              onChange={handleChange}
              placeholder="아이디를 입력하세요"
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">비밀번호</label>
            <input
              type="password"
              id="password"
              name="password"
              className="form-control"
              value={formData.password}
              onChange={handleChange}
              placeholder="비밀번호를 입력하세요"
              disabled={loading}
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button type="submit" className={styles.primary} disabled={loading}>
            {loading ? '로그인 중...' : '들어가기'}
          </button>
        </form>

        <div className={styles.footer}>
          <span>계정이 없으신가요?</span>
          <Link to="/signup" className={styles.link}>회원가입</Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
