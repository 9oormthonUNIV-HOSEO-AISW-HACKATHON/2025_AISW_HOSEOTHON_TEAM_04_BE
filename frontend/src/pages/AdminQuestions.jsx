import React, { useEffect, useState } from 'react';
import { questionAPI } from '../api';
import './AdminQuestions.css';

const AdminQuestions = () => {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [formData, setFormData] = useState({ text: '', orderIndex: '' });
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadQuestions();
  }, []);

  const loadQuestions = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await questionAPI.getAdminQuestions();
      setQuestions(data);
    } catch (err) {
      setError(err.response?.data?.message || '질문 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!formData.text.trim()) {
      setError('질문 내용을 입력해주세요.');
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      await questionAPI.createAdminQuestion({
        text: formData.text.trim(),
        orderIndex: formData.orderIndex ? Number(formData.orderIndex) : null,
      });
      setFormData({ text: '', orderIndex: '' });
      await loadQuestions();
    } catch (err) {
      setError(err.response?.data?.message || '질문을 추가하지 못했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (questionId) => {
    if (!window.confirm('해당 질문을 삭제할까요? (배정된 질문은 삭제할 수 없습니다)')) {
      return;
    }

    try {
      await questionAPI.deleteAdminQuestion(questionId);
      await loadQuestions();
    } catch (err) {
      setError(err.response?.data?.message || '질문을 삭제하지 못했습니다.');
    }
  };

  if (loading) {
    return (
      <div className="container">
        <div className="loading-spinner"></div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="admin-questions-page">
        <h1>질문 관리</h1>
        <p className="page-description">질문 순서를 지정하거나 삭제할 수 있습니다.</p>

        <form className="question-form" onSubmit={handleCreate}>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="text">질문 내용</label>
              <textarea
                id="text"
                name="text"
                value={formData.text}
                onChange={handleChange}
                placeholder="추가할 질문을 입력해주세요."
                rows="3"
                disabled={submitting}
              />
            </div>
            <div className="form-group order-group">
              <label htmlFor="orderIndex">삽입할 순번 (선택)</label>
              <input
                id="orderIndex"
                name="orderIndex"
                type="number"
                min="1"
                value={formData.orderIndex}
                onChange={handleChange}
                placeholder="비우면 마지막에 추가"
                disabled={submitting}
              />
            </div>
          </div>
          <button type="submit" className="btn-primary" disabled={submitting}>
            {submitting ? '추가 중...' : '질문 추가'}
          </button>
        </form>

        {error && <div className="error-message">{error}</div>}

        <div className="question-list">
          {questions.length === 0 ? (
            <p>등록된 질문이 없습니다.</p>
          ) : (
            questions.map((question) => (
              <div key={question.id} className="question-row">
                <div className="question-info">
                  <span className="question-order">#{question.orderIndex}</span>
                  <p className="question-text">{question.text}</p>
                </div>
                <button
                  className="delete-btn"
                  onClick={() => handleDelete(question.id)}
                >
                  삭제
                </button>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};

export default AdminQuestions;
