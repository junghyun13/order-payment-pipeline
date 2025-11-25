# builder
FROM gradle:8.9-jdk21 AS builder
WORKDIR /workspace
COPY --chown=gradle:gradle . .
RUN gradle bootJar -x test

# runner
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# netcat 설치 (Redis/Elasticsearch 연결 확인용)
RUN apt-get update && apt-get install -y netcat wget && rm -rf /var/lib/apt/lists/*

# wait-for-it.sh 복사
COPY infra/wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

# 빌드 결과 jar 복사
COPY --from=builder /workspace/build/libs/*jar /app/app.jar

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1



