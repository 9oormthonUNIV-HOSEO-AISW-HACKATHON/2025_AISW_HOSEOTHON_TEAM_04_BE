import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { questionAPI, familyAPI } from '../api';
import './Home.css';

const Home = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const showDebugControls = import.meta.env.DEV || import.meta.env.VITE_SHOW_DEBUG_BUTTONS === 'true';
  const [todayQuestion, setTodayQuestion] = useState(null);
  const [family, setFamily] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [startLoading, setStartLoading] = useState(false);
  const [startError, setStartError] = useState('');

  useEffect(() => {
    loadData();
  }, [user]);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    setStartError('');

    try {
      // 가족 정보 확인
      if (user?.familyId) {
        const familyData = await familyAPI.getMyFamily();
        setFamily(familyData);

        // 오늘의 질문 가져오기
        if (familyData.questionsStarted) {
          const questionData = await questionAPI.getTodayQuestion();
          setTodayQuestion(questionData);
        } else {
          setTodayQuestion(null);
        }
      }
    } catch (err) {
      if (err.response?.status !== 400) {
        setError('데이터를 불러오는 중 오류가 발생했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleAnswerSubmit = () => {
    if (todayQuestion) {
      navigate(`/questions/${todayQuestion.familyQuestionId}`);
    }
  };

  const handleRefreshQuestion = async () => {
    try {
      setLoading(true);
      await questionAPI.refreshQuestion(user.id);
      await loadData();
    } catch (err) {
      console.error('질문 새로고침 실패:', err);
      setError('질문 새로고침에 실패했습니다. 관리자 권한이 필요합니다.');
      setLoading(false);
    }
  };

  const handleSkipToNext = async () => {
    if (!window.confirm('현재 질문을 완료 처리하고 다음 질문으로 넘어갑니다. 계속하시겠습니까?')) {
      return;
    }

    try {
      setLoading(true);
      await questionAPI.skipToNextQuestion(user.id);
      await loadData();
    } catch (err) {
      console.error('다음 질문으로 건너뛰기 실패:', err);
      setError('다음 질문으로 건너뛰기에 실패했습니다. 관리자 권한이 필요합니다.');
      setLoading(false);
    }
  };

  const handleStartQuestions = async () => {
    setStartLoading(true);
    setStartError('');
    setError(null);
    try {
      const updatedFamily = await familyAPI.startQuestions();
      setFamily(updatedFamily);

      if (updatedFamily.questionsStarted) {
        const questionData = await questionAPI.getTodayQuestion();
        setTodayQuestion(questionData);
      }
    } catch (err) {
      setStartError(err.response?.data?.message || '질문을 시작하는 중 오류가 발생했습니다.');
    } finally {
      setStartLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="container">
        <div className="loading-spinner"></div>
      </div>
    );
  }

  // 가족이 없는 경우
  if (!user?.familyId || !family) {
    return (
      <div className="container">
        <div className="home-welcome">
          <h1>안녕하세요, {user?.name}님!</h1>
          <p className="welcome-subtitle">
            FamilyQ와 함께 가족과의 소중한 일상을 나누어보세요
          </p>

          <div className="no-family-card">
            <h2>아직 가족이 없으시네요!</h2>
            <p>가족을 생성하거나 초대 코드로 참여해보세요</p>
            <div className="action-buttons">
              <button
                onClick={() => navigate('/family')}
                className="btn-primary"
              >
                가족 관리로 이동
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const hasQuestion = Boolean(todayQuestion);
  const hasAnswered = Boolean(todayQuestion?.myAnswer);
  const isCompleted = Boolean(todayQuestion?.completed);
  const answeredCount = todayQuestion?.answeredCount ?? 0;
  const requiredCount = todayQuestion?.requiredMemberCount ?? 0;
  const questionText = todayQuestion?.questionText ?? '';
  const questionDate = todayQuestion?.assignedDate
    ? new Date(todayQuestion.assignedDate).toLocaleDateString()
    : '';
  const memberCount = family?.memberCount ?? family?.members?.length ?? 0;
  const canStartQuestions = family?.readyForQuestions;
  const hasStartedQuestions = family?.questionsStarted;

  // 오늘의 질문이 있는 경우
  return (
    <div className="container">
      <div className="home-content">
        <h1>안녕하세요, {user?.name}님!</h1>

        {family && (
          <div className="family-info-card">
            <h3>우리 가족</h3>
            <p>가족 코드: <span className="family-code">{family.familyCode}</span></p>
            <p>구성원: {memberCount}명</p>
          </div>
        )}

        {!hasStartedQuestions ? (
          <div className="no-question-card">
            <h2>가족이 다 가입했나요?</h2>
            <p>질문은 가족 구성원 2명 이상 모였을 때부터 시작돼요.</p>
            <p className="question-info">현재 {memberCount}명이 함께하고 있어요.</p>
            <button
              onClick={handleStartQuestions}
              className="btn-primary answer-btn"
              disabled={!canStartQuestions || startLoading}
            >
              {startLoading ? '시작 중...' : '질문 받기 시작하기'}
            </button>
            {!canStartQuestions && (
              <p className="question-info">최소 2명이 모이면 버튼이 활성화됩니다.</p>
            )}
            {startError && <div className="error-message">{startError}</div>}
          </div>
        ) : hasQuestion ? (
          <div className="question-card">
            <div className="question-header">
              <h2>오늘의 질문</h2>
              <span className="question-date">
                {questionDate}
              </span>
            </div>

            <div className="question-content">
              <p className="question-text">
                {questionText}
              </p>
            </div>

            <div className="question-status">
              {hasAnswered ? (
                <div className="status-answered">
                  <span>✅ 답변 완료</span>
                  {isCompleted && (
                    <p className="insight-available">
                      모든 가족이 답변을 완료했습니다! 인사이트를 확인해보세요.
                    </p>
                  )}
                </div>
              ) : (
                <div className="status-pending">
                  <span>답변을 기다리고 있어요</span>
                </div>
              )}
              </div>

              <div className="question-actions">
              {!hasAnswered ? (
                <button
                  onClick={handleAnswerSubmit}
                  className="btn-primary answer-btn"
                >
                  답변하기
                </button>
              ) : (
                <button
                  onClick={handleAnswerSubmit}
                  className="btn-secondary view-btn"
                >
                  답변 보기
                </button>
              )}
            </div>

            {requiredCount > 0 && (
              <div className="progress-info">
                <p>진행 상황: {answeredCount} / {requiredCount}명 답변 완료</p>
                <div className="progress-bar">
                  <div
                    className="progress-fill"
                    style={{
                      width: `${Math.min(100, (answeredCount / requiredCount) * 100)}%`
                    }}
                  ></div>
                </div>
              </div>
            )}

            {/* 디버깅용 버튼 */}
            {showDebugControls && (
              <div style={{ marginTop: '20px', borderTop: '1px solid #e0e0e0', paddingTop: '20px' }}>
                <small style={{ color: '#999', display: 'block', marginBottom: '10px' }}>
                  테스트 도구 (다음 질문 넘기기/새로고침)
                </small>
                <button
                  onClick={handleSkipToNext}
                  className="btn-secondary"
                  style={{ marginRight: '10px' }}
                >
                  다음 질문으로 건너뛰기
                </button>
                <button
                  onClick={handleRefreshQuestion}
                  className="btn-secondary"
                >
                  질문 새로고침
                </button>
              </div>
            )}
          </div>
        ) : (
          <div className="no-question-card">
            <p>오늘은 아직 질문이 없습니다.</p>
            <p className="question-info">
              가족 구성원이 모두 답변을 완료하면 다음 질문이 생성됩니다.
            </p>

            {/* 디버깅용 버튼 */}
            {showDebugControls && (
              <div style={{ marginTop: '20px' }}>
                <button
                  onClick={handleRefreshQuestion}
                  className="btn-secondary"
                  style={{ marginRight: '10px' }}
                >
                  질문 새로고침 (디버깅)
                </button>
              </div>
            )}
          </div>
        )}

        <div className="quick-links">
          <button
            onClick={() => navigate('/questions')}
            className="link-card"
          >
            <h3>질문 히스토리</h3>
            <p>지난 질문과 답변들을 확인해보세요</p>
          </button>

          <button
            onClick={() => navigate('/family')}
            className="link-card"
          >
            <h3>가족 관리</h3>
            <p>가족 구성원을 확인하고 초대하세요</p>
          </button>
        </div>
      </div>
    </div>
  );
};

export default Home;
