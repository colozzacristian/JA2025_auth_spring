# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:resolve

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose default port
EXPOSE 8083

# Environment variables with defaults
ENV PORT=8083
ENV JWT_SECRET=apdapdjaodcjnoajdoadijoajdoajnaaaaaaaaaaaaaaaaaaaaa
ENV token_expiration=3600
ENV backoff_time_seconds=300
ENV max_failed_requests=5
ENV whitelist_ips=
ENV otp_length=6
ENV otp_characters=0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz
ENV BACKOFFICE_SERVICE_URL=http://localhost:8090
ENV MESSAGE_SERVICE_URL=http://localhost:8071
ENV CORS_ALLOWED_ORIGINS=http://localhost:4200,http://127.0.0.1:4200

# Run the application
CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT} --token.secret=${JWT_SECRET} --token.expiration=${token_expiration} --limiting.backoff=${backoff_time_seconds} --limiting.maxFailedRequests=${max_failed_requests} --limiting.whitelist=${whitelist_ips} --otp.length=${otp_length} --otp.characters=${otp_characters} --url.backoffice=${BACKOFFICE_SERVICE_URL} --url.message=${MESSAGE_SERVICE_URL} --cors.allowedOrigins=${CORS_ALLOWED_ORIGINS}"]
