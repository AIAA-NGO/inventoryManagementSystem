# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy everything into the container
COPY . .

# Move to backend folder
WORKDIR /app/backend

# Make mvnw executable
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean install -DskipTests

# Expose port 8080 (Spring Boot default)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/inventoryManagementSystem-0.0.1-SNAPSHOT.jar"]
