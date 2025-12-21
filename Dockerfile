# Stage 1: Build the application using the confirmed Maven tag with JDK 17
# This is a verified tag from the Docker Hub list you provided.
FROM maven:3.9.12-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and source files
# The build context is the root (where pom.xml is)
COPY pom.xml .
COPY src ./src

# Build the JAR file. The cache mount for Maven dependencies should work here.
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests


# Stage 2: Create the final image (minimal JRE for smaller size)
# Use JRE 17
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Configuration
EXPOSE [8080,8050]
ENTRYPOINT ["java", "-jar", "app.jar"]