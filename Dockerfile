FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY target/*.jar app.jar

RUN useradd -u 10001 appuser && chown -R appuser /app
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]