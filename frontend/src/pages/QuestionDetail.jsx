import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { questionAPI } from '../api';
import styles from './QuestionDetail.module.css';

const normalizeInsight = (insight, insightJson) => {
  let parsedInsight = insight;

  if (!parsedInsight && insightJson) {
    try {
      parsedInsight = JSON.parse(insightJson);
    } catch (e) {
      return null;
    }
  }

  if (!parsedInsight) return null;

  const toList = (value) => {
    if (!value) return [];
    if (Array.isArray(value)) return value.filter(Boolean);
    if (typeof value === 'string') {
      return value
        .split(/\r?\n/)
        .map((item) => item.trim())
        .filter(Boolean);
    }
    return [String(value)];
  };

  const commonThemes = toList(
    parsedInsight.commonThemes ??
    parsedInsight.common_themes ??
    parsedInsight.commonPoints ??
    parsedInsight.common_points
  );
  const generationDifferences = toList(
    parsedInsight.generationDifferences ??
    parsedInsight.generation_differences ??
    parsedInsight.differences
  );
  const conversationSuggestions = toList(
    parsedInsight.conversationSuggestions ??
    parsedInsight.conversation_suggestions ??
    parsedInsight.suggestedDialogue ??
    parsedInsight.suggested_dialogue
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
    const loadQuestionDetail = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await questionAPI.getQuestionDetail(id);
        setQuestion(data);
        setIsEditing(false);
        setAnswerText('');
      } catch (err) {
        setError('질문을 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };
    loadQuestionDetail();
  }, [id]);

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
      const refreshed = await questionAPI.getQuestionDetail(id);
      setQuestion(refreshed);
      setIsEditing(false);
      setAnswerText('');
    } catch (err) {
      setError('답변 제출에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}. ${date.getMonth() + 1}. ${date.getDate()}.`;
  };

  if (loading) {
    return (
      <div className="container">
        <div className="loading-spinner" />
      </div>
    );
  }

  if (!question) {
    return (
      <div className="container">
        <div className={styles.error}>질문을 찾을 수 없습니다.</div>
      </div>
    );
  }

  const isCompleted = Boolean(question.completed);
  const userAnswer = question.myAnswer;
  const showAnswerForm = !userAnswer || isEditing;
  const normalizedInsight = normalizeInsight(question.insight, question.insightJson);

  return (
    <div className="container">
      <div className={styles.page}>
        <header className={styles.header}>
          <button className={styles.back} onClick={() => navigate('/questions')}>
            ← 목록으로
          </button>
          <div className={styles.meta}>
            <span className={styles.day}>Day {question.sequenceNumber}</span>
            <span className={styles.date}>{formatDate(question.assignedDate)}</span>
            <span className={`${styles.status} ${isCompleted ? styles.statusDone : styles.statusProg}`}>
              {isCompleted ? '모두 완료' : '진행중'}
            </span>
          </div>
        </header>

        <section className={styles.questionCard}>
          <p className={styles.label}>질문</p>
          <h1 className={styles.question}>{question.questionText}</h1>
        </section>

        <section className={styles.answerCard}>
          <div className={styles.answerHeader}>
            <h3 className={styles.sectionTitle}>나의 답변</h3>
            {userAnswer && !isEditing && !isCompleted && (
              <button
                type="button"
                className={styles.secondary}
                onClick={() => {
                  setIsEditing(true);
                  setAnswerText(userAnswer.content);
                }}
              >
                수정하기
              </button>
            )}
          </div>

          {userAnswer && !isEditing && (
            <div className={styles.answerBubble}>
              <p className={styles.answerText}>{userAnswer.content}</p>
              <span className={styles.answerTime}>{formatDate(userAnswer.createdAt)}</span>
            </div>
          )}

          {showAnswerForm && (
            <form onSubmit={handleSubmitAnswer} className={styles.form}>
              <textarea
                className={styles.textarea}
                value={answerText}
                onChange={(e) => setAnswerText(e.target.value)}
                placeholder="가족에게 마음을 들려주세요."
                rows={6}
                disabled={submitting}
              />
              {error && <div className="error-message">{error}</div>}
              <div className={styles.formActions}>
                {userAnswer && (
                  <button
                    type="button"
                    className={styles.ghost}
                    onClick={() => {
                      setIsEditing(false);
                      setAnswerText('');
                      setError(null);
                    }}
                    disabled={submitting}
                  >
                    취소
                  </button>
                )}
                <button type="submit" className={styles.primary} disabled={submitting}>
                  {submitting ? '저장 중...' : userAnswer ? '답변 수정' : '답변 제출'}
                </button>
              </div>
            </form>
          )}
        </section>

        {isCompleted && question.answers && question.answers.length > 0 && (
          <section className={styles.familySection}>
            <div className={styles.sectionHeader}>
              <h3 className={styles.sectionTitle}>가족들의 답변</h3>
              <span className={styles.muted}>모두의 마음을 읽어보세요</span>
            </div>
            <div className={styles.answersGrid}>
              {question.answers.map((answer) => (
                <div key={answer.answerId} className={styles.familyCard}>
                  <div className={styles.familyHead}>
                    <span className={styles.avatar}>
                      {answer.userName?.[0] || 'F'}
                    </span>
                    <div>
                      <p className={styles.name}>{answer.userName}</p>
                      <p className={styles.role}>
                        {answer.roleType === 'FATHER'
                          ? '아버지'
                          : answer.roleType === 'MOTHER'
                            ? '어머니'
                            : '자녀'}
                      </p>
                    </div>
                  </div>
                  <p className={styles.answerText}>{answer.content}</p>
                </div>
              ))}
            </div>
          </section>
        )}

        {normalizedInsight && (
          <section className={styles.insightSection}>
            <h3 className={styles.sectionTitle}>💡 AI 인사이트</h3>
            <div className={styles.insightCard}>
              {normalizedInsight.commonThemes.length > 0 && (
                <div className={styles.insightItem}>
                  <h4>공통 주제</h4>
                  <ul>
                    {normalizedInsight.commonThemes.map((theme, index) => (
                      <li key={index}>{theme}</li>
                    ))}
                  </ul>
                </div>
              )}
              {normalizedInsight.generationDifferences.length > 0 && (
                <div className={styles.insightItem}>
                  <h4>세대별 차이</h4>
                  <ul>
                    {normalizedInsight.generationDifferences.map((diff, index) => (
                      <li key={index}>{diff}</li>
                    ))}
                  </ul>
                </div>
              )}
              {normalizedInsight.conversationSuggestions.length > 0 && (
                <div className={styles.insightItem}>
                  <h4>대화 제안</h4>
                  <ul>
                    {normalizedInsight.conversationSuggestions.map((suggestion, index) => (
                      <li key={index}>{suggestion}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          </section>
        )}

        {!isCompleted && userAnswer && (
          <div className={styles.waiting}>
            다른 가족들의 답변을 기다리는 중입니다. 모두 완료되면 서로의 답변과 인사이트가 열려요.
          </div>
        )}
      </div>
    </div>
  );
};

export default QuestionDetail;
