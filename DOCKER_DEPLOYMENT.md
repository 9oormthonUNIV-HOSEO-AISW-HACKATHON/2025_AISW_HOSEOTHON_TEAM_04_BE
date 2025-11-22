# FamilyQ Docker 배포 가이드

## 📋 구성 요소

- **Backend**: Spring Boot 애플리케이션 (포트 3000)
- **Frontend**: Vite + React 애플리케이션 (포트 3001, nginx 미사용)
- **Database**: MariaDB 11.2 (포트 3306)

## 🚀 빠른 시작

### 1. 환경 설정

```bash
# .env 파일 생성
cp .env.example .env

# .env 파일 편집 (필요한 값 수정)
vim .env
```

### 2. Docker Compose로 전체 서비스 실행

```bash
# 전체 서비스 빌드 및 실행
docker-compose up -d --build

# 로그 확인
docker-compose logs -f

# 서비스 상태 확인
docker-compose ps
```

### 3. 배포 스크립트 사용

```bash
# 실행 권한 부여
chmod +x scripts/deploy-backend.sh

# 배포 실행
./scripts/deploy-backend.sh

# 클린 시작 (기존 데이터 삭제)
CLEAN_START=true ./scripts/deploy-backend.sh
```

## 📁 프로젝트 구조

```
.
├── backend/
│   ├── Dockerfile          # 백엔드 Docker 이미지 정의
│   ├── .dockerignore       # Docker 빌드 제외 파일
│   └── src/                # Spring Boot 소스 코드
├── frontend/
│   ├── Dockerfile          # 프론트엔드 Docker 이미지 정의 (nginx 없음)
│   ├── .dockerignore       # Docker 빌드 제외 파일
│   └── src/                # React 소스 코드
├── docker-compose.yml      # Docker Compose 설정
├── .env.example            # 환경 변수 예제
└── scripts/
    └── deploy.sh           # 배포 스크립트
```

## 🔧 환경 변수 설정

`.env` 파일에서 다음 변수들을 설정하세요:

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `BACKEND_PORT` | 백엔드 포트 | 3000 |
| `FRONTEND_PORT` | 프론트엔드 포트 | 3001 |
| `DB_HOST` | 데이터베이스 호스트 | mariadb |
| `DB_PORT` | 데이터베이스 포트 | 3306 |
| `DB_NAME` | 데이터베이스 이름 | family_db |
| `DB_USER` | 데이터베이스 사용자 | family_user |
| `DB_PASS` | 데이터베이스 비밀번호 | family_pass |
| `VITE_API_URL` | 프론트엔드 API URL | http://localhost:3000/api |

## 🐳 개별 서비스 관리

### 백엔드만 재시작
```bash
docker-compose restart backend
```

### 프론트엔드만 재빌드
```bash
docker-compose build frontend
docker-compose up -d frontend
```

### 데이터베이스 접속
```bash
docker-compose exec mariadb mysql -u family_user -p
```

## 📊 모니터링

### 로그 확인
```bash
# 전체 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mariadb
```

### 컨테이너 상태 확인
```bash
docker-compose ps
docker stats
```

## 🔍 헬스체크

각 서비스는 자동 헬스체크가 설정되어 있습니다:

- **Backend**: `http://localhost:3000/actuator/health`
- **Frontend**: `http://localhost:3001`
- **MariaDB**: `mariadb-admin ping`

## ⚠️ 문제 해결

### 포트 충돌
```bash
# 사용 중인 포트 확인
lsof -i :3000
lsof -i :3001
lsof -i :3306

# .env에서 포트 변경
BACKEND_PORT=3100
FRONTEND_PORT=3101
DB_PORT=3307
```

### 컨테이너 초기화
```bash
# 모든 컨테이너 중지 및 삭제
docker-compose down

# 볼륨까지 삭제 (데이터 초기화)
docker-compose down -v

# 이미지까지 삭제
docker-compose down --rmi all
```

### 빌드 캐시 삭제
```bash
docker system prune -a
```

## 📝 주의사항

1. **nginx 미사용**: 프론트엔드는 nginx 대신 Vite의 preview 서버를 사용합니다.
2. **네트워크**: 별도의 네트워크 설정 없이 Docker의 기본 브리지 네트워크를 사용합니다.
3. **데이터 영속성**: MariaDB 데이터는 `mariadb_data` 볼륨에 저장됩니다.
4. **빌드 시간**: 첫 빌드는 의존성 다운로드로 인해 시간이 걸릴 수 있습니다.

## 🚢 프로덕션 배포

프로덕션 환경에서는 다음을 고려하세요:

1. `.env` 파일의 보안 관리
2. HTTPS 설정 (리버스 프록시 사용 권장)
3. 데이터베이스 백업 전략
4. 로그 관리 및 모니터링
5. 리소스 제한 설정

```yaml
# docker-compose.yml에 리소스 제한 추가 예시
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 1G
```