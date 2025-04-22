# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder
# Install Maven
RUN apk add --no-cache maven
WORKDIR /app
COPY . .
# Use Maven to build the project
RUN mvn clean package -Dmaven.test.skip=true
# Run stage
FROM eclipse-temurin:17-jre-alpine AS runner
WORKDIR /app
COPY --from=builder /app/target/*.jar ./app.jar 
# Run the application
CMD ["java", "-jar", "app.jar"]
