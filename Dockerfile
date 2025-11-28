# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

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
EXPOSE 8080

# Set environment variables for database connection (these can be overridden at runtime)
ENV DB_HOST=mysql \
    DB_PORT=3306 \
    DB_NAME=projectWork \
    DB_USER=root \
    DB_PASSWORD=4nt4n1 \
    JWT_SECRET_KEY=your-secret-key-here

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8080/authModule || exit 1

# Run the application with Jetty
CMD ["java", "-jar", "/app/authModule.war"]
