import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { questionAPI } from '../api';
import styles from './Questions.module.css';

const filters = [
  { key: 'all', label: '전체' },
  { key: 'progress', label: '진행중' },
  { key: 'done', label: '완료' },
];

const Questions = () => {
  const navigate = useNavigate();
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('all');

  useEffect(() => {
    const loadQuestions = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await questionAPI.getHistory();
        setQuestions(data);
      } catch (err) {
        setError('질문 기록을 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };
    loadQuestions();
  }, []);

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}. ${date.getMonth() + 1}. ${date.getDate()}.`;
  };

  const filtered = questions.filter((q) => {
    if (filter === 'progress') return !q.completed;
    if (filter === 'done') return q.completed;
    return true;
  });

  if (loading) {
    return (
      <div className="container">
        <div className="loading-spinner" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="container">
        <div className={styles.error}>{error}</div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className={styles.page}>
        <header className={styles.header}>
          <div>
            <h1 className={styles.title}>질문 히스토리</h1>
          </div>
          <div className={styles.filters}>
            {filters.map((item) => (
              <button
                key={item.key}
                className={`${styles.chip} ${filter === item.key ? styles.active : ''}`}
                onClick={() => setFilter(item.key)}
              >
                {item.label}
              </button>
            ))}
          </div>
        </header>

        {filtered.length === 0 ? (
          <div className={styles.empty}>
            <p className={styles.emptyTitle}>아직 진행된 질문이 없어요.</p>
            <p className={styles.helper}>홈에서 오늘의 질문을 먼저 만나보세요.</p>
          </div>
        ) : (
          <div className={styles.list}>
            {filtered.map((question) => (
              <article
                key={question.familyQuestionId}
                className={`${styles.card} ${question.completed ? styles.cardDone : styles.cardProgress}`}
                onClick={() => navigate(`/questions/${question.familyQuestionId}`)}
              >
                <div className={styles.cardHead}>
                  <span className={styles.hash}>Day {question.sequenceNumber}</span>
                  <p className={styles.previewInline}>{question.questionText}</p>
                </div>
              </article>
            ))}
          </div>
        )}

        <p className={styles.note}>Tip! 모든 가족이 답을 마치면, 다음 질문은 내일 자정에 몰래 찾아와요.</p>
      </div>
    </div>
  );
};

export default Questions;
