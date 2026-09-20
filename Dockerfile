# Stage 1: Build stage
FROM maven:3-eclipse-temurin-24-alpine AS builder
WORKDIR /build

# Copy dependency definition and source code
COPY pom.xml .
COPY src ./src

# Build production jar skipping tests (tests run in CI stage)
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: Create non-root system group and user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy built artifact from builder stage
COPY --from=builder /build/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

# Run as non-root user
USER appuser

EXPOSE 8080

# Health check wired to Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]