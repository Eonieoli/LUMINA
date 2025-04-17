# Stage 1: Spring Boot 앱 빌드

# Gradle LTS 버전 사용
FROM gradle:8.12-jdk21-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /home/gradle/project

# 전체 소스코드를 복사
COPY --chown=gradle:gradle . .

# gradlew에 실행 권한 부여
RUN chmod +x gradlew

# Gradle 빌드 수행
RUN ./gradlew clean build --no-daemon -x test


# Stage 2: Spring Boot 앱 실행

# 경량화 JRE 이미지 사용
FROM eclipse-temurin:21-jre-alpine

# alpine 기반 컨테이너에 curl 명령어 설치
RUN apk add --no-cache curl

# 작업 디렉토리 생성
WORKDIR /app

# 빌드된 JAR 파일을 고정된 이름으로 복사
COPY --from=builder /home/gradle/project/build/libs/app.jar app.jar

# 실행 포트
EXPOSE 8080

# 실행 명령
ENTRYPOINT ["java", "-jar", "/app/app.jar"]