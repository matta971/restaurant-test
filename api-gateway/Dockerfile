# Multi-stage build for API Gateway
FROM openjdk:17-jdk-slim as builder

WORKDIR /app

# Copy parent pom and common module first for better layer caching
COPY pom.xml ./
COPY common/pom.xml ./common/
COPY common/src ./common/src

# Copy api gateway
COPY api-gateway/pom.xml ./api-gateway/
COPY api-gateway/src ./api-gateway/src

# Build the application
RUN mvn clean package -DskipTests -pl api-gateway -am

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Create non-root user
RUN adduser --system --group gateway

# Copy the built jar
COPY --from=builder /app/api-gateway/target/*.jar app.jar

# Change ownership
RUN chown -R gateway:gateway /app
USER gateway

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]