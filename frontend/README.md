# FamilyQ Frontend

FamilyQ 프론트엔드 - 가족과 함께 나누는 일상 질문 서비스

## 프로젝트 개요

FamilyQ는 가족 구성원들이 매일 같은 질문에 답변하고, 모든 가족이 답변을 완료하면 서로의 답변과 AI 인사이트를 확인할 수 있는 웹 애플리케이션입니다.

## 기술 스택

- **React** (JavaScript)
- **Vite** - 빌드 도구
- **React Router** - 라우팅
- **Axios** - HTTP 클라이언트
- **CSS** - 스타일링

## 주요 기능

### 1. 사용자 인증
- 회원가입 (아이디, 비밀번호, 이름, 출생연도, 가족 역할)
- 로그인/로그아웃 (세션 기반 인증)
- 사용자 정보 조회

### 2. 가족 관리
- 가족 생성 (고유 가족 코드 자동 생성)
- 가족 참여 (초대 코드로 참여)
- 가족 구성원 목록 조회
- 가족 통계 확인

### 3. 질문 & 답변
- 오늘의 질문 조회
- 답변 제출
- 질문 히스토리 확인
- 질문별 상세 정보 조회

### 4. AI 인사이트
- 모든 가족 구성원이 답변 완료 시 AI 인사이트 생성
- 공통 주제, 세대별 차이, 대화 제안 제공

## 프로젝트 구조

```
familyq-frontend/
├── src/
│   ├── api/              # API 통신 레이어
│   │   ├── axios.js      # Axios 설정
│   │   ├── auth.js       # 인증 API
│   │   ├── user.js       # 사용자 API
│   │   ├── family.js     # 가족 API
│   │   └── question.js   # 질문 API
│   ├── components/       # 재사용 가능한 컴포넌트
│   │   ├── ErrorBoundary.jsx
│   │   ├── Layout.jsx
│   │   ├── LoadingSpinner.jsx
│   │   └── ProtectedRoute.jsx
│   ├── contexts/         # Context API
│   │   └── AuthContext.jsx
│   ├── pages/           # 페이지 컴포넌트
│   │   ├── Home.jsx
│   │   ├── Login.jsx
│   │   ├── Signup.jsx
│   │   ├── Family.jsx
│   │   ├── Questions.jsx
│   │   └── QuestionDetail.jsx
│   ├── styles/          # 스타일 파일
│   ├── App.jsx
│   └── main.jsx
├── index.html
├── vite.config.js       # Vite 설정
└── package.json
```

## 설치 및 실행

### 사전 요구사항

- Node.js 18.x 이상
- npm 또는 yarn
- 백엔드 서버가 포트 4001에서 실행 중이어야 함

### 설치

```bash
# 의존성 설치
npm install
```

### 개발 서버 실행

```bash
# 개발 서버 시작 (포트 3000)
npm run dev
```

### 프로덕션 빌드

```bash
# 프로덕션 빌드
npm run build

# 빌드 결과물 미리보기
npm run preview
```

## 환경 설정

### 백엔드 연동

`vite.config.js`에서 백엔드 프록시 설정:

```javascript
proxy: {
  '/api': {
    target: 'http://localhost:4001',
    changeOrigin: true,
  },
}
```

### API 설정

`src/api/axios.js`에서 API 클라이언트 설정:

```javascript
const apiClient = axios.create({
  baseURL: '/api',
  withCredentials: true, // 세션 쿠키 포함
});
```

## 사용 방법

1. **회원가입**
   - `/signup` 페이지에서 계정 생성
   - 가족 내 역할 선택 (아버지/어머니/자녀)

2. **가족 생성/참여**
   - 가족 생성: 새로운 가족 그룹 생성 및 고유 코드 발급
   - 가족 참여: 초대 코드로 기존 가족에 참여

3. **질문 답변**
   - 홈 화면에서 오늘의 질문 확인
   - 답변 제출
   - 모든 가족이 답변 완료 시 결과 확인

4. **인사이트 확인**
   - 전원 답변 완료 시 AI 인사이트 자동 생성
   - 가족 간 공통점과 차이점 분석

## 주요 페이지

- `/` - 홈 (오늘의 질문)
- `/login` - 로그인
- `/signup` - 회원가입
- `/family` - 가족 관리
- `/questions` - 질문 히스토리
- `/questions/:id` - 질문 상세 및 답변

## API 엔드포인트

백엔드 API 엔드포인트 (기본 경로: `/api`):

- **인증**
  - `POST /auth/signup` - 회원가입
  - `POST /auth/login` - 로그인
  - `POST /auth/logout` - 로그아웃

- **사용자**
  - `GET /users/me` - 내 정보 조회

- **가족**
  - `POST /families` - 가족 생성
  - `POST /families/join` - 가족 참여
  - `GET /families/me` - 내 가족 정보

- **질문**
  - `GET /questions/today` - 오늘의 질문
  - `GET /questions/history` - 질문 히스토리
  - `GET /questions/:id` - 질문 상세
  - `POST /questions/:id/answers` - 답변 제출

## 보안 설정

- 세션 기반 인증 사용
- CORS 설정을 통한 백엔드 통신
- withCredentials 옵션으로 쿠키 포함
- Protected Route로 인증 필요 페이지 보호

## 개발 시 주의사항

1. 백엔드 서버가 먼저 실행되어야 함 (포트 4001)
2. 세션 쿠키 처리를 위해 withCredentials 설정 필수
3. API 응답 에러는 401 (인증 실패) 시 자동으로 로그인 페이지로 리다이렉트
4. 모든 날짜/시간은 한국 시간(Asia/Seoul) 기준

## 문제 해결

### CORS 에러
- 백엔드 CORS 설정 확인
- 프록시 설정 확인

### 세션 유지 안됨
- withCredentials: true 설정 확인
- 쿠키 정책 확인

### API 호출 실패
- 백엔드 서버 실행 여부 확인
- 네트워크 탭에서 요청/응답 확인
- 콘솔 에러 메시지 확인

## 라이선스

This project is private and proprietary.