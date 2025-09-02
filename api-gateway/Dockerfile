# Multi-stage build for API Gateway
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy parent pom and common module first (for better caching)
COPY pom.xml ./

# Copy ALL module poms (nécessaire pour la résolution des dépendances)
COPY common/pom.xml ./common/
COPY restaurant-service/pom.xml ./restaurant-service/
COPY reservation-service/pom.xml ./reservation-service/
COPY api-gateway/pom.xml ./api-gateway/

# Copy common module source (requis comme dépendance)
COPY common/src ./common/src

# Copy api-gateway module source
COPY api-gateway/src ./api-gateway/src

# Create empty src directories for other modules (to satisfy Maven structure)
RUN mkdir -p ./reservation-service/src/main/java && \
    mkdir -p ./reservation-service/src/main/resources && \
    mkdir -p ./api-gateway/src/main/java && \
    mkdir -p ./api-gateway/src/main/resources

# Build the application
RUN mvn clean package -pl api-gateway -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Install wget for health checks (curl not available in alpine)
RUN apk add --no-cache wget

# Create app user for security
RUN addgroup -g 1001 appuser && \
    adduser -u 1001 -G appuser -s /bin/sh -D appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/api-gateway/target/api-gateway-*.jar app.jar

# Create config directory
RUN mkdir -p /app/config && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

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
LABEL description="API Gateway - Routes and secures requests to microservices"