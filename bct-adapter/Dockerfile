FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY common common
COPY bct-adapter bct-adapter
RUN mvn install -N -DskipTests && \
    mvn -f common/pom.xml install -DskipTests && \
    mvn -f bct-adapter/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/bct-adapter/target/bct-adapter-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
