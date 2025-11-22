# FamilyQ 프로젝트 상세 명세서

## 1. 서비스 개요

### 목적
가족 단위로 매일 같은 질문에 답변을 남기고, 가족 구성원 전원이 답변을 완료하면 서로의 답변과 AI가 생성한 인사이트를 확인할 수 있는 서비스입니다.

### 특징
- 고정 20개 질문 (서버에 미리 저장, 순서 고정)
- 가족 단위 진행 (가족마다 질문 진행 속도/날짜는 다를 수 있음)
- 세션 기반 인증

## 2. 기술 스택 / 아키텍처

### 백엔드
- Spring Boot, Spring MVC
- Spring Security (세션 기반)
- JPA
- MariaDB (호스트: 192.168.0.51, 실제 접속 정보는 설정 파일?env로 분리)

### 프론트엔드
- React + JavaScript
- Tailwind CSS

### 인증
- 서버 세션 + 세션 쿠키 (HTTP-only)
- 프론트는 withCredentials: true로 API 호출
- CORS 설정: Access-Control-Allow-Credentials: true, Origin은 프론트 도메인으로 명시

### 배포
- Docker (로컬 → 시놀로지 NAS)
- 타임존: Asia/Seoul 기준

## 3. 도메인 모델

### 3.1 User
- id (PK)
- loginId (String, unique)
- passwordHash (String)
- name (String)
- birthYear (Int)
- roleType (Enum: FATHER, MOTHER, CHILD)
- familyId (FK → Family.id, nullable)
- createdAt, updatedAt

### 3.2 Family
- id (PK)
- familyCode (String, unique, 영문+숫자 랜덤 코드, 길이 6~8)
- createdAt

### 3.3 Question (고정 20개)
- id (PK)
- text (String)
- orderIndex (Int, 1~20, 진행 순서)
- 서버 시작 시 시드 데이터로 20개 insert (순서 고정)

### 3.4 FamilyQuestion (가족별 질문 진행 상태)
- id (PK)
- familyId (FK)
- questionId (FK)
- sequenceNumber (Int, 1~20, 해당 가족에서 몇 번째 질문인지)
- assignedDate (Date)
- status (Enum: IN_PROGRESS, COMPLETED)
- completedAt (DateTime, nullable)
- requiredMemberCount (Int) → 이 FamilyQuestion을 생성할 당시 가족 구성원 수 스냅샷
- insightJson (Text, nullable) – AI 인사이트 원본 JSON

### 3.5 Answer
- id (PK)
- familyQuestionId (FK)
- userId (FK)
- content (Text)
- createdAt, updatedAt
- Unique 제약: (familyQuestionId, userId) 1개만 허용

## 4. 핵심 비즈니스 규칙

### 4.1 회원가입
- 입력 필드: loginId, password, name, birthYear, roleType (부/모/자녀)
- loginId는 중복 불가
- 비밀번호는 해시 저장
- 회원가입 성공 후 동작: 바로 로그인 처리 또는 로그인 페이지로 이동

### 4.2 로그인 / 세션
- POST /api/auth/login
- 인증 성공 시 서버 세션 생성, 세션 쿠키 반환
- 프론트는 모든 API 호출 시 withCredentials: true로 세션 쿠키를 포함
- 비로그인 상태에서 보호된 API 접근 시 401 반환

### 4.3 가족 코드 생성 / 참여
- 회원가입 후, familyId가 없는 상태에서는 질문 관련 API 호출 불가
- "가족 코드가 있나요?" 화면에서:
  - 코드 있음: familyCode 입력 → 유효한 코드면 해당 Family에 현재 User.familyId 연결
  - 코드 생성: 새 Family 생성, 랜덤 familyCode 부여, 현재 유저를 첫 멤버로 연결
- familyId == null 상태에서 질문/답변 관련 API 호출 시: 400 또는 409 + 에러 코드 반환

### 4.4 질문 진행 규칙

#### 4.4.1 기본 규칙
- 한 Family 당 하루에 하나의 질문만 진행
- Family가 첫 질문을 받는 시점: 해당 Family 멤버가 처음으로 GET /api/questions/today를 호출했을 때
- FamilyQuestion이 하나도 없으면:
  - sequenceNumber = 1인 Question으로 FamilyQuestion 생성
  - status = IN_PROGRESS
  - assignedDate = today
  - requiredMemberCount = 현재 Family 멤버 수

#### 4.4.2 자정 배치 (매일 00:00, Asia/Seoul)
- 전날까지의 해당 Family의 마지막 FamilyQuestion 조회
- 그 질문이:
  - status = COMPLETED이고 sequenceNumber < 20이면
    → 다음 질문(sequenceNumber+1)으로 새 FamilyQuestion 생성
    → status = IN_PROGRESS, assignedDate = 오늘 날짜, requiredMemberCount = 현재 Family 멤버 수
  - 마지막 질문이 IN_PROGRESS이거나, 아직 전원 답변이 아닌 경우: 새 질문 생성 없이 기존 질문 유지
  - 이미 sequenceNumber = 20까지 모두 COMPLETED이면: 이후 질문은 생성하지 않음

#### 4.4.3 requiredMemberCount 스냅샷 정책
- FamilyQuestion 생성 시점의 Family 멤버 수를 requiredMemberCount로 저장
- 전원 답변 완료 여부 판단은 항상: 해당 FamilyQuestion에 대해 저장된 Answer 개수 == requiredMemberCount
- 중간에 새 가족 구성원이 합류하더라도 이전에 생성된 FamilyQuestion의 필수 답변 대상에는 포함되지 않음

### 4.5 답변 공개 조건
- Answer 수가 requiredMemberCount와 같아지는 순간:
  - FamilyQuestion.status = COMPLETED
  - completedAt 기록
  - AI 인사이트 생성 트리거 (최초 1회만)
- 그 전까지는:
  - 각 User는 자신의 답변만 조회 가능
  - 다른 가족의 답변은 비공개
  - 인사이트는 null

### 4.6 AI 인사이트 생성

#### 4.6.1 입력 데이터
- 질문 내용: Question.text
- 해당 FamilyQuestion의 Family 멤버 정보 (현재 Family 전체 기준):
  - name, roleType, birthYear
  - 자녀(CHILD)에 대해서는 birthYear 오름차순 정렬 후, firstChild, secondChild, … 순서 태그 계산
  - 연령대 태그: 현재연도 - birthYear를 이용해 10대, 20대, 30대 등 단순 그룹핑
- 각 Answer 구조:
```json
{
  "userName": "...",
  "roleType": "FATHER | MOTHER | CHILD",
  "ageGroupTag": "10대 | 20대 | ...",
  "birthOrderTag": "firstChild | secondChild | null",
  "content": "..."
}
```

#### 4.6.2 출력 JSON (필드명 고정)
```json
{
  "common_themes": ["..."],
  "generation_differences": ["..."],
  "conversation_suggestions": ["..."]
}
```
- common_themes: 가족 공통 주제/가치/감정
- generation_differences: 세대별 시각 차이 요약
- conversation_suggestions: 내일 가볍게 이야기해 볼 대화 주제 제안

#### 4.6.3 생성 타이밍 & 중복 방지
- FamilyQuestion가 처음 COMPLETED 상태로 변경될 때 1회 호출
- 트랜잭션 내부에서:
  - FamilyQuestion를 FOR UPDATE 또는 동등한 방식으로 잠그고
  - 아직 status = IN_PROGRESS인 경우에만:
    - 전원 답변 여부 확인 → COMPLETED로 변경 → 이때만 AI 호출
  - 이미 COMPLETED이면 AI 호출 스킵
- AI 응답은 그대로 insightJson에 저장
- 이후 동일 질문 조회 시 DB에 저장된 insightJson을 파싱해서 프론트로 전달

## 5. API 개요

### 5.1 인증
- **POST /api/auth/signup**
  - req: `{ "loginId": "", "password": "", "name": "", "birthYear": 1990, "roleType": "FATHER|MOTHER|CHILD" }`

- **POST /api/auth/login**
  - req: `{ "loginId": "", "password": "" }`
  - res: 로그인 성공 여부, 유저 정보
  - 세션 쿠키 설정 (HttpOnly)

- **POST /api/auth/logout**
  - 현재 세션 무효화

- **GET /api/users/me**
  - 현재 세션 유저 정보 반환
  - 비로그인 시 401

### 5.2 가족
- **POST /api/families**
  - 새 Family 생성 + 현재 유저 연결
  - res: `{ "familyId": 1, "familyCode": "A7KD9Q" }`

- **POST /api/families/join**
  - req: `{ "familyCode": "A7KD9Q" }`
  - res: `{ "familyId": 1 }`

- **GET /api/families/me**
  - res: 가족 정보 및 구성원 목록

### 5.3 질문 & 답변
- **GET /api/questions/today**
  - 현재 세션 유저의 familyId 기준
  - 해당 Family에 진행 중/오늘 질문이 없으면 새로운 FamilyQuestion 생성

- **POST /api/questions/{familyQuestionId}/answers**
  - req: `{ "content": "..." }`
  - 현재 유저의 Answer가 없으면 생성, 있으면 수정

- **GET /api/questions/history**
  - 지금까지의 질문 목록

- **GET /api/questions/{familyQuestionId}**
  - 질문 상세 조회 (전원 답변 완료 시 다른 답변 및 인사이트 포함)

## 6. 프론트엔드 주요 페이지
- `/login` – 로그인 페이지
- `/signup` – 회원가입 페이지
- `/family/connect` – 가족 코드 입력 / 코드 생성 페이지
- `/questions/today` – 오늘의 질문, 내 답변 작성/수정, 진행 상황 표시
- `/questions/history` – 질문 리스트
- `/questions/:familyQuestionId` – 질문별 답변 + 인사이트 상세

## 7. 환경 설정

### 로컬 테스트 환경
- 서버 포트: 4001 (백엔드). 4002 (프론트)
- 데이터베이스: MariaDB (192.168.0.51:3306)
- 타임존: Asia/Seoul

### 데이터베이스 연결 정보
```
DB_HOST=192.168.0.51
DB_PORT=3306
DB_NAME=familyq
DB_USER=[설정 필요]
DB_PASSWORD=[설정 필요]
```