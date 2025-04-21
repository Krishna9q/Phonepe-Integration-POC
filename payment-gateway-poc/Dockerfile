# Use an official Maven image as the base image
FROM maven:3.8.8-openjdk-17 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code into the container
COPY src ./src

# Package the application
RUN mvn package -DskipTests

# Use an official OpenJDK image to run the application
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "app.jar"]