FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /home/app
COPY . /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM  bellsoft/liberica-openjdk-alpine:17
EXPOSE 8080
COPY --from=build /home/app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]