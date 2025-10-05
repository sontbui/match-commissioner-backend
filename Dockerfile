# -------- STAGE 1: BUILD JAR --------
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B


COPY src ./src
COPY .env .env
RUN mvn clean package -DskipTests

# -------- STAGE 2: RUN APP --------
FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY .env   .env

# Expose port 8080
EXPOSE 8080

# Run application
ENTRYPOINT ["java","-jar","app.jar"]
