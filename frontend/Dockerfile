# syntax=docker/dockerfile:1

############################################################
# 1) Build stage
############################################################
FROM node:20-alpine AS build
WORKDIR /app

# 의존성 캐시
COPY package*.json ./
RUN npm ci

# 소스 복사
COPY . .

# API 베이스 URL 주입 (없으면 코드 기본값 사용)
ARG VITE_API_BASE_URL=https://familyqapi.hibiscus.biz
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

# 빌드
RUN npm run build

############################################################
# 2) Runtime stage (Node + static serve)
############################################################
FROM node:20-alpine
WORKDIR /app

# 타임존 한국
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# 정적 파일 배포 준비
COPY --from=build /app/dist ./dist

# 정적 서버 설치 (serve)
RUN npm install -g serve@14

# 비루트 사용자
USER node

EXPOSE 3001

# serve로 dist 제공 (3001 포트)
CMD ["serve", "-s", "dist", "-l", "3001"]
