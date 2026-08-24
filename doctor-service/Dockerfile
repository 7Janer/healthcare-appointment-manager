FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl doctor-service -am -DskipTests package
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/doctor-service/target/doctor-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]
