# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn install package -DskipTests

# Stage 2: Runtime stage
FROM jetty:11.0-jre17

# Copy the built WAR file from the builder stage
COPY --from=builder /build/target/authModule.war /var/lib/jetty/webapps/authModule.war

# Expose the default port for the application
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8080/authModule || exit 1
