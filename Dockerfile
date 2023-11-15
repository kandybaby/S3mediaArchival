# Frontend build stage
FROM node:18 as frontend-builder

# Set the working directory for the frontend build
WORKDIR /app/frontend

# Copy the frontend project files into the container
COPY frontend/ .

# Install dependencies and build the frontend
RUN npm install
RUN npm run build

# Backend build stage
FROM maven:3.8.5-openjdk-18 as backend-builder

# Copy the entire project into the container
COPY . /app

# Change working directory to the root of the project
WORKDIR /app

# Build the entire project (including backend)
RUN mvn clean package -DskipTests

# Final stage
FROM bellsoft/liberica-openjdk-alpine:18.0.2.1

# Copy the backend JAR file from the builder stage
COPY --from=backend-builder /app/backend/target/MediaArchivalBackend-0.1.1.jar /app/app.jar

WORKDIR /app

# Run the application
CMD ["java", "-jar", "app.jar"]
