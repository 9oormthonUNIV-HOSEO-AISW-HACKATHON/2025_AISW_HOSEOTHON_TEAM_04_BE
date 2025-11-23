import React from 'react';
import { NavLink } from 'react-router-dom';
import styles from './BottomNav.module.css';

const HeartIcon = () => (
  <svg viewBox="0 0 24 24" aria-hidden="true">
    <path
      d="M12 20s-6-3.7-8-7.5C2.3 10 3 6.5 6 6c2-.3 3.4 1 4 2.1C10.6 7 12 5.7 14 6c3 .5 3.7 4 2 6.5C18 16.3 12 20 12 20Z"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  </svg>
);

const NoteIcon = () => (
  <svg viewBox="0 0 24 24" aria-hidden="true">
    <path
      d="M6 4.5h10.5M6 9h10.5M6 13.5h6.5"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
    />
    <path
      d="M7.5 3h9c1 0 1.5.5 1.5 1.5v15L15 17H8.5C7.5 17 7 16.5 7 15.5v-12c0-.4.1-.7.5-.9Z"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinejoin="round"
    />
  </svg>
);

const CogIcon = () => (
  <svg viewBox="0 0 24 24" aria-hidden="true">
    <circle cx="12" cy="12" r="3.5" fill="none" stroke="currentColor" strokeWidth="1.8" />
    <path
      d="M12 3.5v2M12 18.5v2M4.9 6l1.6 1.2M17.5 16.8l1.6 1.2M3.5 12h2M18.5 12h2M6.5 17.5l1.2-1.6M16.8 6.5l1.2-1.6"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
    />
  </svg>
);

const navItems = [
  { to: '/', label: '홈', icon: <HeartIcon /> },
  { to: '/questions', label: '질문', icon: <NoteIcon /> },
  { to: '/settings', label: '설정', icon: <CogIcon /> },
];

const BottomNav = () => {
  return (
    <nav className={styles.nav}>
      <div className={styles.inner}>
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/'}
            className={({ isActive }) =>
              `${styles.link} ${isActive ? styles.active : ''}`
            }
          >
            <span className={styles.icon}>{item.icon}</span>
            <span className={styles.label}>{item.label}</span>
          </NavLink>
        ))}
      </div>
    </nav>
  );
};

export default BottomNav;
