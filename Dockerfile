# 멀티스테이지 빌드를 위한 Dockerfile
FROM amazoncorretto:21-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 래퍼와 빌드 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src src

# 실행 권한 부여 및 빌드
RUN chmod +x gradlew
RUN ./gradlew build -x test --no-daemon

# 런타임 스테이지
FROM amazoncorretto:21-alpine

# 비루트 사용자 생성
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 작업 디렉토리 설정
WORKDIR /app

# 업로드 디렉토리 생성
RUN mkdir -p /app/uploads && \
    chown -R appuser:appgroup /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 파일 소유권 변경
RUN chown appuser:appgroup app.jar

# 비루트 사용자로 전환
USER appuser

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 포트 노출
EXPOSE 8080

# JVM 옵션 설정
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=docker -jar app.jar"]
