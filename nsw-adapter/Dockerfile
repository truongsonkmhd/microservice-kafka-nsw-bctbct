FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY common common
COPY nsw-adapter nsw-adapter
RUN mvn install -N -DskipTests && \
    mvn -f common/pom.xml install -DskipTests && \
    mvn -f nsw-adapter/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/nsw-adapter/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
