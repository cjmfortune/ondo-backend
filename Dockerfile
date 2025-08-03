# 1단계: JDK 이미지 기반
FROM openjdk:21-jdk-slim

# 2단계: JAR 파일을 컨테이너에 복사 (plain이 아닌 실행 가능한 JAR 파일 선택)
COPY build/libs/choi-0.0.1-SNAPSHOT.jar app.jar

# 3단계: 애플리케이션 실행
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "/app.jar"]
