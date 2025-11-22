import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { familyAPI } from '../api';
import './Family.css';

const Family = () => {
  const { user, fetchUser } = useAuth();
  const [family, setFamily] = useState(null);
  const [familyCode, setFamilyCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [copySuccess, setCopySuccess] = useState(false);

  useEffect(() => {
    if (user?.familyId) {
      loadFamilyData();
    }
  }, [user?.familyId]);

  const loadFamilyData = async () => {
    try {
      const data = await familyAPI.getMyFamily();
      setFamily(data);
    } catch (err) {
      setError('가족 정보를 불러오는데 실패했습니다.');
    }
  };

  const handleCreateFamily = async () => {
    setLoading(true);
    setError('');
    setSuccessMessage('');

    try {
      const response = await familyAPI.createFamily();
      setFamily(response);
      setSuccessMessage(`가족이 생성되었습니다! 가족 코드: ${response.familyCode}`);
      await fetchUser(); // 사용자 정보 업데이트
    } catch (err) {
      setError(err.response?.data?.message || '가족 생성에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleJoinFamily = async (e) => {
    e.preventDefault();
    if (!familyCode.trim()) {
      setError('가족 코드를 입력해주세요.');
      return;
    }

    setLoading(true);
    setError('');
    setSuccessMessage('');

    try {
      const response = await familyAPI.joinFamily(familyCode);
      setFamily(response);
      setFamilyCode('');
      setSuccessMessage('가족에 성공적으로 참여했습니다!');
      await fetchUser(); // 사용자 정보 업데이트
    } catch (err) {
      setError(err.response?.data?.message || '가족 참여에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const copyFamilyCode = () => {
    navigator.clipboard.writeText(family.familyCode).then(() => {
      setCopySuccess(true);
      setTimeout(() => setCopySuccess(false), 2000);
    });
  };

  // 가족이 없는 경우
  if (!user?.familyId && !family) {
    return (
      <div className="container">
        <div className="family-page">
          <h1>가족 관리</h1>

          <div className="no-family-section">
            <div className="create-family-card">
              <h2>새로운 가족 만들기</h2>
              <p>가족을 생성하고 가족 구성원들을 초대해보세요</p>
              <button
                onClick={handleCreateFamily}
                disabled={loading}
                className="btn-primary create-btn"
              >
                {loading ? '생성 중...' : '가족 생성하기'}
              </button>
            </div>

            <div className="divider">또는</div>

            <div className="join-family-card">
              <h2>기존 가족 참여하기</h2>
              <p>가족 구성원으로부터 받은 초대 코드를 입력하세요</p>
              <form onSubmit={handleJoinFamily}>
                <input
                  type="text"
                  value={familyCode}
                  onChange={(e) => setFamilyCode(e.target.value.toUpperCase())}
                  placeholder="가족 코드 입력 (예: ABC123)"
                  className="form-control code-input"
                  disabled={loading}
                  maxLength={10}
                />
                <button
                  type="submit"
                  disabled={loading}
                  className="btn-primary join-btn"
                >
                  {loading ? '참여 중...' : '가족 참여하기'}
                </button>
              </form>
            </div>

            {error && <div className="error-message">{error}</div>}
            {successMessage && <div className="success-message">{successMessage}</div>}
          </div>
        </div>
      </div>
    );
  }

  // 가족이 있는 경우
  return (
    <div className="container">
      <div className="family-page">
        <h1>우리 가족</h1>

        <div className="family-info-section">
          <div className="family-code-card">
            <h2>가족 초대 코드</h2>
            <div className="code-display">
              <span className="code-text">{family?.familyCode}</span>
              <button
                onClick={copyFamilyCode}
                className="copy-btn"
                title="코드 복사"
              >
                {copySuccess ? '✅ 복사됨' : '📋 복사'}
              </button>
            </div>
            <p className="code-hint">이 코드를 가족들에게 공유하세요</p>
          </div>

          <div className="family-members-card">
            <h2>가족 구성원</h2>
            <div className="members-list">
              {family?.members && family.members.length > 0 ? (
                family.members.map((member) => (
                  <div key={member.id} className="member-item">
                    <div className="member-avatar">
                      {member.roleType === 'FATHER' ? '👨' :
                       member.roleType === 'MOTHER' ? '👩' : '👦'}
                    </div>
                    <div className="member-info">
                      <div className="member-name">{member.name}</div>
                      <div className="member-role">
                        {member.roleType === 'FATHER' ? '아버지' :
                         member.roleType === 'MOTHER' ? '어머니' : '자녀'}
                        {member.id === user?.id && ' (나)'}
                      </div>
                    </div>
                    <div className="member-year">{member.birthYear}년생</div>
                  </div>
                ))
              ) : (
                <p>가족 구성원이 없습니다.</p>
              )}
            </div>
          </div>

          <div className="family-stats-card">
            <h2>가족 통계</h2>
            <div className="stats-grid">
              <div className="stat-item">
                <div className="stat-number">{family?.members?.length || 0}</div>
                <div className="stat-label">가족 구성원</div>
              </div>
              <div className="stat-item">
                <div className="stat-number">{family?.completedQuestions || 0}</div>
                <div className="stat-label">완료한 질문</div>
              </div>
              <div className="stat-item">
                <div className="stat-number">{family?.totalAnswers || 0}</div>
                <div className="stat-label">총 답변 수</div>
              </div>
            </div>
          </div>
        </div>

        {error && <div className="error-message">{error}</div>}
        {successMessage && <div className="success-message">{successMessage}</div>}
      </div>
    </div>
  );
};

export default Family;