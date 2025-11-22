import React from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import './Layout.css';

const Layout = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    const result = await logout();
    if (result.success) {
      navigate('/login');
    }
  };

  return (
    <div className="layout">
      <header className="header">
        <div className="container header-content">
          <div className="logo">
            <NavLink to="/">FamilyQ</NavLink>
          </div>

          <nav className="nav">
            <NavLink to="/" className="nav-link">
              홈
            </NavLink>
            <NavLink to="/family" className="nav-link">
              가족
            </NavLink>
            <NavLink to="/questions" className="nav-link">
              질문
            </NavLink>
            {user?.admin && (
              <NavLink to="/admin/questions" className="nav-link">
                관리자
              </NavLink>
            )}
          </nav>

          <div className="user-menu">
            <span className="user-info">
              {user?.name}님 ({user?.roleType === 'FATHER' ? '아버지' :
                           user?.roleType === 'MOTHER' ? '어머니' : '자녀'})
            </span>
            <button onClick={handleLogout} className="logout-btn">
              로그아웃
            </button>
          </div>
        </div>
      </header>

      <main className="main">
        <Outlet />
      </main>

      <footer className="footer">
        <div className="container">
          <p>&copy; 2025 FamilyQ. 가족과 함께 나누는 일상</p>
        </div>
      </footer>
    </div>
  );
};

export default Layout;
