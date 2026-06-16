FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY common common
COPY bct-gateway bct-gateway
RUN mvn install -N -DskipTests && \
    mvn -f common/pom.xml install -DskipTests && \
    mvn -f bct-gateway/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/bct-gateway/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
