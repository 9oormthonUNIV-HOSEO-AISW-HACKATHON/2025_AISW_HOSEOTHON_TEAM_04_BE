import React from 'react';
import './LoadingSpinner.css';

const LoadingSpinner = ({ message = '로딩 중...' }) => {
  return (
    <div className="loading-container">
      <div className="loading-spinner"></div>
      <p className="loading-message">{message}</p>
    </div>
  );
};

export default LoadingSpinner;