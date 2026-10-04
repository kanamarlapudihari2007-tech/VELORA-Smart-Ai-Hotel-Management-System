# ==============================================================================
# Velora Smart Hotel Platform - Production Dockerfile
# Multi-stage build: 1) Maven Build, 2) Tomcat 9 Deployment
# ==============================================================================

# Stage 1: Build the WAR artifact
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production WAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime Environment with Apache Tomcat 9
FROM tomcat:9.0-jdk17-corretto

LABEL maintainer="Velora Smart Hotel Team"
LABEL description="Production deployment container for Velora AI Smart Hotel"

# Remove default Tomcat web applications
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy built WAR file from build stage as ROOT.war or ai-smart-hotel.war

COPY --from=build /app/target/ai-smart-hotel.war /usr/local/tomcat/webapps/ROOT.war

# Expose default HTTP port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]
