# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src
COPY .env .

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Install Jetty
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    default-jre-headless \
    ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Copy the built WAR file from the builder stage
COPY --from=builder /build/target/authModule.war /app/authModule.war

# Expose the default port for the application
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8081/authModule || exit 1
# Run the application with Jetty
CMD ["java", "-jar", "/app/authModule.war"]
