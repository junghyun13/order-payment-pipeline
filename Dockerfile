# builder
FROM gradle:8.9-jdk21 AS builder
WORKDIR /workspace
COPY --chown=gradle:gradle . .
RUN gradle bootJar -x test

# runner
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /workspace/build/libs/*jar /app/app.jar
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java","-jar","/app/app.jar"]
