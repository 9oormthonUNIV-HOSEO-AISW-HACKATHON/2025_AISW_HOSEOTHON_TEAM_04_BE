import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { questionAPI, familyAPI } from '../api';
import styles from './Home.module.css';

const Home = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [todayQuestion, setTodayQuestion] = useState(null);
  const [family, setFamily] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      setError(null);
      try {
        if (user?.familyId) {
          const familyData = await familyAPI.getMyFamily();
          setFamily(familyData);
          if (familyData.questionsStarted) {
            const questionData = await questionAPI.getTodayQuestion();
            setTodayQuestion(questionData);
          } else {
            setTodayQuestion(null);
          }
        } else {
          setFamily(null);
          setTodayQuestion(null);
        }
      } catch (err) {
        if (err.response?.status !== 400) {
          setError('데이터를 불러오는 중 문제가 발생했어요.');
        }
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, [user]);

  if (loading) {
    return (
      <div className="container">
        <div className="loading-spinner" />
      </div>
    );
  }

  if (!user?.familyId || !family) {
    return (
      <div className="container">
        <div className={styles.page}>
          <header className={styles.hero}>
            <p className={styles.kicker}>오늘의 가족 질문</p>
            <h1 className={styles.title}>안녕하세요, {user?.name}님</h1>
            <p className={styles.subtitle}>가족과 하루 한 번 마음을 나눠요.</p>
          </header>
          <section className={styles.card}>
            <h2 className={styles.sectionTitle}>아직 가족이 없어요</h2>
            <p className={styles.helper}>가족을 만들거나 초대 코드로 참여해 주세요.</p>
            <button className={styles.primary} onClick={() => navigate('/settings')}>
              설정에서 가족 시작하기
            </button>
          </section>
        </div>
      </div>
    );
  }

  const hasStarted = family?.questionsStarted;
  const hasQuestion = Boolean(todayQuestion);
  const hasAnswered = Boolean(todayQuestion?.myAnswer);
  const answeredCount = todayQuestion?.answeredCount ?? 0;
  const requiredCount = todayQuestion?.requiredMemberCount ?? family?.memberCount ?? 0;
  const dayLabel = todayQuestion?.sequenceNumber ? `Day ${todayQuestion.sequenceNumber}` : 'Day -';
  const dateLabel = todayQuestion?.assignedDate
    ? new Date(todayQuestion.assignedDate).toLocaleDateString()
    : '';
  const progressText =
    requiredCount > 0
      ? answeredCount === 0
        ? '아직 아무도 답변하지 않았어요. 먼저 마음을 열어볼까요?'
        : `지금까지 ${answeredCount}/${requiredCount}명이 답변했어요.`
      : '';

  return (
    <div className="container">
      <div className={styles.page}>
        <header className={styles.hero}>
          <p className={styles.kicker}>오늘, 우리 가족에게 온 질문</p>
          <p className={styles.subtitle}>하루 한 번, 가족의 마음을 기록해요.</p>
          {error && <p className={styles.error}>{error}</p>}
        </header>

        {!hasStarted && (
          <section className={styles.notice}>
            <h2 className={styles.sectionTitle}>가족이 다 모이면 시작해요</h2>
            <p className={styles.helper}>
              최소 2명이 모이면 질문이 열립니다. 설정에서 시작할 수 있어요.
            </p>
            <button className={styles.secondary} onClick={() => navigate('/settings')}>
              설정으로 이동
            </button>
          </section>
        )}

        {hasStarted && hasQuestion ? (
          <section className={styles.card}>
            <div className={styles.metaRow}>
              <div className={styles.metaLeft}>
                <span className={styles.day}>{dayLabel}</span>
                {dateLabel && <span className={styles.date}>{dateLabel}</span>}
              </div>
              {requiredCount > 0 && (
                <span className={styles.progress}>
                  {answeredCount}/{requiredCount}명 참여 중
                </span>
              )}
            </div>
            <div className={styles.divider} />
            <h2 className={styles.question}>{todayQuestion.questionText}</h2>
            <p className={styles.helper}>
              {hasAnswered ? '내 답변을 확인하거나 고쳐볼까요?' : '가족에게 마음을 들려주세요.'}
            </p>
            <button
              className={styles.primary}
              onClick={() => navigate(`/questions/${todayQuestion.familyQuestionId}`)}
            >
              {hasAnswered ? '답변 이어보기' : '지금 답변하기'}
            </button>
            {todayQuestion.completed && (
              <div className={styles.success}>모두 답변 완료! AI 인사이트를 보러 가요.</div>
            )}
          </section>
        ) : hasStarted ? (
          <section className={styles.cardMuted}>
            <h2 className={styles.question}>오늘 질문이 준비 중이에요.</h2>
            <p className={styles.helper}>가족이 모두 답하면 다음 질문이 도착해요.</p>
          </section>
        ) : null}

      </div>
    </div>
  );
};

export default Home;
