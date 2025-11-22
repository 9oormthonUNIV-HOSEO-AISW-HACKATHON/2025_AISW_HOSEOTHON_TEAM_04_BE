# ============================================
# Stage 1: Build
# ============================================
FROM node:20-alpine AS build

WORKDIR /app

# package.json과 package-lock.json 복사 (캐싱 최적화)
COPY package*.json ./

# 의존성 설치
RUN npm ci

# 소스 코드 복사
COPY . .

# Vite 빌드
RUN npm run build

# ============================================
# Stage 2: Production without Nginx
# ============================================
FROM node:20-alpine

WORKDIR /app

# 빌드된 파일과 필요한 파일들 복사
COPY --from=build /app/dist ./dist
COPY --from=build /app/package*.json ./
COPY --from=build /app/vite.config.js ./

# production 의존성만 설치 (vite 포함)
RUN npm ci --only=production && \
    npm install vite

# 타임존 설정 (한국 시간)
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# 비루트 사용자 생성 및 권한 설정
RUN addgroup -g 1001 -S nodejs && \
    adduser -S nodejs -u 1001 && \
    chown -R nodejs:nodejs /app

USER nodejs

# 포트 노출
EXPOSE 3007

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD node -e "require('http').get('http://localhost:3007', (res) => { process.exit(res.statusCode === 200 ? 0 : 1); });" || exit 1

# Vite preview 서버 실행 (포트 3007)
CMD ["npx", "vite", "preview", "--host", "0.0.0.0", "--port", "3007"]
