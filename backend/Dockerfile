# syntax=docker/dockerfile:1

FROM gradle:9.2.1-jdk17-alpine AS build
WORKDIR /workspace

# 1) Gradle 관련 파일 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 2) wrapper 실행 권한 부여
RUN chmod +x gradlew

# 3) Gradle distribution 및 의존성 캐시 워밍 (소스 없이 실행)
#    → distribution 다운로드 + dependencies 캐시를 레이어에 저장해 이후 빌드 가속
RUN ./gradlew --no-daemon --version
RUN ./gradlew --no-daemon dependencies

# 4) 그 다음에야 소스 복사
#    → 소스가 변경돼도 이 아래 레이어만 다시 빌드되므로,
#       위에서 받은 Gradle distribution은 다시 안 받아도 됩니다.
COPY src src

# 5) 실제 빌드
#    필요에 따라 clean 제거 가능 (속도 vs 안정성 트레이드오프)
ARG SKIP_TESTS=true
RUN if [ "$SKIP_TESTS" = "true" ]; then \
      ./gradlew --no-daemon bootJar -x test; \
    else \
      ./gradlew --no-daemon bootJar; \
    fi

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

COPY --from=build /workspace/build/libs/*.jar /app/app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# 실제 애플리케이션 포트에 맞춰 조정 (필요시)
EXPOSE 3000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
