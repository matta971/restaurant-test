# Multi-stage build for Reservation Service
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy parent pom first
COPY pom.xml ./

# Copy ALL module poms (nécessaire pour la résolution des dépendances)
COPY common/pom.xml ./common/
COPY restaurant-service/pom.xml ./restaurant-service/
COPY reservation-service/pom.xml ./reservation-service/
COPY api-gateway/pom.xml ./api-gateway/

# Copy common module source (requis comme dépendance)
COPY common/src ./common/src

COPY reservation-service/src ./reservation-service/src

# Create empty src directories for other modules (to satisfy Maven structure)
RUN mkdir -p ./reservation-service/src/main/java && \
    mkdir -p ./reservation-service/src/main/resources && \
    mkdir -p ./api-gateway/src/main/java && \
    mkdir -p ./api-gateway/src/main/resources

# Build the application
RUN mvn clean package -DskipTests -pl reservation-service -am

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create app user for security
RUN addgroup -g 1001 appuser && \
    adduser -u 1001 -G appuser -s /bin/sh -D appuser


WORKDIR /app

# Copy the built jar
COPY --from=builder /app/reservation-service/target/reservation-service-*.jar app.jar

# Create config directory and set permissions
RUN mkdir -p /app/config && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

EXPOSE 8082
# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8082/actuator/health || exit 1



ENTRYPOINT ["java", "-jar", "app.jar"]