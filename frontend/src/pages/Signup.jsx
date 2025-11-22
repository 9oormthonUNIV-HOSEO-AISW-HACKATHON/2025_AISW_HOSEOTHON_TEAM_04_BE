import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import './Signup.css';

const Signup = () => {
  const navigate = useNavigate();
  const { signup } = useAuth();
  const [formData, setFormData] = useState({
    loginId: '',
    password: '',
    passwordConfirm: '',
    name: '',
    birthYear: '',
    roleType: '',
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // 해당 필드의 에러 메시지 제거
    setErrors((prev) => ({
      ...prev,
      [name]: '',
    }));
  };

  const validateForm = () => {
    const newErrors = {};

    // 아이디 검증
    if (!formData.loginId) {
      newErrors.loginId = '아이디는 필수 값입니다.';
    }

    // 비밀번호 검증
    if (!formData.password) {
      newErrors.password = '비밀번호는 필수 값입니다.';
    } else if (formData.password.length < 6) {
      newErrors.password = '비밀번호는 최소 6자 이상이어야 합니다.';
    }

    // 비밀번호 확인 검증
    if (formData.password !== formData.passwordConfirm) {
      newErrors.passwordConfirm = '비밀번호가 일치하지 않습니다.';
    }

    // 이름 검증
    if (!formData.name) {
      newErrors.name = '이름은 필수 값입니다.';
    }

    // 출생연도 검증
    const birthYear = parseInt(formData.birthYear);
    if (!formData.birthYear) {
      newErrors.birthYear = '출생연도는 필수 값입니다.';
    } else if (birthYear < 1900 || birthYear > 2100) {
      newErrors.birthYear = '올바른 출생연도를 입력해주세요.';
    }

    // 역할 검증
    if (!formData.roleType) {
      newErrors.roleType = '역할을 선택해 주세요.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setLoading(true);
    try {
      const signupData = {
        loginId: formData.loginId,
        password: formData.password,
        name: formData.name,
        birthYear: parseInt(formData.birthYear),
        roleType: formData.roleType,
      };

      const result = await signup(signupData);
      if (result.success) {
        navigate('/');
      } else {
        setErrors({ general: result.error || '회원가입에 실패했습니다.' });
      }
    } catch (err) {
      setErrors({ general: '회원가입 중 오류가 발생했습니다.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="signup-container">
      <div className="signup-box">
        <h1 className="signup-title">회원가입</h1>
        <p className="signup-subtitle">FamilyQ에 오신 것을 환영합니다</p>

        <form onSubmit={handleSubmit} className="signup-form">
          <div className="form-group">
            <label className="form-label" htmlFor="loginId">
              아이디
            </label>
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
            {errors.loginId && (
              <div className="error-message">{errors.loginId}</div>
            )}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">
              비밀번호
            </label>
            <input
              type="password"
              id="password"
              name="password"
              className="form-control"
              value={formData.password}
              onChange={handleChange}
              placeholder="비밀번호를 입력하세요 (6자 이상)"
              disabled={loading}
            />
            {errors.password && (
              <div className="error-message">{errors.password}</div>
            )}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="passwordConfirm">
              비밀번호 확인
            </label>
            <input
              type="password"
              id="passwordConfirm"
              name="passwordConfirm"
              className="form-control"
              value={formData.passwordConfirm}
              onChange={handleChange}
              placeholder="비밀번호를 다시 입력하세요"
              disabled={loading}
            />
            {errors.passwordConfirm && (
              <div className="error-message">{errors.passwordConfirm}</div>
            )}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="name">
              이름
            </label>
            <input
              type="text"
              id="name"
              name="name"
              className="form-control"
              value={formData.name}
              onChange={handleChange}
              placeholder="이름을 입력하세요"
              disabled={loading}
            />
            {errors.name && <div className="error-message">{errors.name}</div>}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="birthYear">
              출생연도
            </label>
            <input
              type="number"
              id="birthYear"
              name="birthYear"
              className="form-control"
              value={formData.birthYear}
              onChange={handleChange}
              placeholder="예: 1990"
              min="1900"
              max="2100"
              disabled={loading}
            />
            {errors.birthYear && (
              <div className="error-message">{errors.birthYear}</div>
            )}
          </div>

          <div className="form-group">
            <label className="form-label">가족 내 역할</label>
            <div className="role-buttons">
              <button
                type="button"
                className={`role-button ${
                  formData.roleType === 'FATHER' ? 'active' : ''
                }`}
                onClick={() =>
                  handleChange({ target: { name: 'roleType', value: 'FATHER' } })
                }
                disabled={loading}
              >
                아버지
              </button>
              <button
                type="button"
                className={`role-button ${
                  formData.roleType === 'MOTHER' ? 'active' : ''
                }`}
                onClick={() =>
                  handleChange({ target: { name: 'roleType', value: 'MOTHER' } })
                }
                disabled={loading}
              >
                어머니
              </button>
              <button
                type="button"
                className={`role-button ${
                  formData.roleType === 'CHILD' ? 'active' : ''
                }`}
                onClick={() =>
                  handleChange({ target: { name: 'roleType', value: 'CHILD' } })
                }
                disabled={loading}
              >
                자녀
              </button>
            </div>
            {errors.roleType && (
              <div className="error-message">{errors.roleType}</div>
            )}
          </div>

          {errors.general && (
            <div className="error-message">{errors.general}</div>
          )}

          <button
            type="submit"
            className="btn-primary signup-button"
            disabled={loading}
          >
            {loading ? '가입 중...' : '회원가입'}
          </button>
        </form>

        <div className="signup-footer">
          <p>
            이미 계정이 있으신가요?{' '}
            <Link to="/login" className="login-link">
              로그인
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Signup;