# Build JAR
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

# Run JAR
FROM openjdk:17-jdk
ENV TZ Africa/Nairobi
ARG DOCKER_SPRING_PROFILES_ACTIVE
ENV SPRING_PROFILES_ACTIVE=$DOCKER_SPRING_PROFILES_ACTIVE
ARG DB_USER
ENV DB_USER=$DB_USER
ARG DB_PASS
ENV DB_PASS=$DB_PASS
ARG DB_NAME
ENV DB_NAME=$DB_NAME
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Djavamelody.disabled=true", "-jar", "app.jar"]
