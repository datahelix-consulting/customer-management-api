# Stage 1: Build the application
FROM maven:3.9.0-amazoncorretto-17 AS builder
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM amazoncorretto:23.0.1-alpine
WORKDIR /app
COPY --from=builder /app/target/customer-management-api-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
