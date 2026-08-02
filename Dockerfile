# 1단계: 빌드
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# 빌드에 필요한 파일들 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

# 실행 권한 부여 및 빌드 (테스트 제외)
RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test

# 2. 실행 스테이지 (실행할 때는 경량화된 JRE 버전 사용)
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드 스테이지에서 생성된 jar 파일만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 개방 및 타임존 설정
EXPOSE 8080
ENV TZ=Asia/Seoul

# 실행 컨텍스트 설정
ENTRYPOINT ["java", "-jar", "app.jar"]