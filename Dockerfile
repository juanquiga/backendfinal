# Multi-stage Dockerfile for a Spring Boot app using the Maven wrapper
# Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
# Copy only what is needed for dependency resolution first
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src ./src
RUN chmod +x mvnw
RUN mvn -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
ARG JAR_FILE=target/*.jar
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
