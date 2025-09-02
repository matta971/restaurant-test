# Multi-stage build for Reservation Service
FROM openjdk:17-jdk-slim as builder

WORKDIR /app

# Copy parent pom and common module first for better layer caching
COPY pom.xml ./
COPY common/pom.xml ./common/
COPY common/src ./common/src

# Copy reservation service
COPY reservation-service/pom.xml ./reservation-service/
COPY reservation-service/src ./reservation-service/src

# Build the application
RUN mvn clean package -DskipTests -pl reservation-service -am

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Create non-root user
RUN adduser --system --group reservation

# Copy the built jar
COPY --from=builder /app/reservation-service/target/*.jar app.jar

# Change ownership
RUN chown -R reservation:reservation /app
USER reservation

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8082/actuator/health || exit 1

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]