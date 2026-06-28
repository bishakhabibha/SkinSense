# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy project files
COPY . .

# Build Spring Boot jar (skip tests for speed)
RUN mvn clean package -DskipTests

---

# Stage 2: Run the application
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy built jar from previous stage
COPY --from=build /app/target/*.jar app.jar

# Render will inject PORT dynamically
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]