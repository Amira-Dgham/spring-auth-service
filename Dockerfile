# Build stage - using official Maven image with exact tag
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# 1. Copy only pom.xml first
COPY pom.xml .

# 2. Download dependencies (this layer will cache as long as pom.xml doesn't change)
RUN mvn dependency:go-offline

# 3. Now copy source code
COPY src ./src

# Build with profile from build arg
ARG ENV=dev
RUN mvn clean package -P${ENV} -DskipTests

# Run stage - using official Eclipse Temurin JRE
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Use ENV variable passed at runtime
ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.profiles.active=${ENV}"]