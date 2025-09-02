# Multi-stage build for Restaurant Service
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy parent pom and common module first (for better caching)
COPY pom.xml ./
COPY common/pom.xml ./common/
COPY common/src ./common/src

# Copy restaurant service
COPY restaurant-service/pom.xml ./restaurant-service/
COPY restaurant-service/src ./restaurant-service/src

# Build the application
RUN mvn clean package -pl restaurant-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create app user for security
RUN addgroup -g 1001 appuser && \
    adduser -u 1001 -G appuser -s /bin/sh -D appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/restaurant-service/target/restaurant-service-*.jar app.jar

# Create config directory
RUN mkdir -p /app/config && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Labels for metadata
LABEL maintainer="restaurant-system-team"
LABEL version="1.0.0"
LABEL description="Restaurant Service - Manages restaurant data and table availability"