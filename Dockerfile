# -------- STAGE 1: BUILD JAR --------
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B


COPY src ./src
RUN mvn clean package -DskipTests

# -------- STAGE 2: RUN APP --------
FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar


# Expose port 8080
EXPOSE 8080

# Health check script
COPY healthcheck.sh /healthcheck.sh
RUN chmod +x /healthcheck.sh

# -------- HEALTH CHECK --------
HEALTHCHECK --interval=250s --timeout=5s --start-period=20s --retries=3 \
  CMD /healthcheck.sh

# -------- ENTRYPOINT --------
ENTRYPOINT ["java","-jar","app.jar"]
