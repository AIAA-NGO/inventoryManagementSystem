# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy everything into the container
COPY . .

# Build the application
RUN ./mvnw clean install -DskipTests

# Expose port 8080 (default Spring Boot port)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/inventoryManagementSystem-0.0.1-SNAPSHOT.jar"]
