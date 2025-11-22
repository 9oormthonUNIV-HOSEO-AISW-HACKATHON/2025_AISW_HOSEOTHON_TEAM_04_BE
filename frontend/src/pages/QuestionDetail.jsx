import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { questionAPI } from '../api';
import './QuestionDetail.css';

const QuestionDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [question, setQuestion] = useState(null);
  const [answerText, setAnswerText] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadQuestionDetail();
  }, [id]);

  const loadQuestionDetail = async () => {
    setLoading(true);
    setError(null);

    try {
      const data = await questionAPI.getQuestionDetail(id);
      setQuestion(data);
    } catch (err) {
      setError('질문을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitAnswer = async (e) => {
    e.preventDefault();

    if (!answerText.trim()) {
      setError('답변을 입력해주세요.');
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      await questionAPI.submitAnswer(id, { content: answerText });
      await loadQuestionDetail(); // 답변 후 데이터 새로고침
      setAnswerText('');
    } catch (err) {
      setError('답변 제출에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`;
  };

  if (loading) {
    return (
      <div className="container">
        <div className="loading-spinner"></div>
      </div>
    );
  }

  if (!question) {
    return (
      <div className="container">
        <div className="error-message">질문을 찾을 수 없습니다.</div>
      </div>
    );
  }

  const isCompleted = Boolean(question.completed);
  const questionTitle = question.questionText;

  return (
    <div className="container">
      <div className="question-detail-page">
        <button onClick={() => navigate('/questions')} className="back-button">
          ← 목록으로 돌아가기
        </button>

        <div className="question-detail-header">
          <div className="question-meta">
            <span className="question-day">Day {question.sequenceNumber}</span>
            <span className="question-date">{formatDate(question.assignedDate)}</span>
          </div>
          <div className={`question-status ${isCompleted ? 'status-completed' : 'status-progress'}`}>
            {isCompleted ? '✅ 모든 답변 완료' : '⏳ 진행중'}
          </div>
        </div>

        <div className="question-detail-content">
          <h1 className="question-title">{questionTitle}</h1>

          {/* 답변 제출 섹션 */}
          {!question.myAnswer && (
            <div className="answer-submit-section">
              <h3>나의 답변</h3>
              <form onSubmit={handleSubmitAnswer}>
                <textarea
                  value={answerText}
                  onChange={(e) => setAnswerText(e.target.value)}
                  placeholder="답변을 입력해주세요..."
                  className="answer-textarea"
                  rows="6"
                  disabled={submitting}
                />
                {error && <div className="error-message">{error}</div>}
                <button
                  type="submit"
                  className="btn-primary submit-button"
                  disabled={submitting}
                >
                  {submitting ? '제출 중...' : '답변 제출'}
                </button>
              </form>
            </div>
          )}

          {/* 내 답변 표시 */}
          {question.myAnswer && (
            <div className="my-answer-section">
              <h3>나의 답변</h3>
              <div className="answer-card">
                <p className="answer-content">{question.myAnswer.content}</p>
                <span className="answer-time">
                  {formatDate(question.myAnswer.createdAt)}
                </span>
              </div>
            </div>
          )}

          {/* 다른 가족들의 답변 (완료 상태일 때만) */}
          {isCompleted && question.answers && question.answers.length > 0 && (
            <div className="all-answers-section">
              <h3>가족들의 답변</h3>
              <div className="answers-grid">
                {question.answers.map((answer) => (
                  <div key={answer.answerId} className="family-answer-card">
                    <div className="answer-header">
                      <span className="answer-author">
                        {answer.userName}
                        <span className="role-badge">
                          {answer.roleType === 'FATHER' ? '아버지' :
                           answer.roleType === 'MOTHER' ? '어머니' : '자녀'}
                        </span>
                      </span>
                    </div>
                    <p className="answer-content">{answer.content}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* AI 인사이트 (있을 경우) */}
          {question.insight && (
            <div className="insight-section">
              <h3>💡 AI 인사이트</h3>
              <div className="insight-card">
                {question.insight.commonThemes && (
                  <div className="insight-item">
                    <h4>공통 주제</h4>
                    <ul>
                      {question.insight.commonThemes.map((theme, index) => (
                        <li key={index}>{theme}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {question.insight.generationDifferences && (
                  <div className="insight-item">
                    <h4>세대별 차이</h4>
                    <ul>
                      {question.insight.generationDifferences.map((diff, index) => (
                        <li key={index}>{diff}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {question.insight.conversationSuggestions && (
                  <div className="insight-item">
                    <h4>대화 제안</h4>
                    <ul>
                      {question.insight.conversation_suggestions.map((suggestion, index) => (
                        <li key={index}>{suggestion}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* 답변 대기 중 메시지 */}
          {question.status === 'IN_PROGRESS' && question.myAnswer && (
            <div className="waiting-message">
              <p>다른 가족 구성원들의 답변을 기다리는 중입니다.</p>
              <p className="hint">모든 가족이 답변을 완료하면 서로의 답변과 AI 인사이트를 확인할 수 있습니다.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default QuestionDetail;
