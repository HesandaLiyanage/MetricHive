# Use an official OpenJDK runtime as base image
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Copy the jar file from target directory
COPY target/*.jar app.jar

# Expose the port your Spring app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]