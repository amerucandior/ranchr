# Hardened, minimal runtime container for Ranchr
FROM docker.io/library/eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app

# Run as non-privileged service user
RUN addgroup -S ranchr && adduser -S ranchr -G ranchr

# Copy pre-packaged Spring Boot executable jar
COPY --chown=ranchr:ranchr target/*.jar app.jar

USER ranchr:ranchr

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
