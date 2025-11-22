import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { questionAPI } from '../api';
import './Questions.css';

const Questions = () => {
  const navigate = useNavigate();
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadQuestions();
  }, []);

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

  const handleQuestionClick = (questionId) => {
    navigate(`/questions/${questionId}`);
  };

  const getStatusBadge = (completed) => {
    if (completed) {
      return <span className="badge badge-success">완료</span>;
    }
    return <span className="badge badge-info">진행중</span>;
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return `${date.getMonth() + 1}월 ${date.getDate()}일`;
  };

  if (loading) {
    return (
      <div className="container">
        <div className="loading-spinner"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container">
        <div className="error-message">{error}</div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="questions-page">
        <h1>질문 히스토리</h1>

        {questions.length === 0 ? (
          <div className="no-questions">
            <p>아직 진행된 질문이 없습니다.</p>
            <p className="hint-text">
              홈에서 오늘의 질문을 확인해보세요!
            </p>
          </div>
        ) : (
          <div className="questions-grid">
            {questions.map((question) => (
              <div
                key={question.familyQuestionId}
                className={`question-card-item ${question.completed ? 'completed' : ''}`}
                onClick={() => handleQuestionClick(question.familyQuestionId)}
              >
                <div className="question-header">
                  <span className="question-number">
                    Day {question.sequenceNumber}
                  </span>
                  {getStatusBadge(question.completed)}
                </div>

                <div className="question-date">
                  {formatDate(question.assignedDate)}
                </div>

                <div className="question-content-preview">
                  {question.questionText}
                </div>
              </div>
            ))}
          </div>
        )}

        <div className="questions-summary">
          <div className="summary-card">
            <h3>통계</h3>
            <div className="summary-stats">
              <div className="stat">
                <span className="stat-label">전체 질문:</span>
                <span className="stat-value">{questions.length}개</span>
              </div>
              <div className="stat">
                <span className="stat-label">완료된 질문:</span>
                <span className="stat-value">
                  {questions.filter(q => q.completed).length}개
                </span>
              </div>
              <div className="stat">
                <span className="stat-label">진행중 질문:</span>
                <span className="stat-value">
                  {questions.filter(q => !q.completed).length}개
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Questions;
