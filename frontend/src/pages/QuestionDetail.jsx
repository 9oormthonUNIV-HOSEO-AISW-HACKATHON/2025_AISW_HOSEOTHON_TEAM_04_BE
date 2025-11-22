import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { questionAPI } from '../api';
import './QuestionDetail.css';

// 백엔드에서 내려오는 인사이트 JSON(raw string)과 객체를 모두 지원
const normalizeInsight = (insight, insightJson) => {
  let parsedInsight = insight;

  // insightJson이 있으면 우선 파싱해서 사용
  if (!parsedInsight && insightJson) {
    try {
      parsedInsight = JSON.parse(insightJson);
      console.log('[Insight Debug] parsed raw insightJson successfully');
    } catch (e) {
      console.warn('[Insight Debug] failed to parse insightJson', e);
    }
  }

  if (!parsedInsight) return null;

  const toList = (value) => {
    if (!value) return [];
    if (Array.isArray(value)) {
      return value.filter(Boolean);
    }
    if (typeof value === 'string') {
      return value
        .split(/\r?\n/)
        .map((item) => item.trim())
        .filter(Boolean);
    }
    return [String(value)];
  };

  const commonThemes = toList(
    insight.commonThemes ??
    insight.common_themes ??
    insight.commonPoints ??
    insight.common_points
  );
  const generationDifferences = toList(
    insight.generationDifferences ??
    insight.generation_differences ??
    insight.differences
  );
  const conversationSuggestions = toList(
    insight.conversationSuggestions ??
    insight.conversation_suggestions ??
    insight.suggestedDialogue ??
    insight.suggested_dialogue
  );

  if (!commonThemes.length && !generationDifferences.length && !conversationSuggestions.length) {
    return null;
  }

  return { commonThemes, generationDifferences, conversationSuggestions };
};

const QuestionDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [question, setQuestion] = useState(null);
  const [answerText, setAnswerText] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [isEditing, setIsEditing] = useState(false);

  useEffect(() => {
    loadQuestionDetail();
  }, [id]);

  const loadQuestionDetail = async () => {
    setLoading(true);
    setError(null);

    try {
      const data = await questionAPI.getQuestionDetail(id);
      console.log('[Insight Debug] fetched question detail', {
        familyQuestionId: id,
        completed: data?.completed,
        answersCount: data?.answers?.length,
        insight: data?.insight,
        insightJson: data?.insightJson,
      });
      setQuestion(data);
      setIsEditing(false);
      setAnswerText('');
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
      setIsEditing(false);
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

  useEffect(() => {
    if (!question) return;
    const normalized = normalizeInsight(question.insight, question.insightJson);
    console.log('[Insight Debug] normalized insight', {
      familyQuestionId: question.familyQuestionId,
      completed: question.completed,
      rawInsight: question.insight,
      rawInsightJson: question.insightJson,
      normalizedInsight: normalized,
    });
  }, [question]);

  const handleStartEdit = () => {
    if (!question?.myAnswer) return;
    setAnswerText(question.myAnswer.content);
    setIsEditing(true);
    setError(null);
  };

  const handleCancelEdit = () => {
    setIsEditing(false);
    setAnswerText('');
    setError(null);
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
  const userAnswer = question.myAnswer;
  const showAnswerForm = !userAnswer || isEditing;
  const showWaitingMessage = !isCompleted && Boolean(userAnswer);
  const normalizedInsight = normalizeInsight(question.insight, question.insightJson);

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

          <div className="answer-submit-section">
            <h3>나의 답변</h3>

            {userAnswer && !isEditing && (
              <div className="my-answer-section">
                <div className="answer-card">
                  <p className="answer-content">{userAnswer.content}</p>
                  <div className="answer-footer">
                    <span className="answer-time">
                      {formatDate(userAnswer.createdAt)}
                    </span>
                    {!isCompleted && (
                      <button
                        type="button"
                        className="btn-secondary edit-button"
                        onClick={handleStartEdit}
                      >
                        답변 수정
                      </button>
                    )}
                  </div>
                </div>
              </div>
            )}

            {showAnswerForm && (
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
                <div className="answer-actions">
                  {userAnswer && (
                    <button
                      type="button"
                      className="btn-secondary cancel-button"
                      onClick={handleCancelEdit}
                      disabled={submitting}
                    >
                      취소
                    </button>
                  )}
                  <button
                    type="submit"
                    className="btn-primary submit-button"
                    disabled={submitting}
                  >
                    {submitting ? '저장 중...' : userAnswer ? '답변 수정' : '답변 제출'}
                  </button>
                </div>
              </form>
            )}
          </div>

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
          {normalizedInsight && (
            <div className="insight-section">
              <h3>💡 AI 인사이트</h3>
              <div className="insight-card">
                {normalizedInsight.commonThemes.length > 0 && (
                  <div className="insight-item">
                    <h4>공통 주제</h4>
                    <ul>
                      {normalizedInsight.commonThemes.map((theme, index) => (
                        <li key={index}>{theme}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {normalizedInsight.generationDifferences.length > 0 && (
                  <div className="insight-item">
                    <h4>세대별 차이</h4>
                    <ul>
                      {normalizedInsight.generationDifferences.map((diff, index) => (
                        <li key={index}>{diff}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {normalizedInsight.conversationSuggestions.length > 0 && (
                  <div className="insight-item">
                    <h4>대화 제안</h4>
                    <ul>
                      {normalizedInsight.conversationSuggestions.map((suggestion, index) => (
                        <li key={index}>{suggestion}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* 답변 대기 중 메시지 */}
          {showWaitingMessage && (
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
