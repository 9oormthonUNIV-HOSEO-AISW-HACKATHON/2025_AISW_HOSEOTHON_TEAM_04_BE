import React from 'react';
import { Outlet, NavLink } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import BottomNav from './BottomNav';
import styles from './Layout.module.css';

const Layout = () => {
  const { user } = useAuth();
  return (
    <div className={styles.layout}>
      <header className={styles.header}>
        <div className={`${styles.headerContent} container`}>
          <NavLink to="/" className={styles.logo}>
            FamilyQ
          </NavLink>
          <div className={styles.headerActions}>
            {user?.admin && (
              <NavLink
                to="/admin/questions"
                className={({ isActive }) =>
                  `${styles.adminLink} ${isActive ? styles.adminActive : ''}`
                }
              >
                관리자
              </NavLink>
            )}
            <NavLink to="/settings" className={styles.profileChip}>
              <span className={styles.profileText}>{user?.name}</span>
            </NavLink>
          </div>
        </div>
      </header>

      <main className={styles.main}>
        <Outlet />
      </main>

      <BottomNav />
    </div>
  );
};

export default Layout;
