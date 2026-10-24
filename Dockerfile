# Stage 1: Build application using Maven and Temurin Java 25
FROM maven:3-eclipse-temurin-25-alpine AS builder
WORKDIR /workspace

# Pre-cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package application jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Hardened, minimal runtime container
FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app

# Run as non-privileged service user
RUN addgroup -S ranchr && adduser -S ranchr -G ranchr

# Copy jar from builder stage
COPY --from=builder --chown=ranchr:ranchr /workspace/target/*.jar app.jar

USER ranchr:ranchr

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
