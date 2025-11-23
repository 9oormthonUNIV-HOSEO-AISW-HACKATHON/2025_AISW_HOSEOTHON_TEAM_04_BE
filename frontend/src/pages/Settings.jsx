import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { familyAPI } from '../api';
import styles from './Settings.module.css';

const Settings = () => {
  const { user, fetchUser, logout } = useAuth();
  const navigate = useNavigate();
  const [family, setFamily] = useState(null);
  const [familyCode, setFamilyCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [startLoading, setStartLoading] = useState(false);
  const [copySuccess, setCopySuccess] = useState(false);

  useEffect(() => {
    if (user?.familyId) {
      loadFamily();
    } else {
      setFamily(null);
    }
  }, [user?.familyId]);

  const loadFamily = async () => {
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
    setSuccess('');
    try {
      const response = await familyAPI.createFamily();
      setFamily(response);
      setSuccess('가족이 생성되었어요. 초대 코드를 공유해보세요.');
      await fetchUser();
      await loadFamily();
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
    setSuccess('');
    try {
      const response = await familyAPI.joinFamily(familyCode.trim());
      setFamily(response);
      setFamilyCode('');
      setSuccess('가족에 참여했어요!');
      await fetchUser();
      await loadFamily();
    } catch (err) {
      setError(err.response?.data?.message || '가족 참여에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleStartQuestions = async () => {
    setStartLoading(true);
    setError('');
    setSuccess('');
    try {
      const response = await familyAPI.startQuestions();
      setFamily(response);
      setSuccess('질문을 시작했어요! 홈에서 오늘의 질문을 확인해보세요.');
    } catch (err) {
      setError(err.response?.data?.message || '질문 시작에 실패했습니다.');
    } finally {
      setStartLoading(false);
    }
  };

  const copyFamilyCode = () => {
    if (!family?.familyCode) return;
    navigator.clipboard.writeText(family.familyCode).then(() => {
      setCopySuccess(true);
      setTimeout(() => setCopySuccess(false), 1800);
    });
  };

  const handleLogout = async () => {
    const result = await logout();
    if (result.success) {
      navigate('/login');
    }
  };

  const memberCount = family?.memberCount ?? family?.members?.length ?? 0;

  return (
    <div className="container">
      <div className={styles.page}>
        <header className={styles.header}>
          <div>
            <p className={styles.kicker}>설정</p>
            <h1 className={styles.title}>가족과 계정</h1>
          </div>
        </header>

        <section className={styles.card}>
          <h3 className={styles.sectionTitle}>프로필</h3>
          <div>
            <p className={styles.name}>{user?.name}</p>
            <p className={styles.helper}>연속 답변 목표를 이어가볼까요?</p>
          </div>
        </section>

        {!family && (
          <section className={styles.card}>
            <h3 className={styles.sectionTitle}>가족 시작하기</h3>
            <div className={styles.actions}>
              <button className={styles.primary} onClick={handleCreateFamily} disabled={loading}>
                {loading ? '생성 중...' : '새 가족 만들기'}
              </button>
              <div className={styles.divider}>또는</div>
              <form className={styles.joinForm} onSubmit={handleJoinFamily}>
                <input
                  type="text"
                  value={familyCode}
                  onChange={(e) => setFamilyCode(e.target.value.toUpperCase())}
                  placeholder="초대 코드 입력"
                  className="form-control"
                  disabled={loading}
                  maxLength={10}
                />
                <button type="submit" className={styles.secondary} disabled={loading}>
                  {loading ? '참여 중...' : '가족 참여하기'}
                </button>
              </form>
            </div>
            {error && <div className="error-message">{error}</div>}
            {success && <div className="success-message">{success}</div>}
          </section>
        )}

        {family && (
          <>
            <section className={styles.card}>
              <div className={styles.cardHeader}>
                <h3 className={styles.sectionTitle}>가족 구성원</h3>
                <span className={styles.badge}>{memberCount}명</span>
              </div>
              <div className={styles.members}>
                {family.members?.map((member) => (
                  <div key={member.id} className={styles.member}>
                    <div className={styles.memberAvatar}>
                      {member.name?.[0] || 'F'}
                    </div>
                    <div>
                      <p className={styles.name}>{member.name}</p>
                      <p className={styles.helper}>
                        {member.roleType === 'FATHER'
                          ? '아버지'
                          : member.roleType === 'MOTHER'
                            ? '어머니'
                            : '자녀'}
                        {member.id === user?.id ? ' (나)' : ''}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </section>

            <section className={styles.card}>
              <h3 className={styles.sectionTitle}>가족 통계</h3>
              <div className={styles.stats}>
                <div className={styles.stat}>
                  <p className={styles.statLabel}>완료한 질문</p>
                  <p className={styles.statValue}>{family.completedQuestions || 0}</p>
                </div>
                <div className={styles.stat}>
                  <p className={styles.statLabel}>총 답변 수</p>
                  <p className={styles.statValue}>{family.totalAnswers || 0}</p>
                </div>
                <div className={styles.stat}>
                  <p className={styles.statLabel}>구성원</p>
                  <p className={styles.statValue}>{memberCount}</p>
                </div>
              </div>
            </section>

            <section className={styles.card}>
              <div className={styles.cardHeader}>
                <h3 className={styles.sectionTitle}>가족 초대 코드</h3>
                <button className={styles.ghost} onClick={copyFamilyCode}>
                  {copySuccess ? '복사됨' : '복사'}
                </button>
              </div>
              <div className={styles.codeBox}>{family.familyCode}</div>
              <p className={styles.helper}>가족에게 코드를 공유해 주세요.</p>
            </section>

            {!family.questionsStarted && (
              <section className={styles.card}>
                <h3 className={styles.sectionTitle}>가족이 다 가입했나요?</h3>
                <p className={styles.helper}>2명 이상 모이면 질문을 시작할 수 있어요.</p>
                <button
                  className={styles.primary}
                  onClick={handleStartQuestions}
                  disabled={!family.readyForQuestions || startLoading}
                >
                  {startLoading ? '시작 중...' : '질문 받기 시작하기'}
                </button>
                {!family.readyForQuestions && (
                  <p className={styles.helper}>한 명 더 초대하면 시작할 수 있어요.</p>
                )}
              </section>
            )}
          </>
        )}

        <section className={styles.card}>
          <button className={styles.logout} onClick={handleLogout}>
            ↪ 로그아웃
          </button>
        </section>

        {error && family && <div className="error-message">{error}</div>}
        {success && family && <div className="success-message">{success}</div>}
      </div>
    </div>
  );
};

export default Settings;
